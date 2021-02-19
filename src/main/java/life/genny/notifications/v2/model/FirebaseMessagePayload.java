package life.genny.notifications.v2.model;

import life.genny.qwanda.message.QBaseMSGMessageType;

public class FirebaseMessagePayload extends MessagePayLoad {
	
	private String recipient;
	
	private String body;
	
	private String title;
	
	private String apiKey;
	
	public String getApiKey() {
		return apiKey;
	}



	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



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
 		return QBaseMSGMessageTypeV2.FIREBASE;
	}

}
