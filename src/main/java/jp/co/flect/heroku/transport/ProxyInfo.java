package jp.co.flect.heroku.transport;

public class ProxyInfo {
	
	private String host;
	private int port;
	private String username;
	private String password;
	
	public ProxyInfo(String host, int port) {
		this(host, port, null, null);
	}
	
	public ProxyInfo(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public String getHost() { return this.host;}
	public int getPort() { return this.port;}
	public String getUserName() { return this.username;}
	public String getPassword() { return this.password;}
	
}

