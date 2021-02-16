package life.genny.notifications.v2;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.Logger;

import life.genny.eventbus.MockCache;
import life.genny.models.GennyToken;
import life.genny.notifications.EmailHelper;
import life.genny.notifications.SmsHelper;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;

public class MessageUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static void sendMessage( String code, String name, String tokenString, QBaseMSGMessageType[] qBaseMSGMessageType, String... recipients ) throws IOException {
        
		
		VertxUtils.cacheInterface =  new MockCache();
		
		String clientId = "internmatch";
		String secret = System.getenv("CLIENT_SECRET");
		String username = "test1234@gmail.com";
		String password = System.getenv("USER_PASSWORD");
		String realm = "internmatch";
		String keycloakUrl = "https://keycloak.gada.io";

		tokenString = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password).getString("access_token");
		
		GennyToken gennyToken = new GennyToken(tokenString);
	
		String intermMatchCode = System.getenv("INTERNMATCH_CODE");;
		
		BaseEntityUtils be = new BaseEntityUtils(gennyToken);
		BaseEntity baseEntity = be.getBaseEntityByCode(intermMatchCode);
		
		// Map base entity to ContextList
		ContextList contextList = new ContextList();

		baseEntity.getBaseEntityAttributes().forEach(atrribute -> {
			contextList.addContext(atrribute);
 		});
		contextList.addContext("URL_LINK", "https://iamalinkurl.com/page=7687d6sf87as6df87as6df");  
		contextList.addContext("SENDER", "[\"PER_HGJTYGJHHGJHG76876\"]");
		contextList.print();

		try {

			if(qBaseMSGMessageType == null) {
				throw new Exception("qBaseMSGMessageType is empty or null");
			}
			
 			Arrays.asList(recipients).forEach(recipient-> {
				Arrays.asList(qBaseMSGMessageType).forEach(type ->{
					switch (type) {
					case SMS:
						sendSms( contextList,   recipient);
						break;
					case SENDGRID:
						sendSendGrid(contextList,gennyToken, recipient);
						break;
					case EMAIL:
						sendEmail(  contextList,   recipient);
						break;
					}
					
					
				});
			});
			
			
			
		} catch (Exception messageException) {
			log.error(messageException.toString());
		}
		
	}
	
	static void sendSms(ContextList contextList, String recipient) {
		final SmsHelper smsHelper = new SmsHelper();
		smsHelper.deliverSmsMsg(recipient,"msgBody");
	}
	
	static void sendSendGrid(ContextList contextList, GennyToken gennyToken, String recipient) {
		List<String> ccList = new LinkedList<>();
		List<String> bccList = new LinkedList<>();
		BaseEntityUtils beUtils = new BaseEntityUtils(gennyToken);
		String template_id ="";
		HashMap<String, String> templateData = new HashMap< >();
		try {
			EmailHelper.sendGrid(beUtils, recipient, ccList, bccList, "", template_id, templateData);
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	static void sendEmail(ContextList contextList, String recipient) {
		EmailHelper emailHelper = new EmailHelper();
		try {
			emailHelper.deliverEmailMsg(recipient, "emailBody");
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		}
	}

}
