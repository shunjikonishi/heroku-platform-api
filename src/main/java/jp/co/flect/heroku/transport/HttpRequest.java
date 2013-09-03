package jp.co.flect.heroku.transport;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	
	public enum Method {
		GET,
		POST
	};
	
	private Method method;
	private String url;
	
	private Map<String, String[]> params = new HashMap<String, String[]>();
	private Map<String, String[]> headers = new HashMap<String, String[]>();
	
	public HttpRequest(Method method, String url) {
		this.method = method;
		this.url = url;
	}
	
	public Method getMethod() { return this.method;}
	public String getUrl() { return this.url;}
	
	public Map<String, String[]> getParameters() { return this.params;}
	public Map<String, String[]> getHeaders() { return this.headers;}
	
	public void setParameter(String name, String... values) {
		this.params.put(name, values);
	}
	
	public void setHeader(String name, String... values) {
		this.headers.put(name, values);
	}
	
}

