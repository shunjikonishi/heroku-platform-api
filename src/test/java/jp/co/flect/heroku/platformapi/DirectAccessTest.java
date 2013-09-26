package jp.co.flect.heroku.platformapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.BeforeClass;
import java.util.List;

import jp.co.flect.heroku.platformapi.model.App;

public class DirectAccessTest {
	
	public static final String USERNAME = System.getenv("HEROKU_USERNAME");
	public static final String PASSWORD = System.getenv("HEROKU_PASSWORD");
	
	@Test
	public void app() throws Exception {
		System.out.println(USERNAME + ", " + PASSWORD);
		PlatformApi api = PlatformApi.fromPassword(USERNAME, PASSWORD);
		List<App> list = api.getAppList();
		assertTrue(list.size() > 0);
		
		App app = list.get(0);
		System.out.println("First app: " + app.getName());
	}
}
