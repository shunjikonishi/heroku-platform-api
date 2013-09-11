package jp.co.flect.heroku.platformapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
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
		this.requestStart = System.currentTimeMillis();
		HttpRequest request = new HttpRequest(method, HOST_API + path);
		request.setHeader("Accept", "application/vnd.heroku+json; version=3");
		request.setHeader("Authorization", getAuthorization());
		if (method == HttpRequest.Method.POST || method == HttpRequest.Method.PATCH) {
			request.setHeader("content-type", "application/json");
		}
		return request;
	}
	
	private <T extends AbstractModel> List<T> handleResponse(String name, HttpResponse res, Class<T> returnClass) throws IOException {
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
		
		if (status >= 200 && status < 300) {
			List<T> list = new ArrayList<T>();
			List<Map<String, Object>> maps = JsonUtils.parseArray(body);
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
	
	public Account getAccount() throws IOException {
		if (this.account != null) {
			return this.account;
		}
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account"));
		this.account = handleResponse("getAccount", res, Account.class).get(0);
		return this.account;
	}
	
	public int getRateLimits() throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account/rate-limits"));
		return handleResponse("getRateLimits", res, RateLimits.class).get(0).getRemaining();
	}
	
	//Addon
	public List<Addon> getAddonList(String appName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/addons"));
		return handleResponse("getAddonList", res, Addon.class);
	}
	
	public Addon getAddon(String appName, String idOrName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/addons/" + idOrName));
		return handleResponse("getAddon", res, Addon.class).get(0);
	}
	
	//AddonService
	public List<AddonService> getAddonServiceList() throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/addon-services"));
		return handleResponse("getAddonServiceList", res, AddonService.class);
	}
	
	public AddonService getAddonService(String idOrName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/addon-services/" + idOrName));
		return handleResponse("getAddonServiceList", res, AddonService.class).get(0);
	}
	
	//App
	public List<App> getAppList() throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps"));
		return handleResponse("getAppList", res, App.class);
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
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/releases");
		HttpResponse res = getTransport().execute(request);
		List<Release> list = handleResponse("getReleaseList", res, Release.class);
	}
	
	//Release
	public Release getRelease(String appName, String idOrVersion) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/releases/" + idOrVersion);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getReleaseList", res, Release.class).get(0);
	}
}
