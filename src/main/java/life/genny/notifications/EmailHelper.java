package life.genny.notifications;

import static java.lang.System.getProperties;
import static javax.mail.Message.RecipientType.TO;
import static javax.mail.Session.getDefaultInstance;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
// import com.sendgrid.Content;
// import com.sendgrid.Email;
// import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
// import com.sendgrid.Personalization;
import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.GennySettings;

public class EmailHelper extends NotificationHelper {

  public EmailHelper() {
    super("PRI_EMAIL");
    this.setEMAIL_SMTP_HOST(GennySettings.emailSmtpHost);
    this.setEMAIL_SMTP_USER(GennySettings.emailSmtpUser);
    this.setEMAIL_SMTP_PASS(GennySettings.emailSmtpPassword);
    this.setEMAIL_SMTP_PORT(GennySettings.emailSmtpPort);
    this.setEMAIL_SMTP_AUTH(GennySettings.emailSmtpAuth);
    this.setEMAIL_SMTP_STARTTLS(GennySettings.emailSmtpStartTls);
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
   * 
   * @param emailBody Email body text
   *
   */
  public void deliverEmailMsg(String recipient, String emailBody) throws AddressException, MessagingException {

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
    // generateMailMessage.addRecipient(CC, new InternetAddress("email@emailaddress.email"));
    generateMailMessage.setSubject(this.getEmailSubject());

    /*
     * Construct email Body
     */
    generateMailMessage.setContent(emailBody, "text/html");

    // Enter your correct gmail UserID and Password
    // if you have 2FA enabled then provide App Specific Password
    Transport transport = getMailSession.getTransport("smtp");
    transport.connect(this.getEMAIL_SMTP_HOST(), this.getEMAIL_SMTP_USER(), this.getEMAIL_SMTP_PASS());
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

  public static void sendGrid(String recipient, String subject, String templateId) throws IOException {
    Email from = new Email(System.getenv("SENDGRID_EMAIL_SENDER"));
    Email to = new Email(recipient);

    SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

    Personalization personalization = new Personalization();

    personalization.addTo(to);
    personalization.addDynamicTemplateData("hostCompanyName", "Internmatch");
    personalization.addDynamicTemplateData("intern", "Sam");
    personalization.setSubject(subject);

    Mail mail = new Mail();
    mail.addPersonalization(personalization);
    mail.setTemplateId(templateId);
    mail.setFrom(from);

    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      Response response = sg.api(request);
      System.out.println(response.getStatusCode());
      System.out.println(response.getBody());
      System.out.println(response.getHeaders());

    } catch (IOException ex) {
      throw ex;
    }
  }
  
    
//    Email from = new Email(System.getenv("SENDGRID_EMAIL_SENDER"));
//    subject = "Sent from Grid";
//    Email to = new Email(recipient);
//    Content content = new Content("text/html", emailHtml);
//    Mail mail = new Mail(from, subject, to, content);
//
//    SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
//    Request request = new Request();
//    try {
//      request.setMethod(Method.POST);
//      request.setEndpoint("mail/send");
//      request.setBody(mail.build());
//      Response response = sg.api(request);
//    } catch (IOException ex) {
//      throw ex;
//    }
//    try {
//      request.setMethod(Method.POST);
//      request.setEndpoint("templates");
//      request.setBody("{\"name\":\"example_name\"}");
//      Response response = sg.api(request);
//    } catch (IOException ex) {
//      throw ex;
//    }
//  }
  // public static void sendGridTemplate(List<String> recipients, Map<String,String> dynamicData) {
  // Personalization personalization = new Personalization();
  // recipients.stream().map(Email::new).forEach(personalization::addTo);
  // dynamicData.entrySet().stream().forEach(d -> personalization.addDynamicTemplateData(, ));
  //
  //
  // }

 // public static void main(String... args) throws IOException {
 //   sendGrid("christopher.pyke@gada.io", "Recommended letter","d-4880601ff6b444dca5b49265e1bdd186");
 // }


	public static void sendGrid(String recipient, String subject, String templateId, String hardcodedTemplateData1, String hardcodedTemplateData2) throws IOException {
    Email from = new Email(System.getenv("SENDGRID_EMAIL_SENDER"));
    Email to = new Email(recipient);

    SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

    Personalization personalization = new Personalization();

    personalization.addTo(to);
    personalization.addDynamicTemplateData(hardcodedTemplateData1, hardcodedTemplateData2);
    personalization.setSubject(subject);

    Mail mail = new Mail();
    mail.addPersonalization(personalization);
    mail.setTemplateId(templateId);
    mail.setFrom(from);

    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      Response response = sg.api(request);
      System.out.println(response.getStatusCode());
      System.out.println(response.getBody());
      System.out.println(response.getHeaders());

    } catch (IOException ex) {
      throw ex;
    }
	}
}
    

