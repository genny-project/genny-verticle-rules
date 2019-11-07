package life.genny.notifications;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;

import life.genny.models.GennyToken;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwandautils.MergeUtil;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.VertxUtils;

/*
 * Following ENV Variables need to set in order to make the class working
 *
 * EMAIL_SMTP_AUTH=
 * EMAIL_SMTP_HOST=
 * EMAIL_SMTP_PASS=
 * EMAIL_SMTP_PORT=587
 * EMAIL_SMTP_STARTTTLS=true
 * EMAIL_SMTP_USER=
 *
 * TWILIO_ACCOUNT_SID=
 * TWILIO_AUTH_TOKEN=
 * TWILIO_SENDER_MOBILE=
 *
 */
public abstract class NotificationHelper {

  private String msgTargetAttribute = null;
  private String emailSubject = null;

  public static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(
          MethodHandles.lookup().lookupClass().getCanonicalName());

  public NotificationHelper(String msgTargetAttribute) {
    this.msgTargetAttribute = msgTargetAttribute;
  }

  /*
   * The construtor of each child will passing the message attribute field
   * message = email return email address
   * message = SMS return mobile number
   */
  public String resolveRecipient(
      String[] arrRecipient, HashMap<String, String> contextMap, GennyToken userToken)
      throws Exception {

    String recipient = null;
    BaseEntity recipientBeFromDDT =
        VertxUtils.readFromDDT(userToken.getRealm(), arrRecipient[0], userToken.getToken());
    recipient =
        MergeUtil.getBaseEntityAttrValueAsString(recipientBeFromDDT, this.msgTargetAttribute);

    recipient = StringUtils.strip(recipient);
    if (StringUtils.isBlank(recipient)) {
      String errorMsg = "Recipient cannot EMPTY";
      log.error(errorMsg);
      throw new Exception(errorMsg);
    }

    // Using strip to do the basic sanitize
    return recipient;
  }

  /*
   * This will prepare the Message Body
   */
  public String prepareMessageBody(
      String[] arrNotificationRecipient,
      HashMap<String, String> contextMap,
      String templateCode,
      QBaseMSGMessageType messageType,
      GennyToken userToken) {

    /* Adding project code to context */
    String projectCode = "PRJ_" + userToken.getRealm().toUpperCase();

    contextMap.put("PROJECT", projectCode);

    String messageBody = null;
    String templateWordings = null;
    if (arrNotificationRecipient != null && arrNotificationRecipient.length > 0) {

      /*
       * Remarks: The email template is storing in GennyDB > template
       * If change the BaseEntity, then need to restart Wildfly-Rulsserveice
       *
       * This block is getting the template wordings from "Genny" google spreadsheet
       * https://docs.google.com/spreadsheets/d/1n60kJeBGY4v084JnhZtAxW-V1dnK9yNzjAs5qnDpd2k/edit?ts=5db24233#gid=791052034
       * templateCode = "Notifications" sheet > "code" colmun
       * User Token string is required
       */
      QBaseMSGMessageTemplate loadTemplate =
          QwandaUtils.getTemplate(templateCode, userToken.getToken());
      /*
       * This block of code is to fetch email template from the github CDN by using http
       * If is SMS message, then will just load the template form the Database
       */
      if (this.msgTargetAttribute == "PRI_EMAIL") {
        try {

          templateWordings = QwandaUtils.apiGet(loadTemplate.getEmail_templateId(), null);
          this.setEmailSubject(loadTemplate.getSubject());

        } catch (ClientProtocolException e) { // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) { // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else if (this.msgTargetAttribute == "PRI_MOBILE") {
        templateWordings = loadTemplate.getSms_template();
      }
      /*
       * This function retreive BaseEntity
       */
      BaseEntity recipientBeFromDDT =
          VertxUtils.readFromDDT(
              userToken.getRealm(), arrNotificationRecipient[0], userToken.getToken());
      Map<String, Object> newMap = new HashMap<>();
      newMap = createBaseEntityContextMap(contextMap, userToken);

      /*
       * This method combines the contextMap with the Template to product the final email content workings.
       *
       */
      messageBody = MergeUtil.merge(templateWordings, newMap);

      log.info("-------------------------Template Wordings-----------------------------");
      log.info(templateWordings);
      log.info("-------------------------Contect Map-----------------------------------");
      log.info(contextMap);
      log.info("-------------------------Tempate Map-----------------------------------");
      log.info(newMap);
      log.info("------------------------------------------------------------------------");

    } else {
      log.error("Recipient array is null");
    }
    // Using strip to do the basic sanitize
    return StringUtils.strip(messageBody);
  }

  /*
   * This will build the BaseEntity Map for the message template
   *
   */
  public Map<String, Object> createBaseEntityContextMap(
      Map<String, String> contextMap, GennyToken userToken) {

    Map<String, Object> baseEntityContextMap = new HashMap<>();

    for (Entry<String, String> entry : contextMap.entrySet()) {
      log.info(entry.getKey() + "/" + entry.getValue());

      String value = entry.getValue();
      BaseEntity be = null;
      if ((value != null) && (value.length() > 4)) {
        if (value.matches("[A-Z]{3}\\_.*")) { // MUST BE A BE CODE
          be = VertxUtils.readFromDDT(userToken.getRealm(), value, userToken.getToken());
        }
      }

      if (be != null) {
        baseEntityContextMap.put(entry.getKey().toUpperCase(), be);
        log.info("BE FOUND >>>>>>>>>>> " + be);
      } else {
        baseEntityContextMap.put(entry.getKey().toUpperCase(), value);
        log.info("BE NOT FOUND >>>>>>>>>>> " + be);
      }
    }

    return baseEntityContextMap;
  }

  public String getEmailSubject() {
    return emailSubject;
  }

  public void setEmailSubject(String emailSubject) {
    this.emailSubject = emailSubject;
  }
}
