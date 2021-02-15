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

import life.genny.models.GennyToken;
import life.genny.notifications.EmailHelper;
import life.genny.notifications.SmsHelper;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;

public class MessageUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static void sendMessage( String code, String name, String tokenString, QBaseMSGMessageType[] qBaseMSGMessageType, String... recipients ) {
        // BaseEntity baseEntity = beUtils.getBaseEntityByCode("JNL_488F4EC2-8731-4E30-9198-1821EF0914EB20200914");
		GennyToken gennyToken = new GennyToken(tokenString);

		BaseEntity baseEntity = VertxUtils.readFromDDT(gennyToken.getRealm(),code, tokenString);

		// Map base entity to ContextList
		ContextList contextList = new ContextList();

		baseEntity.getBaseEntityAttributes().forEach(atrribute -> {
			contextList.addContext(atrribute);
		});

		contextList.addContext("URL_LINK", "https://iamalinkurl.com/page=7687d6sf87as6df87as6df"); // note that this is
																									// a direct string
																									// and not an
																									// indirect value
		contextList.addContext("SENDER", "[\"PER_HGJTYGJHHGJHG76876\"]"); // note this could be the
																			// userToken.getUserCode , could be
																			// stephanie..

		try {
//			String recipient = contextList.getEntityAttribute("PER_HGFHGFHGFHGFHJF");
//			String code = "MSG_INTERNSHIP_RECOMMENDATION";
//			String name = "Internship Recommendation";
//			QBaseMSGMessageType[] qBaseMSGMessageType = new QBaseMSGMessageType[] { QBaseMSGMessageType.EMAIL,
//					QBaseMSGMessageType.SMS };
			//VertxUtils.sendMessage(code, name, contextList, qBaseMSGMessageType, recipient); // Note that by putting the
																								// recipient code ast we
																								// can do a String... in
			 																					// the function
			if(qBaseMSGMessageType == null) {
				throw new Exception("qBaseMSGMessageType is empty or null");
			}
			
 			Arrays.asList(recipients).forEach(recipient-> {
				Arrays.asList(qBaseMSGMessageType).forEach(type ->{
					switch (type) {
					case SMS:
						final SmsHelper smsHelper = new SmsHelper();
						smsHelper.deliverSmsMsg(recipient,"msgBody");
						break;
					case SENDGRID:
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
						break;
					case EMAIL:
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
						break;
					}
					
					
				});
			});
			
			
			
		} catch (Exception messageException) {
			log.error(messageException.toString());
		}
		
	}

}
