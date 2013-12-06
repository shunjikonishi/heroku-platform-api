package jp.co.flect.heroku.platformapi.model;

public enum Scope {
	Global("global"),
	Identity("identity"),
	Read("read"),
	Write("write"),
	ReadProtected("read-protected"),
	WriteProtected("write-protected")
	;
	
	public static Scope fromString(String s) {
		for (Scope scope : Scope.values()) {
			if (scope.value.equals(s)) {
				return scope;
			}
		}
		throw new IllegalArgumentException(s);
	}
	
	private String value;
	
	private Scope(String value) {
		this.value = value;
	}
	
	public String toString() { return this.value;}
}

