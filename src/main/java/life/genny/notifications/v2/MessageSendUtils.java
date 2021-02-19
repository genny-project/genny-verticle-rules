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
import life.genny.notifications.v2.model.EmailMessagePayload;
import life.genny.notifications.v2.model.FirebaseMessagePayload;
import life.genny.notifications.v2.model.MessagePayLoad;
import life.genny.notifications.v2.model.QBaseMSGMessageTypeV2;
import life.genny.notifications.v2.model.SMSMessagePayload;
import life.genny.notifications.v2.model.SendGridMessagePayload;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;
import life.genny.qwandautils.FirebaseUtils;
 
public class MessageSendUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static void sendMessage( String code, String name, String tokenString, GennyCacheInterface cacheInterface, MessagePayLoad... messagePayLoads) throws Exception {
        
		VertxUtils.cacheInterface = (cacheInterface == null)?new MockCache(): cacheInterface;  
		
		GennyToken gennyToken = new GennyToken(tokenString);
		BaseEntityUtils be = new BaseEntityUtils(gennyToken);
		BaseEntity baseEntity = be.getBaseEntityByCode(code);
		 
		
		// Map base entity to ContextList
		ContextList contextList = new ContextList();
		List<Context> contexts = new LinkedList<>();

		contexts.add(new Context(ContextType.THEME, baseEntity));
		contextList.setContextList(contexts);
		
		Arrays
		  .asList(messagePayLoads)
		  .forEach(messagePayLoad -> {
			  QBaseMSGMessageTypeV2 type = messagePayLoad.getMessageType();
			  switch (type) {
				case SMS:
					sendSms( contextList, messagePayLoad);
					break;
				case SENDGRID:
					sendSendGrid(contextList,be, messagePayLoad);
					break;
				case EMAIL:
					sendEmail(  contextList,   messagePayLoad);
					break;
				case FIREBASE:
					sendFirebase(  contextList,  messagePayLoad);
					break;
			default: 
				  log.error(String.format("%s not implemented yet! ", type.name()));
				break;
				}
		  });
		
		//contexts.add(new Context(  new Bas));
 //
//		list.addContext("INTERN","[\"PER_HGFHGFHGFHGFHJF\"]");
//		list.addContext("INTERNSHIP","[\"BEG_HGLJKHCLKVJOII87687CV6B87\"]");
//		list.addContext("COMPANY","[\"CPY_EVILCORP\"]");
//		list.addContext("URL_LINK","https://iamalinkurl.com/page=7687d6sf87as6df87as6df");   // note that this is a direct string and not an indirect value
//		list.addContext("SENDER","[\"PER_HGJTYGJHHGJHG76876\"]");    // note this could be the userToken.getUserCode , could be stephanie..
		
	}
	
	private static void sendFirebase(ContextList contextList,  MessagePayLoad messagePayLoad) {
		FirebaseMessagePayload firebaseMessagePayload = (FirebaseMessagePayload) messagePayLoad;
		String recipient = firebaseMessagePayload.getRecipient();
		String body = firebaseMessagePayload.getBody();
		String title = firebaseMessagePayload.getTitle();
		String apiKey = firebaseMessagePayload.getApiKey();
	 	FirebaseUtils.send(recipient, title, body, apiKey);
		
	}

	private static void sendSms(ContextList contextList, MessagePayLoad messagePayLoad) {
		SMSMessagePayload smsMessagePayload = (SMSMessagePayload) messagePayLoad;
		String recipient = smsMessagePayload.getRecipient();
		String body = smsMessagePayload.getBody();
		final SmsHelper smsHelper = new SmsHelper();
		smsHelper.deliverSmsMsg(recipient,body);
	}
	
	private static void sendSendGrid(ContextList contextList, BaseEntityUtils beUtils, MessagePayLoad messagePayLoad ) {
		SendGridMessagePayload sendGridMessagePayload = (SendGridMessagePayload)messagePayLoad;
		String subject = sendGridMessagePayload.getSubject();
		String template_id = sendGridMessagePayload.getTemplateId();
		String recipient = sendGridMessagePayload.getRecipient();
		List<String> ccList = sendGridMessagePayload.getCcList();
		List<String> bccList = sendGridMessagePayload.getBccList();
		
 		HashMap<String, String> templateData =  sendGridMessagePayload.getTemplateData();
 		
		try {
			EmailHelper.sendGrid(beUtils, recipient, ccList, bccList, subject, template_id, templateData);
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	private static void sendEmail(ContextList contextList, MessagePayLoad messagePayLoad) {
		EmailMessagePayload emailMessagePayload = (EmailMessagePayload) messagePayLoad;
		String recipient = emailMessagePayload.getRecipient();
		String body = emailMessagePayload.getBody();
		EmailHelper emailHelper = new EmailHelper();
		try {
			emailHelper.deliverEmailMsg(recipient, body);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		}
	}

}
