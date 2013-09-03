package jp.co.flect.heroku.platformapi;

import jp.co.flect.heroku.HerokuException;

public class PlatformApiException extends HerokuException {
	
	private int status;
	private String id;
	
	public PlatformApiException(int status, Error e) {
		super(e.getMessage());
		this.status = status;
		this.id = e.getId();
	}
	
	public int getStatus() { return this.status;}
	public String getId() { return this.id;}
	
	public static class Error {
		
		private String id;
		private String message;
		
		public String getId() { return this.id;}
		public String getMessage() { return this.message;}
	}
}

