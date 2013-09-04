package jp.co.flect.heroku.platformapi.model;

import java.util.Map;

public class RateLimits extends AbstractModel{
	
	public RateLimits() {
		super();
	}
	
	public RateLimits(Map<String, Object> map) {
		super(map);
	}
	
	public int getRemaining() { return getAsInt("remaining");}
	
}
