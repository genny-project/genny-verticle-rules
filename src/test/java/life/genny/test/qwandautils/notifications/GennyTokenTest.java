package life.genny.test.qwandautils.notifications;

import org.junit.Test;

import life.genny.models.GennyToken;

public class GennyTokenTest {
	@Test
	public void gennyTokenTest()
	{
		String token = System.getenv("TOKEN");
		if (token != null) {
		GennyToken gToken = new GennyToken(token);
		
				System.out.println("token="+token);
		}
	}
}
