package life.genny.notifications;

import static java.lang.System.getProperties;
import static javax.mail.Message.RecipientType.TO;
import static javax.mail.Session.getDefaultInstance;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

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
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.EEntityStatus;
import life.genny.qwandautils.GennySettings;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;

public class EmailHelper extends NotificationHelper {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

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
		// generateMailMessage.addRecipient(CC, new
		// InternetAddress("email@emailaddress.email"));
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

	public static String encodeUrl(String base, String parentCode, String code, String targetCode) {
		String encodedParentCode = new String(Base64.getEncoder().encode(parentCode.getBytes()));
		String encodedCode = new String(Base64.getEncoder().encode(code.getBytes()));
		String encodedTargetCode = new String(Base64.getEncoder().encode(targetCode.getBytes()));

		String url = base + "/" + encodedParentCode + "/" + encodedCode + "/" + encodedTargetCode;
		return url;
	}

	public static void sendGrid(BaseEntityUtils beUtils, String recipient, String subject, String templateId,
			HashMap<String, String> templateData, Boolean nonProdTest) throws IOException {

		sendGrid(beUtils, recipient, null, null, subject, templateId, templateData, nonProdTest);
	}

	public static void sendGrid(BaseEntityUtils beUtils, String recipient, List<String> ccList, List<String> bccList,
			String subject, String templateId, HashMap<String, String> templateData, Boolean nonProdTest)
			throws IOException {

		if (GennySettings.projectUrl.contains("interns") || nonProdTest) {
			BaseEntity projectBE = beUtils
					.getBaseEntityByCode("PRJ_" + beUtils.getGennyToken().getRealm().toUpperCase());
			String sendGridEmailSender = projectBE.getValueAsString("ENV_SENDGRID_EMAIL_SENDER");
			String sendGridEmailNameSender = projectBE.getValueAsString("ENV_SENDGRID_EMAIL_NAME_SENDER");
			String sendGridApiKey = projectBE.getValueAsString("ENV_SENDGRID_API_KEY");
			log.info("The name for email sender " + sendGridEmailNameSender + " sending to " + recipient
					+ " , SG templateID=" + templateId);

			// Find the recipient baseentity

			BaseEntity recipientBE = null;
			String timezoneId = "Australia/Melbourne";

			if (("internmatch@outcomelife.com.au".equals(recipient.trim()))
					|| ("spc@outcome.life".equals(recipient.trim()))) {
				recipientBE = beUtils.getBaseEntityByCode("PRJ_INTERNMATCH");
			} else {

				SearchEntity searchBE = new SearchEntity("SBE_EMAIL", "Search By Email")
						.addFilter("PRI_EMAIL", SearchEntity.StringFilter.EQUAL, recipient.trim())
						.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").setPageStart(0)
						.setSearchStatus(EEntityStatus.PENDING).setPageSize(10000);

				List<BaseEntity> results = beUtils.getBaseEntitys(searchBE);
				if (results != null && !(results.isEmpty())) {
					recipientBE = results.get(0);
					timezoneId = recipientBE.getValue("PRI_TIMEZONE_ID",
							recipientBE.getValue("PRI_TIME_ZONE", "Australia/Melbourne")); // if not there , check for
																							// incorrect tz
				} else {
					log.error("CANNOT FIND RECIPIENT from email:" + recipient + ", skip sending email!!!");
					return;
				}
			}

			Email from = new Email(sendGridEmailSender, sendGridEmailNameSender);
			Email to = new Email(recipient);

			// TODO: HACK HACK HACK ACC
			// To stop sending the same email to the same recipient more than once, we will
			// make some assumptions...
			// (1) The mutiple emails are being sent within the same short time session and
			// we can assume that if
			// we save a flag to the cache to indicate an email has been sent then it will
			// remain in cache whilst the
			// subsequent emails are being sent.

			String urlBasedAttribute = GennySettings.projectUrl.replace("https://", "").replace(".gada.io", "")
					.replace("-", "_").toUpperCase();
			String dedicatedTestEmail = projectBE.getValue("EML_" + urlBasedAttribute, null);
			if (dedicatedTestEmail != null) {
				log.info("Found email " + dedicatedTestEmail + " for project attribute EML_" + urlBasedAttribute);
				to = new Email(dedicatedTestEmail);
			}

			SendGrid sg = new SendGrid(sendGridApiKey);

			Personalization personalization = new Personalization();

			personalization.addTo(to);
			personalization.setSubject(subject);

			if (ccList != null) {
				for (String email : ccList) {
					if (!email.equals(to.getEmail())) {
						personalization.addCc(new Email(email));
					}
				}
			}
			if (bccList != null) {
				for (String email : bccList) {
					if (!email.equals(to.getEmail())) {
						personalization.addBcc(new Email(email));
					}
				}
			}

			String dateStr = "";
			String timeStr = "";
			LocalDateTime dateTime = null;

			for (String key : templateData.keySet()) {
				String printValue = templateData.get(key);
				if (key.equals("password")) {
					printValue = "REDACTED";
				}
				if ((key.equals("date")) || (key.equals("time"))) {
					if (key.equals("date")) {
						dateStr = templateData.get(key);
					}
					if (key.equals("time")) {
						timeStr = templateData.get(key);
					}
				} else {
					log.info("key: " + key + ", value: " + printValue);
					personalization.addDynamicTemplateData(key, templateData.get(key));
				}
			}

			if ((dateStr != null) || (timeStr != null)) {
				log.info("'date' = " + dateStr + "     and 'time' = " + timeStr + " and timezone id =" + timezoneId);

				// Find LocalDateTime

				// Using TimezoneId find localised DateTime

				// set date and timeStr and supply timezone
				// hack

				personalization.addDynamicTemplateData("date", templateData.get("date"));
				timeStr += " (" + timezoneId + ")";
				personalization.addDynamicTemplateData("time", timeStr);
			}

			Mail mail = new Mail();
			mail.addPersonalization(personalization);
			mail.setTemplateId(templateId);
			mail.setFrom(from);

			// ok, create a unique key
			// use templateId and templateData
			// key = templateId + templateData.get("intern")
			// save a 'sent' flag to cache after sending once
			String key = templateId + ":" + templateData.get("intern") + ":" + to.getEmail();
			JsonObject emailSentFlag = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
					beUtils.getServiceToken().getToken());
			Boolean sendAllowed = false;
			if (emailSentFlag != null) {
				if (emailSentFlag.containsKey("status")) {
					if ("error".equalsIgnoreCase(emailSentFlag.getString("status"))) {
						sendAllowed = true;
						// Now set the flag to prevent sending this one again
						VertxUtils.writeCachedJson(beUtils.getGennyToken().getRealm(), key,
								"Sent " + LocalDateTime.now(), beUtils.getServiceToken().getToken());
						log.warn("Setting "+key+" to prevent email sending more than once");
					} else {
						log.warn("FOUND "+key+" and prevented email sending more than once");
					}
				}
			}

			if (sendAllowed) {
				Request request = new Request();
				try {
					request.setMethod(Method.POST);
					request.setEndpoint("mail/send");
					request.setBody(mail.build());
					Response response = sg.api(request);
					log.info(response.getStatusCode());
					log.info(response.getBody());
					log.info(response.getHeaders());

				} catch (IOException ex) {
					throw ex;
				}
			}

		} else {
			nonProdTest = false;
			log.info("WARNING: Email not sent because it is not on the Production server");
		}
		/*
		 * if (!nonProdTest) { QCmdMessage msg = new QCmdMessage("TOAST", "INFO"); msg.
		 * setMessage("Email not sent because you are not on the Production server!!");
		 * msg.setToken(beUtils.getGennyToken().getToken()); msg.setSend(true);
		 * VertxUtils.writeMsg("webcmds", msg); }
		 */
	}

}
