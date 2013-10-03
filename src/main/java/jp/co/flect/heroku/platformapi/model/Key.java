package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class Key extends BasicModel {
	
	public Key() {
		super();
	}
	
	public Key(Map<String, Object> map) {
		super(map);
	}
	
	public String getEmail() { return getAsString("email");}
	public String getFingerprint() { return getAsString("fingerprint");}
	public String getPublicKey() { return getAsString("public_key");}
	
}

