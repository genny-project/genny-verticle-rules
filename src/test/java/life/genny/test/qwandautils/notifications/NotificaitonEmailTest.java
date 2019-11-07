package life.genny.test.qwandautils.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.github.javafaker.Faker;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import life.genny.notifications.EmailHelper;

// TODO complate the test case
/**
 * This testing requires MAven Example using plain JavaMail for sending / receiving mails via
 * GreenMail server.
 *
 * http://www.icegreen.com/greenmail/
 * https://www.hascode.com/2012/07/integration-testing-imap-smtp-and-pop3-with-greenmail/
 */
public class NotificaitonEmailTest {

  private GreenMail mailServer;
  private EmailHelper emailHelper;

  @Test
  public void sendShouldSetTheRightText() throws Exception {

    // Random faker
    Faker faker = new Faker();

    String SMTP_TEST_PORT = Integer.toString(faker.number().numberBetween(5000, 8000));
    String USER_PASSWORD = faker.internet().password();
    String USER_NAME = faker.internet().uuid();
    String EMAIL_USER_ADDRESS = faker.internet().emailAddress();
    String EMAIL_TO = "Recipient_" + faker.internet().emailAddress();
    String EMAIL_SUBJECT = "SUBJECT: " + faker.buffy().episodes();
    String EMAIL_TEXT = "BODY: " + faker.buffy().quotes();
    String LOCALHOST = "127.0.0.1"; // must localhost

    mailServer = new GreenMail(new ServerSetup(Integer.parseInt(SMTP_TEST_PORT), null, "smtp"));
    mailServer.start();

    // Setting the Email Helper
    emailHelper = new EmailHelper();
    emailHelper.setEMAIL_SMTP_HOST(LOCALHOST);
    emailHelper.setEMAIL_SMTP_PORT(SMTP_TEST_PORT);
    emailHelper.setEMAIL_SMTP_AUTH("true");
    emailHelper.setEMAIL_SMTP_USER(USER_NAME);
    emailHelper.setEMAIL_SMTP_PASS(USER_PASSWORD);
    emailHelper.setEMAIL_SMTP_STARTTLS("false");
    emailHelper.setEmailSubject(EMAIL_SUBJECT);

    // setup user on the mail server
    mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);

    // Send Email
    emailHelper.deliverEmailMsg(EMAIL_TO, EMAIL_TEXT);

    // fetch messages from server
    MimeMessage[] messages = mailServer.getReceivedMessages();
    assertNotNull(messages);
    assertEquals(1, messages.length);
    
    //Check Recipient, Subject and body
    MimeMessage m = messages[0];
    assertEquals(EMAIL_TO, (m.getRecipients(Message.RecipientType.TO))[0].toString());
    assertEquals(EMAIL_SUBJECT, m.getSubject());
    assertTrue(String.valueOf(m.getContent()).contains(EMAIL_TEXT));

    System.out.println((m.getRecipients(Message.RecipientType.TO))[0].toString());
    System.out.println(m.getSubject());
    System.out.println(m.getContent());

    // Must Stop the mail server after test
    mailServer.stop();
  }
}
