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
    this.setEMAIL_SMTP_HOST(System.getenv("EMAIL_SMTP_HOST"));
    this.setEMAIL_SMTP_USER(System.getenv("EMAIL_SMTP_USER"));
    this.setEMAIL_SMTP_PASS(System.getenv("EMAIL_SMTP_PASS"));
    this.setEMAIL_SMTP_PORT(System.getenv("EMAIL_SMTP_PORT"));
    this.setEMAIL_SMTP_AUTH(System.getenv("EMAIL_SMTP_AUTH"));
    this.setEMAIL_SMTP_STARTTLS(System.getenv("EMAIL_SMTP_STARTTLS"));
  }

  private Properties mailServerProperties;
  private Session getMailSession;
  private MimeMessage generateMailMessage;

  private static String EMAIL_SMTP_HOST;
  private static String EMAIL_SMTP_USER;
  private static String EMAIL_SMTP_PASS;
  private static String EMAIL_SMTP_PORT;
  private static String EMAIL_SMTP_AUTH;
  private static String EMAIL_SMTP_STARTTLS;

  public String getEMAIL_SMTP_HOST() {
    return EMAIL_SMTP_HOST;
  }

  public void setEMAIL_SMTP_HOST(String eMAIL_SMTP_HOST) {
    EMAIL_SMTP_HOST = eMAIL_SMTP_HOST;
  }

  public String getEMAIL_SMTP_USER() {
    return EMAIL_SMTP_USER;
  }

  public void setEMAIL_SMTP_USER(String eMAIL_SMTP_USER) {
    EMAIL_SMTP_USER = eMAIL_SMTP_USER;
  }

  public String getEMAIL_SMTP_PASS() {
    return EMAIL_SMTP_PASS;
  }

  public void setEMAIL_SMTP_PASS(String eMAIL_SMTP_PASS) {
    EMAIL_SMTP_PASS = eMAIL_SMTP_PASS;
  }

  public String getEMAIL_SMTP_PORT() {
    return EMAIL_SMTP_PORT;
  }

  public void setEMAIL_SMTP_PORT(String eMAIL_SMTP_PORT) {
    EMAIL_SMTP_PORT = eMAIL_SMTP_PORT;
  }

  public String getEMAIL_SMTP_AUTH() {
    return EMAIL_SMTP_AUTH;
  }

  public void setEMAIL_SMTP_AUTH(String eMAIL_SMTP_AUTH) {
    EMAIL_SMTP_AUTH = eMAIL_SMTP_AUTH;
  }

  public String getEMAIL_SMTP_STARTTLS() {
    return EMAIL_SMTP_STARTTLS;
  }

  public void setEMAIL_SMTP_STARTTLS(String eMAIL_SMTP_STARTTLS) {
    EMAIL_SMTP_STARTTLS = eMAIL_SMTP_STARTTLS;
  }

  /*
   * This function will send email by using JAVA Transport Class
   *
   * @param recipient Email recipient
   * @param emailBody Email body text
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
    mailServerProperties.put("mail.smtp.port", getEMAIL_SMTP_PORT());
    mailServerProperties.put("mail.smtp.auth", getEMAIL_SMTP_AUTH());
    mailServerProperties.put("mail.smtp.starttls.enable", getEMAIL_SMTP_STARTTLS());
    getMailSession = getDefaultInstance(mailServerProperties, null);
    generateMailMessage = new MimeMessage(getMailSession);
    generateMailMessage.addRecipient(TO, new InternetAddress(recipient));
    //      generateMailMessage.addRecipient(CC, new InternetAddress("email@emailaddress.email"));
    generateMailMessage.setSubject(this.getEmailSubject());

    /*
     * Construct email Body
     */
    generateMailMessage.setContent(emailBody, "text/html");

    // Enter your correct gmail UserID and Password
    // if you have 2FA enabled then provide App Specific Password
    Transport transport = getMailSession.getTransport("smtp");
    transport.connect(
        this.getEMAIL_SMTP_HOST(), this.getEMAIL_SMTP_USER(), this.getEMAIL_SMTP_PASS());
    transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
    log.info("-------------EMAIL SENT---------------------");
    log.info(getEMAIL_SMTP_HOST());
    log.info("-------------EMAIL RECIPIENT---------------------");
    log.info(generateMailMessage.getAllRecipients());
    log.info("-------------EMAIL BODY---------------------");
    log.info(generateMailMessage);
    log.info("-------------EMAIL PORT---------------------");
    log.info(getEMAIL_SMTP_PORT());

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