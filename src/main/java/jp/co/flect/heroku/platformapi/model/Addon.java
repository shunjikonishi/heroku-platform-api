package jp.co.flect.heroku.platformapi.model;

import java.io.Serializable;
import java.util.Map;

public class Addon implements Serializable {
	
//	private static final long serialVersionUID = 0L;
	
	private Map<String, Object> config;
	private String id;
	private String created_at;
	private String updated_at;
	private Plan plan;
	
	public String getCreatedAtAsString() { return this.created_at;}
	public String getUpdatedAtAsString() { return this.updated_at;}
	
	public String getId() { return this.id;}
	public Map<String, Object> getConfig() { return this.config;}
	
	public String getPlanId() { return this.plan == null ? null : this.plan.id;}
	public String getPlanName() { return this.plan == null ? null : this.plan.name;}
	
	public static class Plan {
		
		private String id;
		private String name;
		
		public String getId() { return this.id;}
		public String getName() { return this.name;}
		
	}
}

