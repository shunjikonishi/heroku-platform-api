package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class OAuthClient extends BasicModel {
	
	public OAuthClient() {
		super();
	}
	
	public OAuthClient(Map<String, Object> map) {
		super(map);
	}
	
	public String getName() { return getAsString("name");}
	
	public void setId(String s) { set("id", s);}
	public void setName(String s) { set("name", s);}
	public void setRedirectUri(String s) { set("redirect_uri", s);}
	
	public String getRedirectUri() { return getAsString("redirect_uri");}
	
	public String getSecret() { return getAsString("secret");}
	public boolean isTrusted() { return getAsBoolean("trusted");}
	
}
