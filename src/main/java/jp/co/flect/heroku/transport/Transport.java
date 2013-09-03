package jp.co.flect.heroku.transport;

import java.io.File;
import java.io.IOException;
import jp.co.flect.heroku.HerokuException;

public interface Transport {
	
	public HttpResponse execute(HttpRequest request) throws IOException;
	
	public ProxyInfo getProxyInfo();
	public void setProxyInfo(ProxyInfo proxy);
}

