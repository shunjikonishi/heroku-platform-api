package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class OAuthAuthorization extends BasicModel {
	
	public OAuthAuthorization() {
		super();
	}
	
	public OAuthAuthorization(Map<String, Object> map) {
		super();
		init(map);
	}
	
	@Override
	public void init(Map<String, Object> map) {
		Map<String, Object> child = (Map<String, Object>)map.get("access_token");
		if (child != null) {
			map.put("access_token", new Token(child));
		}
		child = (Map<String, Object>)map.get("client");
		if (child != null) {
			map.put("client", new OAuthClient(child));
		}
		child = (Map<String, Object>)map.get("grant");
		if (child != null) {
			map.put("grant", new Token(child));
		}
		child = (Map<String, Object>)map.get("refresh_token");
		if (child != null) {
			map.put("refresh_token", new Token(child));
		}
		List<String> strList = (List<String>)map.get("scope");
		if (strList != null) {
			List<Scope> scopeList = new ArrayList<Scope>();
			for (String s : strList) {
				scopeList.add(Scope.fromString(s));
			}
			map.put("scope", scopeList);
		}
		super.init(map);
	}
	
	public String getDescription() { return getAsString("description");}
	
	public OAuthClient getClient() { return (OAuthClient)get("client");}
	
	public Token getAccessToken() { return (Token)get("access_token");}
	public Token getRefreshToken() { return (Token)get("refresh_token");}
	public Token getGrant() { return (Token)get("grant");}
	
	public List<Scope> getScope() { return (List<Scope>)get("scope");}
}
