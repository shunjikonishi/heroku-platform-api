package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class OAuthClient extends AbstractModel {
	
	public OAuthClient() {
		super();
	}
	
	public OAuthClient(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public String getName() { return getAsString("name");}
	
	public void setName(String s) { set("name", s);}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getRedirectUrl() { return getAsString("redirect_url");}
	public void setRedirctUrl(String s) { set("redirect_url", s);}
	
	public String getSeqret() { return getAsString("secret");}
	
}
