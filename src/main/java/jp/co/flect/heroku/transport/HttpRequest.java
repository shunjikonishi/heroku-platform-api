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
	
	private Map<String, Object> params = new HashMap<String, Object>();
	private Map<String, String[]> headers = new HashMap<String, String[]>();
	
	public HttpRequest(Method method, String url) {
		this.method = method;
		this.url = url;
	}
	
	public Method getMethod() { return this.method;}
	public String getUrl() { return this.url;}
	
	public Map<String, Object> getParameters() { return this.params;}
	public Map<String, String[]> getHeaders() { return this.headers;}
	
	public void setParameter(String name, String... values) {
		doSetParameter(this.params, name, values);
	}
	
	private void doSetParameter(Map<String, Object> map, String name, String... values) {
		int idx = name.indexOf(".");
		if (idx == -1) {
			if (values.length == 1) {
				map.put(name, values[0]);
			} else {
				map.put(name, values);
			}
		} else {
			String prefix = name.substring(0, idx);
			String suffix = name.substring(idx+1);
			Map<String, Object> childMap = (Map<String, Object>)map.get(prefix);
			if (childMap == null) {
				childMap = new HashMap<String, Object>();
				map.put(prefix, childMap);
			}
			doSetParameter(childMap, suffix, values);
		}
	}
	
	public void setHeader(String name, String... values) {
		this.headers.put(name, values);
	}
	
	public String[] getHeader(String name) { 
		return this.headers.get(name);
	}
	
	public String getFirstHeader(String name) {
		String[] values = getHeader(name);
		return values == null || values.length == 0 ? null : values[0];
	}
	
}

