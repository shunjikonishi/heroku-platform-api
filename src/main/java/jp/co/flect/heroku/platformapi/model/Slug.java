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
	
	public String getBlobGet() { return getAsString("blob.get");}
	public String getBlobPut() { return getAsString("blob.put");}

	public String getCommit() { return getAsString("commit");}

	public Map<String, String> getProcessTypes() { 
		return (Map<String, String>)get("process_types");
	}

	public String getProcessType(String name) {
		Map<String, String> map = getProcessTypes();
		return map == null ? null : map.get(name);
	}
	
}

