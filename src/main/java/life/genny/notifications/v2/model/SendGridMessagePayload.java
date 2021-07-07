package life.genny.notifications.v2.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import life.genny.qwanda.message.QBaseMSGMessageType;

public class SendGridMessagePayload extends MessagePayLoad {
	
	private String recipient;
	
	private String body;
	
	private String subject;
	
	private String templateId;

	
	private HashMap<String, String>  templateData = new HashMap<>();
	private List<String> ccList = new LinkedList<>();
	private List<String> bccList = new LinkedList<>();
	
	public void addCcList(String cc) {
		ccList.add(cc);
	}
	
	public void addBCcList(String bcc) {
		bccList.add(bcc);
	}
	
	public void addTemplateData(String key, String value) {
		templateData.put(key, value);
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
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	

	public HashMap<String, String> getTemplateData() {
		return templateData;
	}
	
	public List<String> getCcList() {
		return ccList;
	}

	public List<String> getBccList() {
		return bccList;
	}

	@Override
	public QBaseMSGMessageTypeV2 getMessageType() {
 		return QBaseMSGMessageTypeV2.SENDGRID;
	}

}
