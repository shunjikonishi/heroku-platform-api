package jp.co.flect.heroku.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jp.co.flect.heroku.json.JsonUtils;

import play.libs.WS;
import play.libs.WS.WSRequest;

public class Play1Transport implements Transport {
	
	private int soTimeout = 0;
	
	public HttpResponse execute(HttpRequest request) throws IOException {
		WSRequest ws = WS.url(request.getUrl());
		if (this.soTimeout != 0) {
			ws = ws.timeout(this.soTimeout + "s");
		}
		for (Map.Entry<String, String[]> entry : request.getHeaders().entrySet()) {
			String key = entry.getKey();
			for (String s : entry.getValue()) {
				ws = ws.setHeader(key, s);
			}
		}
		if (request.getParameters().size() > 0) {
			if ("application/json".equals(request.getFirstHeader("content-type"))) {
				String json = JsonUtils.serialize(request.getParameters());
				ws = ws.body(json);
			} else {
				for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (value instanceof String) {
						ws = ws.setParameter(key, value.toString());
					} else if (value instanceof String[]) {
						key += "[]";
						for (String s : (String[])value) {
							ws = ws.setParameter(key, s);
						}
					}
				}
			}
		}
		WS.HttpResponse res = null;
		switch (request.getMethod()) {
			case POST:
				res = ws.post();
				break;
			case GET:
				res = ws.get();
				break;
			case PATCH:
				ws = ws.setHeader("X-Http-Method-Override", "PATCH");
				res = ws.post();
				break;
			case PUT:
				res = ws.put();
				break;
			case DELETE:
				res = ws.delete();
				break;
		} 
		return new HttpResponseWrapper(res);
	}
	
	public ProxyInfo getProxyInfo() { return null;}
	public void setProxyInfo(ProxyInfo proxy) { throw new UnsupportedOperationException();}
	
	public int getSoTimeout() { return this.soTimeout;}
	public void setSoTimeout(int n) { this.soTimeout = n;}
	
	public int getConnectionTimeout() { return 0;}
	public void setConnectionTimeout(int n) { throw new UnsupportedOperationException();}
	
	public static class Factory extends TransportFactory {
		
		public Transport create() { return new Play1Transport();}
		
	}
	
	private static class HttpResponseWrapper implements HttpResponse {
		
		private WS.HttpResponse res;
		private String body;
		
		public HttpResponseWrapper(WS.HttpResponse res) {
			this.res = res;
		}
		
		public int getStatus() { 
		return res.getStatus();
		}
		
		public String getContentType() {
			return res.getContentType();
		}
		
		public String getBody() {
			if (this.body == null) {
				this.body = res.getString();
			}
			return this.body;
		}
		
		public String getHeader(String name) {
			return res.getHeader(name);
		}
		
	}
}

