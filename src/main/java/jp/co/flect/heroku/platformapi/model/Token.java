package jp.co.flect.heroku.platformapi.model;

import java.util.Map;

public class Token extends AbstractModel {
	
	public Token(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public String getToken() { return getAsString("token");}
	public String getCode() { return getAsString("code");}
	public int getExpiresIn() { return getAsInt("expires_in");}
}
