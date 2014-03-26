package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class Slug extends BasicModel {
	
	public Slug() {
		super();
	}
	
	public Slug(Map<String, Object> map) {
		super(map);
	}
	
	public String getBlobUrl() { return getAsString("blob.url");}
	public String getBlobMethod() { return getAsString("blob.method");}
	
	/** @deprecated Use getBlobUrl */
	public String getBlobGet() { return getBlobUrl();}

	/** @deprecated Use getBlobUrl */
	public String getBlobPut() { return getBlobUrl();}

	public String getCommit() { return getAsString("commit");}

	public Map<String, String> getProcessTypes() { 
		return (Map<String, String>)get("process_types");
	}

	public String getProcessType(String name) {
		Map<String, String> map = getProcessTypes();
		return map == null ? null : map.get(name);
	}
	
}

