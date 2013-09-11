package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Dyno extends AbstractModel {
	
	public Dyno() {
		super();
	}
	
	public Dyno(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public String getName() { return getAsString("name");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getCommand() { return getAsString("command");}
	public String getType() { return getAsString("type");}
	
	public int getSize() { return getAsInt("size");}
	
	public String getReleaseId() { return getAsString("release.id");}
	public int getReleaseVersion() { return getAsInt("release.version");}
	
	public String getState() { return getAsString("state");}
	
	public boolean isAttach() { return getAsBoolean("attach");}
	public String getAttachUrl() { return getAsString("attach_url");}
}
