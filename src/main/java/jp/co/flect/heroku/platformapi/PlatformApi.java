package jp.co.flect.heroku.platformapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import jp.co.flect.heroku.HerokuException;
import jp.co.flect.heroku.json.JsonUtils;
import jp.co.flect.heroku.transport.Transport;
import jp.co.flect.heroku.transport.TransportFactory;
import jp.co.flect.heroku.transport.HttpRequest;
import jp.co.flect.heroku.transport.HttpResponse;
import jp.co.flect.heroku.platformapi.model.AbstractModel;
import jp.co.flect.heroku.platformapi.model.Account;
import jp.co.flect.heroku.platformapi.model.Addon;
import jp.co.flect.heroku.platformapi.model.App;
import jp.co.flect.heroku.platformapi.model.AddonService;
import jp.co.flect.heroku.platformapi.model.ConfigVars;
import jp.co.flect.heroku.platformapi.model.RateLimits;
import jp.co.flect.heroku.platformapi.model.Region;
import jp.co.flect.heroku.platformapi.model.Release;
import jp.co.flect.heroku.platformapi.model.Collaborator;
import jp.co.flect.heroku.platformapi.model.Formation;
import jp.co.flect.heroku.platformapi.model.Dyno;
import jp.co.flect.heroku.platformapi.model.Range;

import org.apache.commons.codec.binary.Base64;

public class PlatformApi implements Serializable {
	
	private  static final long serialVersionUID = 6015604605302262585L;
	
	public static final String HOST_ID  = "https://id.heroku.com";
	public static final String HOST_API = "https://api.heroku.com";
	
	public enum Scope {
		Global("global"),
		Identity("identity"),
		Read("read"),
		Write("write"),
		ReadProtected("read-protected"),
		WriteProtected("write-protected")
		;
		
		private String value;
		
		private Scope(String value) {
			this.value = value;
		}
		
		public String toString() { return this.value;}
	}
	
	public static String getOAuthUrl(String clientId, Scope... scope) {
		StringBuilder buf = new StringBuilder();
		for (Scope s : scope) {
			if (buf.length() > 0) {
				buf.append("%20");
			}
			buf.append(s);
		}
		return HOST_ID + "/oauth/authorize?client_id=" + clientId + "&response_type=code&scope=" + buf.toString();
	}
	
	public static PlatformApi authenticate(String secret, String code) throws IOException {
		HttpRequest request = new HttpRequest(HttpRequest.Method.POST, HOST_ID + "/oauth/token");
		request.setParameter("grant_type", "authorization_code");
		request.setParameter("code", code);
		request.setParameter("client_secret", secret);
		
		Transport tran = TransportFactory.createDefaultTransport();
		HttpResponse res = tran.execute(request);
		if (res.getStatus() == 200) {
			return fromJson(res.getBody());
		} else {
			throw new HerokuException(res.getBody());
		}
	}
	
	public static PlatformApi fromJson(String json) {
		return JsonUtils.parse(json, PlatformApi.class);
	}
	
	private String access_token;
	private int expires_in;
	private String refresh_token;
	private String token_type;
	private String session_nonce;
	
	private boolean debug = false;
	private int rateLimitRemaining = -1;
	private long requestStart;
	private Transport transport = TransportFactory.createDefaultTransport();
	
	private Account account;
	private String loginedEmail;
	
	public PlatformApi() {
	}
	
	/**
	 * //ToDo Not work with username and apiKey
	 * https://devcenter.heroku.com/articles/platform-api-reference#authentication
	 */
	public PlatformApi(String apiKey) {
		try {
			this.access_token = Base64.encodeBase64String((":" + apiKey).getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public Transport getTransport() { return this.transport;}
		
	public boolean isDebug() { return this.debug;}
	public void setDebug(boolean b) { this.debug = b;}
	
	public int getRateLimitRemaining() { return this.rateLimitRemaining;}
	
	private void debugLog(String name, String value) {
		if (this.debug) {
			long t = System.currentTimeMillis() - this.requestStart;
			System.out.println("PlatformApi - " + name + "(" + t + "ms): " + value);
		}
	}
	
	public String getAuthorization() {
		return this.token_type == null ? this.access_token : this.token_type + " " + this.access_token;
	}
	
	private HttpRequest buildRequest(HttpRequest.Method method, String path) {
		return buildRequest(method, path, null);
	}
	
	private HttpRequest buildRequest(HttpRequest.Method method, String path, Range range) {
		this.requestStart = System.currentTimeMillis();
		HttpRequest request = new HttpRequest(method, HOST_API + path);
		request.setHeader("Accept", "application/vnd.heroku+json; version=3");
		request.setHeader("Authorization", getAuthorization());
		if (method == HttpRequest.Method.POST || method == HttpRequest.Method.PATCH || method == HttpRequest.Method.PUT) {
			request.setHeader("content-type", "application/json");
		}
		if (range != null) {
			range.apply(request);
		}
		return request;
	}
	
	private <T extends AbstractModel> List<T> handleResponse(String name, HttpResponse res, Class<T> returnClass) throws IOException {
		return handleResponse(name, res, returnClass, null);
	}
	
	private <T extends AbstractModel> List<T> handleResponse(String name, HttpResponse res, Class<T> returnClass, Range range) throws IOException {
		String body = res.getBody();
		int status = res.getStatus();
		
		debugLog(name, body);
		
		String rlr = res.getHeader("RateLimit-Remaining");
		if (rlr != null) {
			try {
				this.rateLimitRemaining = Integer.parseInt(rlr);
			} catch (NumberFormatException e) {
				//not occur
				e.printStackTrace();
			}
		}
		String requestId = res.getHeader("Request-Id");
		if (range != null) {
			String ar = res.getHeader("Accept-Ranges");
			if (ar != null) {
				String[] array = ar.split(",");
				for (int i=0; i<array.length; i++) {
					array[i] = array[i].trim();
				}
				range.setSortableFields(array);
			}
			String nr = res.getHeader("Next-Range");
			if (nr != null) {
				range.setNextRange(new Range(nr));
			}
		}
		
		if (status >= 200 && status < 300) {
			List<Map<String, Object>> maps = JsonUtils.parseArray(body);
			if (maps == null) {
				return null;
			}
			List<T> list = new ArrayList<T>();
			for (Map<String, Object> m : maps) {
				try {
					T obj = returnClass.newInstance();
					obj.init(m);
					obj.setRequestId(requestId);
					list.add(obj);
				} catch (InstantiationException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
			return list;
		} else {
			if (body != null && body.indexOf("\"id\"") != -1 && body.indexOf("\"message\"") != -1) {
				PlatformApiException.Error e = JsonUtils.parse(body, PlatformApiException.Error.class);
				throw new PlatformApiException(status, e);
			} else {
				throw new IOException("status=" + status + ", body=" + body);
			}
		}
	}
	
	public String getLoginedEmail() {
		if (this.loginedEmail != null) {
			return this.loginedEmail;
		}
		try {
			Account a = getAccount();
			this.loginedEmail = a.getEmail();
		} catch (IOException e) {
			this.loginedEmail = e.getMessage();
		}
		return this.loginedEmail;
	}
	
	public int getRateLimits() throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account/rate-limits"));
		return handleResponse("getRateLimits", res, RateLimits.class).get(0).getRemaining();
	}
	
	//Account
	public Account getAccount() throws IOException {
		if (this.account != null) {
			return this.account;
		}
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account"));
		this.account = handleResponse("getAccount", res, Account.class).get(0);
		return this.account;
	}
	
	public void changePassword(String currentPassword, String newPassword) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PUT, "/account/password");
		request.setParameter("current_password", currentPassword);
		request.setParameter("password", newPassword);
		HttpResponse res = getTransport().execute(request);
		handleResponse("changePassword", res, ConfigVars.class);
	}
	
	//Addon
	public List<Addon> getAddonList(String appName) throws IOException {
		return getAddonList(appName, null);
	}
	
	public List<Addon> getAddonList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/addons", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAddonList", res, Addon.class, range);
	}
	
	public Addon getAddon(String appName, String idOrName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/addons/" + idOrName));
		return handleResponse("getAddon", res, Addon.class).get(0);
	}
	
	//AddonService
	public List<AddonService> getAddonServiceList() throws IOException {
		return getAddonServiceList(null);
	}
	
	public List<AddonService> getAddonServiceList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/addon-services", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAddonServiceList", res, AddonService.class, range);
	}
	
	public AddonService getAddonService(String idOrName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/addon-services/" + idOrName));
		return handleResponse("getAddonService", res, AddonService.class).get(0);
	}
	
	//App
	public List<App> getAppList() throws IOException {
		return getAppList(null);
	}
	
	public List<App> getAppList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAppList", res, App.class, range);
	}
	
	public App getApp(String name) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps/" + name));
		return handleResponse("getApp", res, App.class).get(0);
	}

	public App createApp(String name, Region region) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps");
		request.setParameter("name", name);
		request.setParameter("region.name", region.toString());
		HttpResponse res = getTransport().execute(request);
		return handleResponse("createApp", res, App.class).get(0);
	}

	public App deleteApp(String name) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + name);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteApp", res, App.class).get(0);
	}

	public App renameApp(String name, String newName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + name);
		request.setParameter("name", newName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("renameApp", res, App.class).get(0);
	}
	
	public App maintainApp(String name, boolean maintain) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + name);
		request.setParameter("maintenance", maintain);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("maintainApp", res, App.class).get(0);
	}

	//Config
	public ConfigVars getConfigVars(String name) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps/" + name + "/config-vars"));
		return handleResponse("getConfigVars", res, ConfigVars.class).get(0);
	}

	public ConfigVars setConfigVar(String appName, String name, String value) throws IOException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(name, value);
		return setConfigVars(appName, vars);
	}
	
	public ConfigVars setConfigVars(String appName, Map<String, String> vars) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + appName + "/config-vars");
		for (Map.Entry<String, String> entry : vars.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			request.setParameter(name, value);
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("setConfigVars", res, ConfigVars.class).get(0);
	}
	
	//Release
	public List<Release> getReleaseList(String appName) throws IOException {
		Range range = new Range();
		range.setSortOrder("version", false);
		return getReleaseList(appName, range);
	}
	
	public List<Release> getReleaseList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/releases", range);
		HttpResponse res = getTransport().execute(request);
		List<Release> list = handleResponse("getReleaseList", res, Release.class, range);
		return list;
	}
	
	public Release getRelease(String appName, String idOrVersion) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/releases/" + idOrVersion);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getRelease", res, Release.class).get(0);
	}
	
	//Collaborator
	public List<Collaborator> getCollaboratorList(String appName) throws IOException {
		return getCollaboratorList(appName, null);
	}
	
	public List<Collaborator> getCollaboratorList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/collaborators", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getCollaboratorList", res, Collaborator.class, range);
	}
	
	public Collaborator getCollaborator(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/collaborators/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getCollaborator", res, Collaborator.class).get(0);
	}
	
	public Collaborator addCollaborator(String appName, String idOrName) throws IOException {
		return addCollaborator(appName, idOrName, false);
	}
	
	public Collaborator addCollaborator(String appName, String idOrName, boolean silent) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/collaborators");
		if (silent) {
			request.setParameter("silent", silent);
		}
		if (idOrName.indexOf("@") == -1) {
			request.setParameter("user.id", idOrName);
		} else {
			request.setParameter("user.email", idOrName);
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addCollaborator", res, Collaborator.class).get(0);
	}
	
	public Collaborator deleteCollaborator(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/collaborators/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteCollaborator", res, Collaborator.class).get(0);
	}
	
	//Formation
	public List<Formation> getFormationList(String appName) throws IOException {
		return getFormationList(appName, null);
	}
	
	public List<Formation> getFormationList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/formation", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getFormationList", res, Formation.class, range);
	}
	
	public Formation getFormation(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/formation/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getFormation", res, Formation.class).get(0);
	}
	
	public Formation updateFormation(String appName, String idOrName, int quantity, int size) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + appName + "/formation/" + idOrName);
		if (quantity >= 0) {
			request.setParameter("quantity", quantity);
		}
		if (size >= 0) {
			request.setParameter("size", size);
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateFormation", res, Formation.class).get(0);
	}
	
	//Dyno
	public List<Dyno> getDynoList(String appName) throws IOException {
		return getDynoList(appName, null);
	}
	
	public List<Dyno> getDynoList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/dynos", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getDynoList", res, Dyno.class, range);
	}
	
	public Dyno getDyno(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/dynos/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getDyno", res, Dyno.class).get(0);
	}
	
	public void deleteDyno(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/dynos/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		handleResponse("deleteDyno", res, Dyno.class);
	}
	
	public Dyno runDyno(String appName, String command) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/dynos");
		request.setParameter("command", command);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("runDyno", res, Dyno.class).get(0);
	}
	
}
