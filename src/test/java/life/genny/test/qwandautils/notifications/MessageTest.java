package life.genny.test.qwandautils.notifications;

import java.io.IOException;

import org.junit.Test;

import life.genny.eventbus.MockCache;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.notifications.v2.ContextList;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;

public class MessageTest {
	
	@Test
	public void  message_sendWithValidContext_returnSuccessBaseEntity() throws IOException {		
		VertxUtils.cacheInterface =  new MockCache();
		
		String clientId = System.getenv("CLIENT_ID"); 
		String secret = System.getenv("CLIENT_SECRET");
		String username = "test1234@gmail.com";
		String password = System.getenv("USER_PASSWORD");
		String realm = System.getenv("REALM"); ;
		String keycloakUrl = "https://keycloak.gada.io";

		String tokenString = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password).getString("access_token");
		
		GennyToken gennyToken = new GennyToken(tokenString);
	
		String code = System.getenv("BASE_ENTITY_CODE");;
		
		BaseEntityUtils be = new BaseEntityUtils(gennyToken);
		BaseEntity baseEntity = be.getBaseEntityByCode(code);
		
		// Map base entity to ContextList
		ContextList contextList = new ContextList();

		baseEntity.getBaseEntityAttributes().forEach(atrribute -> {
			contextList.addContext(atrribute);
 		});
		contextList.addContext("URL_LINK", "https://iamalinkurl.com/page=7687d6sf87as6df87as6df");  
		contextList.addContext("SENDER", "[\"PER_HGJTYGJHHGJHG76876\"]");
		contextList.print();
	}

}
