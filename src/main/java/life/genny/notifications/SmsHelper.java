package life.genny.notifications;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import life.genny.qwandautils.GennySettings;

/*
 * Documentations: https://www.twilio.com/docs/libraries/java?utm_source=youtube&utm_medium=video&utm_campaign=youtube_send_sms
 *
 */
public class SmsHelper extends NotificationHelper {

  // Install the Java helper library from twilio.com/docs/java/install

  // Find your Account Sid and Token at twilio.com/console
  public static final String TWILIO_ACCOUNT_SID = GennySettings.twilioAccountSid;
  public static final String TWILIO_AUTH_TOKEN = GennySettings.twilioAuthToken;
  public static final String TWILIO_SENDER_MOBILE = GennySettings.twilioSenderMobile;

  public SmsHelper() {
    super("PRI_MOBILE");
  }

  /*
   * This will fire the message to the Agent
   */
  public void deliverSmsMsg(String recipient, String smsBody) {
    Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
    Message message =
        Message.creator(
                new com.twilio.type.PhoneNumber(recipient),
                new com.twilio.type.PhoneNumber(TWILIO_SENDER_MOBILE),
                smsBody)
            .create();

    log.info("-------------SMS SENT---------------------");
    log.info("By Twilio:" + message.getSid());
    log.info("-------------SMS RECIPIENT---------------------");
    log.info(recipient);
    log.info("-------------SMS BODY---------------------");
    log.info(smsBody);
  }
}
