package jp.co.flect.heroku.platformapi;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import jp.co.flect.heroku.HerokuException;
import jp.co.flect.heroku.json.JsonUtils;
import jp.co.flect.heroku.transport.Transport;
import jp.co.flect.heroku.transport.TransportFactory;
import jp.co.flect.heroku.transport.HttpRequest;
import jp.co.flect.heroku.transport.HttpResponse;
import jp.co.flect.heroku.platformapi.model.Account;
import jp.co.flect.heroku.platformapi.model.RateLimits;

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
	
	private Account account = null;
	
	private PlatformApi() {
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
		return this.token_type + " " + this.access_token;
	}
	
	private HttpRequest buildRequest(HttpRequest.Method method, String path) {
		this.requestStart = System.currentTimeMillis();
		HttpRequest request = new HttpRequest(method, HOST_API + path);
		request.setHeader("Accept", "application/vnd.heroku+json; version=3");
		request.setHeader("Authorization", getAuthorization());
		return request;
	}
	
	private <T> T handleResponse(String name, HttpResponse res, Class<T> returnClass) throws IOException {
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
		
		if (status >= 200 && status < 300) {
			return JsonUtils.parse(body, returnClass);
		} else {
			if (body != null && body.indexOf("\"id\"") != -1 && body.indexOf("\"message\"") != -1) {
				PlatformApiException.Error e = JsonUtils.parse(body, PlatformApiException.Error.class);
				throw new PlatformApiException(status, e);
			} else {
				throw new IOException("status=" + status + ", body=" + body);
			}
		}
	}
	
	public Account getAccount() throws IOException {
		if (this.account != null) {
			return this.account;
		}
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account"));
		this.account = handleResponse("getAccount", res, Account.class);
		return this.account;
	}
	
	public int getRateLimits() throws IOException {
		HttpResponse res = getTransport().execute(buildRequest(HttpRequest.Method.GET, "/account/rate-limits"));
		return handleResponse("getRateLimits", res, RateLimits.class).getRemaining();
	}
	
}
