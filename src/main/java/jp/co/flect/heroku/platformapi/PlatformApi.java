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
import jp.co.flect.heroku.platformapi.model.AccountFeature;
import jp.co.flect.heroku.platformapi.model.Addon;
import jp.co.flect.heroku.platformapi.model.App;
import jp.co.flect.heroku.platformapi.model.AppTransfer;
import jp.co.flect.heroku.platformapi.model.AppFeature;
import jp.co.flect.heroku.platformapi.model.Domain;
import jp.co.flect.heroku.platformapi.model.AddonService;
import jp.co.flect.heroku.platformapi.model.ConfigVars;
import jp.co.flect.heroku.platformapi.model.LogDrain;
import jp.co.flect.heroku.platformapi.model.LogSession;
import jp.co.flect.heroku.platformapi.model.RateLimits;
import jp.co.flect.heroku.platformapi.model.Region;
import jp.co.flect.heroku.platformapi.model.Release;
import jp.co.flect.heroku.platformapi.model.Collaborator;
import jp.co.flect.heroku.platformapi.model.Formation;
import jp.co.flect.heroku.platformapi.model.Dyno;
import jp.co.flect.heroku.platformapi.model.Range;
import jp.co.flect.heroku.platformapi.model.Plan;
import jp.co.flect.heroku.platformapi.model.OAuthClient;
import jp.co.flect.heroku.platformapi.model.Key;
import jp.co.flect.heroku.platformapi.model.SSLEndpoint;
import jp.co.flect.heroku.platformapi.model.Stack;

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
	
	public static PlatformApi fromOAuth(String secret, String code) throws IOException {
		HttpRequest request = new HttpRequest(HttpRequest.Method.POST, HOST_ID + "/oauth/token");
		request.setParameter("grant_type", "authorization_code");
		request.setParameter("code", code);
		request.setParameter("client_secret", secret);
		
		Transport tran = TransportFactory.createDefaultTransport();
		HttpResponse res = tran.execute(request);
		if (res.getStatus() == 200) {
			return JsonUtils.parse(res.getBody(), PlatformApi.class);
		} else {
			throw new HerokuException(res.getBody());
		}
	}
	
	public static PlatformApi fromRefreshToken(String secret, String refreshToken) throws IOException {
		HttpRequest request = new HttpRequest(HttpRequest.Method.POST, HOST_ID + "/oauth/token");
		request.setParameter("grant_type", "refresh_token");
		request.setParameter("refresh_token", refreshToken);
		request.setParameter("client_secret", secret);
		
		Transport tran = TransportFactory.createDefaultTransport();
		HttpResponse res = tran.execute(request);
		if (res.getStatus() == 200) {
			return JsonUtils.parse(res.getBody(), PlatformApi.class);
		} else {
			throw new HerokuException(res.getBody());
		}
	}
	
	public static PlatformApi fromPassword(String username, String password) throws IOException {
		HttpRequest request = new HttpRequest(HttpRequest.Method.POST, HOST_API + "/oauth/authorizations");
		String auth = base64(username + ":" + password);
		
		request.setHeader("Accept", "application/vnd.heroku+json; version=3");
		request.setHeader("Authorization", "Basic " + auth);
		
		Transport tran = TransportFactory.createDefaultTransport();
		HttpResponse res = tran.execute(request);
		if (res.getStatus() >= 200 && res.getStatus() < 300) {
			Map<String, Object> map = JsonUtils.parse(res.getBody());
			Map<String, Object> access_token = (Map<String, Object>)map.get("access_token");
			if (access_token == null) {
				throw new HerokuException("Invalid authorization response: " + res.getBody());
			}
			String token = (String)access_token.get("token");
			if (token == null) {
				throw new HerokuException("Invalid authorization response: " + res.getBody());
			}
			return new PlatformApi(token);
		} else {
			throw new HerokuException(res.getStatus() + ": " + res.getBody());
		}
	}
	
	public static PlatformApi fromApiKey(String username, String apiKey) {
		return new PlatformApi(username, apiKey);
	}
	
	private static String base64(String str) {
		try {
			// To use this library in Playframework1, it is necessary to replace "\r\n" to "".
			// Because Play uses commons-codec-1.4 and the behavior of Base64#encodeBase64String was changed from 1.5.
			// In version 1.4 or earlier, this method returns chunked string.
			// See.
			// http://commons.apache.org/proper/commons-codec/apidocs/org/apache/commons/codec/binary/Base64.html#encodeBase64String(byte[])
			return Base64.encodeBase64String(str.getBytes("utf-8")).replaceAll("\r\n", "");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
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
	
	private PlatformApi() {
	}
	
	private PlatformApi(String email, String apiKey) {
		this.access_token = base64(email + ":" + apiKey);
		this.token_type = "Basic";
	}
	
	private PlatformApi(String token) {
		this.access_token = base64(":" + token);
		this.token_type = "Basic";
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
	
	public String getRefreshToken() { return this.refresh_token;}
	
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
	
	public Account updateAccountEmail(String newEmail) throws IOException {
		return updateAccount(newEmail, null);
	}
	
	public Account updateAccountAllowTracking(boolean newAllowTracking) throws IOException {
		return updateAccount(null, newAllowTracking);
	}
	
	public Account updateAccount(String newEmail, Boolean newAllowTracking) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/account");
		if (newEmail != null) {
			request.setParameter("email", newEmail);
		}
		if (newAllowTracking != null) {
			request.setParameter("allow_tracking", newAllowTracking.toString());
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateAccount", res, Account.class).get(0);
	}
	
	public void changePassword(String currentPassword, String newPassword) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PUT, "/account/password");
		request.setParameter("current_password", currentPassword);
		request.setParameter("password", newPassword);
		HttpResponse res = getTransport().execute(request);
		handleResponse("changePassword", res, ConfigVars.class);
	}
	
	public List<AccountFeature> getAccountFeatureList() throws IOException {
		return getAccountFeatureList(null);
	}
	
	public List<AccountFeature> getAccountFeatureList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/account/features", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAccountFeatureList", res, AccountFeature.class, range);
	}
	
	public AccountFeature getAccountFeature(String idOrName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account/features/" + idOrName));
		return handleResponse("getAccountFeature", res, AccountFeature.class).get(0);
	}
	
	public AccountFeature updateAccountFeature(String idOrName, boolean enabled) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/account/features/" + idOrName);
		request.setParameter("enabled", enabled);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateAccountFeature", res, AccountFeature.class).get(0);
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
	
	public Addon addAddon(String appName, String planNameOrId) throws IOException {
		return addAddon(appName, new Addon(planNameOrId));
	}
	
	public Addon addAddon(String appName, Addon addon) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/addons");
		applyAddonToRequest(request, addon);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addAddon", res, Addon.class).get(0);
	}
	
	public Addon updateAddon(String appName, String addonId, Addon addon) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + appName + "/addons/" + addonId);
		applyAddonToRequest(request, addon);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateAddon", res, Addon.class).get(0);
	}
	
	public Addon deleteAddon(String appName, String addonId) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/addons/" + addonId);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteAddon", res, Addon.class).get(0);
	}
	
	private void applyAddonToRequest(HttpRequest request, Addon addon) {
		if (addon.getPlanId() != null) {
			request.setParameter("plan.id", addon.getPlanId());
		}
		if (addon.getPlanName() != null) {
			request.setParameter("plan.name", addon.getPlanName());
		}
		if (addon.getConfig() != null) {
			request.setParameter("config", addon.getConfig());
		}
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
	
	public List<Plan> getAddonPlanList(String idOrName) throws IOException {
		return getAddonPlanList(idOrName, null);
	}
	
	public List<Plan> getAddonPlanList(String idOrName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/addon-services/" + idOrName + "/plans", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAddonPlanList", res, Plan.class, range);
	}
	
	public Plan getAddonPlan(String idOrName, String plan) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/addon-services/" + idOrName + "/plans/" + plan);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAddonPlan", res, Plan.class).get(0);
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
	
	//AppFeature
	public List<AppFeature> getAppFeatureList(String appName) throws IOException {
		return getAppFeatureList(appName, null);
	}
	
	public List<AppFeature> getAppFeatureList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/features", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAppFeatureList", res, AppFeature.class, range);
	}
	
	public AppFeature getAppFeature(String appName, String idOrName) throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/features/" + idOrName));
		return handleResponse("getAppFeature", res, AppFeature.class).get(0);
	}
	
	public AppFeature updateAppFeature(String appName, String idOrName, boolean enabled) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + appName + "/features/" + idOrName);
		request.setParameter("enabled", enabled);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateAppFeature", res, AppFeature.class).get(0);
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
	
	//Domain
	public List<Domain> getDomainList(String appName) throws IOException {
		return getDomainList(appName, null);
	}
	
	public List<Domain> getDomainList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/domains", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getDomainList", res, Domain.class, range);
	}
	
	public Domain getDomain(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/domains/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getDomain", res, Domain.class).get(0);
	}
	
	public Domain addDomain(String appName, String hostName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/domains");
		request.setParameter("hostname", hostName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addDomain", res, Domain.class).get(0);
	}
	
	public Domain deleteDomain(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/domains/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteDomain", res, Domain.class).get(0);
	}
	
	//LogDrain
	public List<LogDrain> getLogDrainList(String appName) throws IOException {
		return getLogDrainList(appName, null);
	}
	
	public List<LogDrain> getLogDrainList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/log-drains", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getLogDrainList", res, LogDrain.class, range);
	}
	
	public LogDrain getLogDrain(String appName, String idOrUrl) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/log-drains/" + idOrUrl);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getLogDrain", res, LogDrain.class).get(0);
	}
	
	public LogDrain addLogDrain(String appName, String url) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/log-drains");
		request.setParameter("url", url);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addLogDrain", res, LogDrain.class).get(0);
	}
	
	public LogDrain deleteLogDrain(String appName, String idOrUrl) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/log-drains/" + idOrUrl);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteLogDrain", res, LogDrain.class).get(0);
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
	
	public void killDyno(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/dynos/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		handleResponse("killDyno", res, Dyno.class);
	}
	
	public void restart(String appName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/dynos");
		HttpResponse res = getTransport().execute(request);
		handleResponse("restart", res, Dyno.class);
	}
	
	public Dyno runDyno(String appName, String command) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/dynos");
		request.setParameter("command", command);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("runDyno", res, Dyno.class).get(0);
	}
	
	//Region
	public List<Region> getRegionList() throws IOException {
		return getRegionList(null);
	}
	
	public List<Region> getRegionList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/regions", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getRegionList", res, Region.class, range);
	}
	
	public Region getRegion(String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/regions/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getRegion", res, Region.class).get(0);
	}
	
	//Stack
	public List<Stack> getStackList() throws IOException {
		return getStackList(null);
	}
	
	public List<Stack> getStackList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/stacks", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getStackList", res, Stack.class, range);
	}
	
	public Stack getStack(String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/stacks/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getStack", res, Stack.class).get(0);
	}
	
	//OAuthClient
	public List<OAuthClient> getOAuthClientList() throws IOException {
		return getOAuthClientList(null);
	}
	
	public List<OAuthClient> getOAuthClientList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/oauth/clients", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getOAuthClientList", res, OAuthClient.class, range);
	}
	
	public OAuthClient getOAuthClient(String id) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/oauth/clients/" + id);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getOAuthClient", res, OAuthClient.class).get(0);
	}
	
	public OAuthClient addOAuthClient(String name, String redirectUrl) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/oauth/clients");
		request.setParameter("name", name);
		request.setParameter("redirect_uri", redirectUrl);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addOAuthClient", res, OAuthClient.class).get(0);
	}
	
	public OAuthClient updateOAuthClient(OAuthClient obj) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/oauth/clients/" + obj.getId());
		if (obj.getName() != null) {
			request.setParameter("name", obj.getName());
		}
		if (obj.getRedirectUri() != null) {
			request.setParameter("redirect_uri", obj.getRedirectUri());
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateOAuthClient", res, OAuthClient.class).get(0);
	}
	
	public OAuthClient deleteOAuthClient(String id) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/oauth/clients/" + id);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteOAuthClient", res, OAuthClient.class).get(0);
	}
	
	//Key
	public List<Key> getKeyList() throws IOException {
		return getKeyList(null);
	}
	
	public List<Key> getKeyList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/account/keys", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getKeyList", res, Key.class, range);
	}
	
	public Key getKey(String id) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/account/keys/" + id);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getKey", res, Key.class).get(0);
	}
	
	public Key addKey(String publicKey) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/account/keys");
		request.setParameter("public_key", publicKey);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addKey", res, Key.class).get(0);
	}
	
	public Key deleteKey(String id) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/account/keys/" + id);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteKey", res, Key.class).get(0);
	}
	
	//AppTransfer
	public AppTransfer createAppTransfer(String appName, String recipientIdOrMail) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/account/app-transfers");
		request.setParameter("app.name", appName);
		if (recipientIdOrMail.indexOf('@') != -1) {
			request.setParameter("recipient.email", recipientIdOrMail);
		} else {
			request.setParameter("recipient.id", recipientIdOrMail);
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("createAppTransfer", res, AppTransfer.class).get(0);
	}
	
	public List<AppTransfer> getAppTransferList() throws IOException {
		return getAppTransferList(null);
	}
	
	public List<AppTransfer> getAppTransferList(Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/account/app-transfers", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAppTransferList", res, AppTransfer.class, range);
	}
	
	public AppTransfer getAppTransfer(String id) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/account/app-transfers/" + id);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getAppTransfer", res, AppTransfer.class).get(0);
	}
	
	public AppTransfer updateAppTransfer(String id, AppTransfer.State state) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/account/app-transfers/" + id);
		request.setParameter("state", state.toString());
		HttpResponse res = getTransport().execute(request);
		return handleResponse("updateAppTransfer", res, AppTransfer.class).get(0);
	}
	
	public AppTransfer deleteAppTransfer(String id) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/account/app-transfers/" + id);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteAppTransfer", res, AppTransfer.class).get(0);
	}
	
	//LogSession
	public LogSession createLogSession(String appName) throws IOException {
		return createLogSession(appName, new LogSession());
	}
	
	public LogSession createLogSession(String appName, LogSession option) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/log-sessions");
		if (option.getDyno() != null) {
			request.setParameter("dyno", option.getDyno());
		}
		if (option.getLines() > 0) {
			request.setParameter("lines", option.getLines());
		}
		if (option.getSource() != null) {
			request.setParameter("source", option.getSource());
		}
		if (option.isTail()) {
			request.setParameter("tail", "true");
		}
		HttpResponse res = getTransport().execute(request);
		return handleResponse("createLogSession", res, LogSession.class).get(0);
	}
	
	//SSLEndpoint
	public List<SSLEndpoint> getSSLEndpointList(String appName) throws IOException {
		return getSSLEndpointList(appName, null);
	}
	
	public List<SSLEndpoint> getSSLEndpointList(String appName, Range range) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/ssl-endpoints", range);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getSSLEndpointList", res, SSLEndpoint.class, range);
	}
	
	public SSLEndpoint getSSLEndpoint(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.GET, "/apps/" + appName + "/ssl-endpoints/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("getSSLEndpoint", res, SSLEndpoint.class).get(0);
	}
	
	public SSLEndpoint addSSLEndpoint(String appName, String crt, String key) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.POST, "/apps/" + appName + "/ssl-endpoints");
		request.setParameter("certificate_chain", crt);
		request.setParameter("private_key", key);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addSSLEndpoint", res, SSLEndpoint.class).get(0);
	}
	
	public SSLEndpoint updateSSLEndpoint(String appName, String idOrName, String crt, String key, boolean rollback) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.PATCH, "/apps/" + appName + "/ssl-endpoints/" + idOrName);
		if (crt != null) {
			request.setParameter("certificate_chain", crt);
		}
		if (key != null) {
			request.setParameter("private_key", key);
		}
		request.setParameter("rollback", rollback);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("addSSLEndpoint", res, SSLEndpoint.class).get(0);
	}
	
	public SSLEndpoint deleteSSLEndpoint(String appName, String idOrName) throws IOException {
		HttpRequest request = buildRequest(HttpRequest.Method.DELETE, "/apps/" + appName + "/ssl-endpoints/" + idOrName);
		HttpResponse res = getTransport().execute(request);
		return handleResponse("deleteSSLEndpoint", res, SSLEndpoint.class).get(0);
	}
}
