package jp.co.flect.heroku.transport;

public abstract class TransportFactory {
	
	private static TransportFactory DEFAULT_FACTORY;
	
	static {
		DEFAULT_FACTORY = new HttpClientTransport.Factory();
	}
	
	public static TransportFactory getDefaultTransportFactory() { return DEFAULT_FACTORY;}
	public static void setDefaultTransportFactory(TransportFactory tf) { DEFAULT_FACTORY = tf;}
	
	public static Transport createDefaultTransport() {
		return DEFAULT_FACTORY.create();
	}
	
	public abstract Transport create();
}

