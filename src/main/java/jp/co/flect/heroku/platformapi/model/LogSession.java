package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class LogSession extends BasicModel {
	
	public LogSession() {
		super();
	}
	
	public LogSession(Map<String, Object> map) {
		super(map);
	}
	
	public String getDyno() { return getAsString("dyno");}
	public void setDyno(String s) { set("dyno", s);}
	
	public String getSource() { return getAsString("source");}
	public void setSource(String s) { set("source", s);}
	
	public int getLines() { return getAsInt("lines");}
	public void setLines(int n) { set("lines", n);}
	
	public String getLogplexUrl() { return getAsString("logplex_url");}
	
	public boolean isTail() { return getAsBoolean("tail");}
	public void setTail(boolean b) { set("tail", b);}
	
}

