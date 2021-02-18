package life.genny.test.qwandautils.notifications;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import life.genny.eventbus.MockCache;
import life.genny.notifications.v2.MessageSendUtils;
import life.genny.notifications.v2.model.EmailMessagePayload;
import life.genny.notifications.v2.model.SMSMessagePayload;
import life.genny.notifications.v2.model.SendGridMessagePayload;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.KeycloakUtils;

public class MessageTest {
	
	private String tokenString;
	private GennyCacheInterface cacheInterface;
	private String code;
	private String name;
	@Before
	public void setup() throws IOException {
		// CONFIG ENV
//		BASE_ENTITY_CODE=PER_XXXXXXXX
//		CLIENT_ID=XXXXX
//		CLIENT_SECRET=XXXXX
//		REACT_APP_QWANDA_API_URL=https://internmatch-dev.gada.io
//		REALM=XXXXXXXX
//		USER_PASSWORD=XXXXX

		cacheInterface =  new MockCache();
		
		String clientId = System.getenv("CLIENT_ID"); 
		String secret = System.getenv("CLIENT_SECRET");
		String username = "test1234@gmail.com";
		String password = System.getenv("USER_PASSWORD");
		String realm = System.getenv("REALM"); ;
		String keycloakUrl = "https://keycloak.gada.io";
		name = "Internship Recommendation";
		
		tokenString = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password).getString("access_token");
		code = System.getenv("BASE_ENTITY_CODE");;

	}
	
	
	@Test
	public void  messageSMS_sendWithValidContext_returnSuccessBaseEntity() throws Exception {

		// CONFIG ENV
		// TWILIO_ACCOUNT_SID=XXXX
		// TWILIO_AUTH_TOKEN=XXXX
		// TWILIO_SENDER_MOBILE=XXXXX
		//TWILIO_RECIPIENT=<YOUR TEST MOBILE>
		
		SMSMessagePayload smsMessagePayload = new SMSMessagePayload();
		smsMessagePayload.setRecipient(System.getenv("TWILIO_RECIPIENT"));
		smsMessagePayload.setBody("this is from gada rx");
		MessageSendUtils.sendMessage(code, name, tokenString, cacheInterface, smsMessagePayload);

	}
	
	@Test
	public void  messageEmail_sendWithValidContext_returnSuccessBaseEntity() throws Exception {
		// CONFIG ENV
		// EMAIL_SMTP_AUTH=true
		// EMAIL_SMTP_HOST=smtp.gmail.com
		// EMAIL_SMTP_PASS=XXXXXXXX
		// EMAIL_SMTP_PORT=587
		// EMAIL_SMTP_STARTTLS=true
		// EMAIL_SMTP_USER=XXXXXXX@gmail.com
		
		EmailMessagePayload emailMessagePayload = new EmailMessagePayload();
		emailMessagePayload.setRecipient(System.getenv("SEND_GRID_RECIPIENT"));
		emailMessagePayload.setBody("Hi this is the test email");
		
		MessageSendUtils.sendMessage(code, name, tokenString, cacheInterface, emailMessagePayload);
	}
	
	@Test
	public void  messageSendGrid_sendWithValidContext_returnSuccessBaseEntity() throws Exception {
		// CONFIG ENV
		// SEND_GRID_RECIPIENT=XXXXXXX@hotmail.com
		// SEND_GRID_TEMPLATE_ID=XXXXXXX		
		SendGridMessagePayload sendGridMessagePayload = new SendGridMessagePayload();
		sendGridMessagePayload.setRecipient(System.getenv("SEND_GRID_RECIPIENT"));
		sendGridMessagePayload.setTemplateId(System.getenv("SEND_GRID_TEMPLATE_ID"));
		sendGridMessagePayload.setSubject("An intern has been shortlisted for your internship opportunity!");		
 
		sendGridMessagePayload.addTemplateData("hostCompanyName", "hostCompanyName");
		sendGridMessagePayload.addTemplateData("hostCompanyRepName", "hcRepName");
		sendGridMessagePayload.addTemplateData("internshipName", "title");
		sendGridMessagePayload.addTemplateData("internFullName", "name");
		sendGridMessagePayload.addTemplateData("url", "sampleurl");	
		
		MessageSendUtils.sendMessage(code, name, tokenString, cacheInterface, sendGridMessagePayload);
	}
	
	
	@Test
	public void  messageMutiple_sendWithValidContext_returnSuccessBaseEntity() throws Exception {
		//MessageSendUtils.sendMessage(code, name, tokenString, cacheInterface, messagePayLoads);
		
		EmailMessagePayload emailMessagePayload = new EmailMessagePayload();
		emailMessagePayload.setRecipient(System.getenv("SEND_GRID_RECIPIENT"));
		emailMessagePayload.setBody("Hi this is the test email Mutiple");
		
		SendGridMessagePayload sendGridMessagePayload = new SendGridMessagePayload();
		sendGridMessagePayload.setRecipient(System.getenv("SEND_GRID_RECIPIENT"));
		sendGridMessagePayload.setTemplateId(System.getenv("SEND_GRID_TEMPLATE_ID"));
		sendGridMessagePayload.setSubject("Mutiple An intern has been shortlisted for your internship opportunity!");		
 
		sendGridMessagePayload.addTemplateData("hostCompanyName", "hostCompanyName Mutiple");
		sendGridMessagePayload.addTemplateData("hostCompanyRepName", "hcRepName");
		sendGridMessagePayload.addTemplateData("internshipName", "title");
		sendGridMessagePayload.addTemplateData("internFullName", "name");
		sendGridMessagePayload.addTemplateData("url", "sampleurl");	
		
		MessageSendUtils.sendMessage(code, name, tokenString, cacheInterface, sendGridMessagePayload, emailMessagePayload);
	}
	 
}
