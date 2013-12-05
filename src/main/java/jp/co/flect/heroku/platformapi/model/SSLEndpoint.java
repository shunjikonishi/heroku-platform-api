package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class SSLEndpoint extends BasicModel {
	
	public SSLEndpoint() {
		super();
	}
	
	public SSLEndpoint(Map<String, Object> map) {
		super(map);
	}
	
	public String getCertificateChain() { return getAsString("certificate_chain");}
	public String getCName() { return getAsString("cname");}
	public String getName() { return getAsString("name");}
	public String getPrivateKey() { return getAsString("private_key");}
	
	public boolean isRollback() { return getAsBoolean("rollback");}
}

