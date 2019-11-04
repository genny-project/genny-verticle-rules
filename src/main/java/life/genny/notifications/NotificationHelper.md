# NotificationHub for Workflows

## The Notificatio Hub package includes

### Genny-Rules

1. Custom WorkItem Handler Controller
`genny-rules/src/main/java/life/genny/jbpm/customworkitemhandlers/NotificationHubWorkItemHandler.java`

### Prj_Genny

1. Workflow Demonstration how to send notificaiton to user
`rules/rulesCurrent/shared/_BPMN_WORKFLOWS/XXX_Lin/notificationHub.bpmn`
2. Defines WorkItem Handler for Eclipse
`src/main/resources/META-INF/notificationHubWorkDefinition.wid`
3. Register Customer WorkItem Handler in this class
`src/test/java/life/genny/test/GennyKieSession.java`
4. jUnit test case for running workflow to trigger the notification sending
`src/test/java/life/genny/test/LinTestNotificationHub.java`

### Genny-Veritcal-Rules

1. Notificaiton Hub Pachage
`src/main/java/life/genny/notifications/EmailHelper.java`
`src/main/java/life/genny/notifications/NotificationHelper.java`
`src/main/java/life/genny/notifications/NotificationHelper.md`
`src/main/java/life/genny/notifications/SmsHelper.java`
3. Test Cases for Notification Hub
`src/test/java/life/genny/test/qwandautils/notifications/NotificaitonEmailTest.java`

### Env Variables
TWILLIO_ACCOUNT_SID=AC04fff3a5b11e82725662d3a5c1ba0711
TWILLIO_AUTH_TOKEN=89a3395a0b1061d815ea5f4b1dcd4bfc

This workflows will accept sigal reqeust then sends email or SMS to the userSession owner.

- The workflow extracts the user information such as email, name, mobile.
- The workflows will combine the student info with a template, then pass the message to Email API or SMS API.
- The template engine will us FreeMaker
- FreeMaker Eclipse plugin : https://marketplace.eclipse.org/content/freemarker-ide
- FreeMaker sandbox: https://try.freemarker.apache.org/
- FreeMaker Examples: https://freemarker.apache.org/docs/pgui_quickstart_all.html
- The package will use QMessage Services genny/messages/src/main/java/life/genny/message/QEmailMessageManager.java and genny/message/QSMSMessageManager.java
- genny/qwanda-utils/src/main/java/life/genny/qwandautils/MessageUtils.java
- genny-rules/src/main/java/life/genny/rules/QRules.java line:923 public void sendMessage()
- Send email exmaple projects/genny/prj_internmatch/rules/05_Workflows/InternPlacement/SendEmailWithAttachment/00_TriggerGeneratingWeeklyJournal.drl
- prj_internmatch/rules/05_Workflows/InternPlacement/SendEmailWithAttachment/20_Send_Email_With_Attachment.drl



How to use the Notifcation Hub workflow?

1. In the workflows property set data item notificaitonCode as String
2. In the Call Activity, kcontext.setVariable("notificationCode", "APPLIED");
3. In the I/O Parameteres, Input Data Mapping, From "notificationCode" to "notificationCode".



Signal `controlSignal` that triggers action:
1. SHORTLIST
2. INTERVIEW
3. OFFER
4. PLACED
5. IN_PROGRESS
6. FINISH_INTERNSHIP