package jp.co.flect.heroku;

import java.io.IOException;

public class HerokuException extends IOException {
	
	public HerokuException(String msg) {
		super(msg);
	}
}
