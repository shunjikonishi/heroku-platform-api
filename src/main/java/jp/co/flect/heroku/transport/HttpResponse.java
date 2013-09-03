package jp.co.flect.heroku.transport;

public interface HttpResponse {
	
	public int getStatus();
	public String getContentType();
	public String getBody();
	public String getHeader(String name);
	
}

