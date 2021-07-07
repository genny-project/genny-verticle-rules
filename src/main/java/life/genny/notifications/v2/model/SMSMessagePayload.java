package life.genny.notifications.v2.model;

import life.genny.qwanda.message.QBaseMSGMessageType;

public class SMSMessagePayload extends MessagePayLoad {
	
	private String recipient;
	
	private String body;
	
	public String getRecipient() {
		return recipient;
	}



	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}



	public String getBody() {
		return body;
	}



	public void setBody(String body) {
		this.body = body;
	}



	@Override
	public QBaseMSGMessageTypeV2 getMessageType() {
 		return QBaseMSGMessageTypeV2.SMS;
	}

}
