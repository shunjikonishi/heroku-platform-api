package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class OAuthToken extends BasicModel {
	
	public OAuthToken() {
		super();
	}
	
	public OAuthToken(Map<String, Object> map) {
		super();
		init(map);
	}
	
	@Override
	public void init(Map<String, Object> map) {
		Map<String, Object> child = (Map<String, Object>)map.get("access_token");
		if (child != null) {
			map.put("access_token", new Token(child));
		}
		child = (Map<String, Object>)map.get("refresh_token");
		if (child != null) {
			map.put("refresh_token", new Token(child));
		}
		super.init(map);
	}
	
	public String getAuthorizationId() { return getAsString("authorization.id");}
	public String getClientSecret() { return getAsString("client.secret");}
	
	public String getGrantCode() { return getAsString("grant.code");}
	public String getGrantType() { return getAsString("grant.type");}
	public String getSessionId() { return getAsString("session.id");}
	public String getUserId() { return getAsString("user.id");}
	
	public Token getAccessToken() { return (Token)get("access_token");}
	public Token getRefreshToken() { return (Token)get("refresh_token");}
	
}
