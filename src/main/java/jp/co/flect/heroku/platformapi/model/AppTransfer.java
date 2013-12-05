package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class AppTransfer extends BasicModel {
	
	public enum State {
		Pending,
		Declined,
		Accepted
		;
		
		public String toString() { return super.toString().toLowerCase();}
	}
	
	public static State strToState(String str) {
		for (State s : State.values()) {
			if (s.toString().equals(str)) {
				return s;
			}
		}
		return null;
	}
	
	public AppTransfer() {
		super();
	}
	
	public AppTransfer(Map<String, Object> map) {
		super(map);
	}
	
	public String getAppId() { return getAsString("app.id");}
	public String getAppName() { return getAsString("app.name");}
	
	public String getOwnerEmail() { return getAsString("owner.email");}
	public String getOwnerId() { return getAsString("owner.id");}
	
	public String getRecipientEmail() { return getAsString("recipient.email");}
	public String getRecipientId() { return getAsString("recipient.id");}
	
	public State getState() { return strToState(getAsString("state"));}
}

