package life.genny.notifications;

import static java.lang.System.getProperties;
import static javax.mail.Message.RecipientType.TO;
import static javax.mail.Session.getDefaultInstance;

import java.util.Base64;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;

import io.vertx.core.json.JsonObject;

public class EmailHelper extends NotificationHelper {

  public EmailHelper() {
    super("PRI_EMAIL");
  }

  private Properties mailServerProperties;
  private Session getMailSession;
  private MimeMessage generateMailMessage;

  /*
   * This function will send email by using JAVA Transport Class
   *
   */
  public void deliverEmailMsg(String recipient, String emailBody)
      throws AddressException, MessagingException {

    if (StringUtils.isBlank(recipient)) {
      String msg = "Recipient is Blank";
      log.error(msg);
      throw new MessagingException(msg);
    }

    mailServerProperties = getProperties();
    mailServerProperties.put("mail.smtp.port", System.getenv("EMAIL_SMTP_PORT"));
    mailServerProperties.put("mail.smtp.auth", System.getenv("EMAIL_SMTP_AUTH"));
    mailServerProperties.put("mail.smtp.starttls.enable", System.getenv("EMAIL_SMTP_STARTTLS"));
    getMailSession = getDefaultInstance(mailServerProperties, null);
    generateMailMessage = new MimeMessage(getMailSession);
    generateMailMessage.addRecipient(TO, new InternetAddress(recipient));
    //      generateMailMessage.addRecipient(CC, new InternetAddress("email@emailaddress.email"));
    generateMailMessage.setSubject("Greetings from Outcome.life");

    /*
     * Construct email Body
     */
    emailBody =
        "Test email from Project Genny<br><br>" + emailBody + "<br><br> Regards, <br>Project Genny";
    generateMailMessage.setContent(emailBody, "text/html");

    // Enter your correct gmail UserID and Password
    // if you have 2FA enabled then provide App Specific Password
    Transport transport = getMailSession.getTransport("smtp");
    transport.connect(
        System.getenv("EMAIL_SMTP_HOST"),
        System.getenv("EMAIL_SMTP_USER"),
        System.getenv("EMAIL_SMTP_PASS"));
    transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
    log.info("-------------EMAIL SENT---------------------");
    log.info(System.getenv("EMAIL_SMTP_HOST"));
    log.info("-------------EMAIL RECIPIENT---------------------");
    log.info(generateMailMessage.getAllRecipients());
    log.info("-------------EMAIL BODY---------------------");
    log.info(generateMailMessage);

    transport.close();
  }

  public String getUnsubscribeLinkForEmailTemplate(String host, String templateCode) {

    JsonObject data = new JsonObject();
    data.put("loading", "Loading...");
    data.put("evt_type", "REDIRECT_EVENT");
    data.put("evt_code", "REDIRECT_UNSUBSCRIBE_MAIL_LIST");

    JsonObject dataObj = new JsonObject();
    dataObj.put("code", "REDIRECT_UNSUBSCRIBE_MAIL_LIST");
    dataObj.put("value", templateCode);

    data.put("data", dataObj);
    String redirectUrl = this.generateRedirectUrl(host, data);
    return redirectUrl;
  }

  public String generateRedirectUrl(String host, JsonObject data) {

    /* we stringify the json object */
    try {
      if (data != null) {
        /* we encode it for URL schema */
        String base64 = Base64.getEncoder().encodeToString(data.toString().getBytes());
        return host + "?state=" + base64;
      }
    } catch (Exception e) {
    }

    return null;
  }
}
