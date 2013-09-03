package jp.co.flect.heroku.platformapi.model;

import java.io.Serializable;

public class Account implements Serializable {
	
	private static final long serialVersionUID = -8066999666984167586L;
	
	private boolean allow_tracking;
	private boolean beta;
	private String created_at;
	private String email;
	private String id;
	private String last_login;
	private String updated_at;
	private boolean verified;
	
	public boolean allowTracking() { return this.allow_tracking;}
	public boolean isBeta() { return this.beta;}
	
	public String getCreatedAtAsString() { return this.created_at;}
	public String getLastLoginAsString() { return this.last_login;}
	public String getUpdatedAtAsString() { return this.updated_at;}
	
	public String getEmail() { return this.email;}
	public String getId() { return this.id;}
	
	public boolean isVerified() { return this.verified;}
	
}
