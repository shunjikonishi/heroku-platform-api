package jp.co.flect.heroku.transport;

public abstract class TransportFactory {
	
	private static TransportFactory DEFAULT_FACTORY;
	
	static {
		if (DEFAULT_FACTORY == null) {
			try {
				Class.forName("org.apache.http.impl.client.DefaultHttpClient");
				DEFAULT_FACTORY = new HttpClientTransport.Factory();
			} catch (ClassNotFoundException e) {
			}
		}
		if (DEFAULT_FACTORY == null) {
			try {
				Class.forName("play.libs.WS");
				DEFAULT_FACTORY = new Play1Transport.Factory();
			} catch (ClassNotFoundException e) {
			}
		}
	}
	
	public static TransportFactory getDefaultTransportFactory() { return DEFAULT_FACTORY;}
	public static void setDefaultTransportFactory(TransportFactory tf) { DEFAULT_FACTORY = tf;}
	
	public static Transport createDefaultTransport() {
		return DEFAULT_FACTORY.create();
	}
	
	public abstract Transport create();
}

