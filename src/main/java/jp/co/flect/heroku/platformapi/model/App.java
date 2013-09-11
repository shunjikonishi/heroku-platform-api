package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class App extends AbstractModel {
	
	public App() {
		super();
	}
	
	public App(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public String getName() { return getAsString("name");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	public Date getArchivedAt() { return getAsDate("archived_at");}
	public Date getReleasedAt() { return getAsDate("released_at");}
	
	public String getOwnerEmail() {
		Map<String, Object> map = (Map<String, Object>)get("owner");
		return map == null ? null : (String)map.get("email");
	}
	
	public String getOwnerId() {
		Map<String, Object> map = (Map<String, Object>)get("owner");
		return map == null ? null : (String)map.get("id");
	}
	
	public String getRegionId() {
		Map<String, Object> map = (Map<String, Object>)get("region");
		return map == null ? null : (String)map.get("id");
	}
	
	public String getRegionName() {
		Map<String, Object> map = (Map<String, Object>)get("region");
		return map == null ? null : (String)map.get("name");
	}
	
	public long getRepoSize() { return getAsLong("repo_size");}
	public long getSlugSize() { return getAsLong("slug_size");}
	
	public String getStack() { return getAsString("stack");}
	public String getWebUrl() { return getAsString("web_url");}
	public String getGitUrl() { return getAsString("git_url");}
	public String getBuildpackProvidedDescription() { return getAsString("buildpack_provided_description");}
	
	public boolean isMaintenance() { return getAsBoolean("maintenance");}
	
}
