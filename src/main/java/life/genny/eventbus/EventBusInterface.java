package life.genny.eventbus;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaMessage;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.RulesUtils;
import life.genny.utils.VertxUtils;

public interface EventBusInterface {
	static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static Object privacyFilter(BaseEntity user, Object payload, final String[] filterAttributes) {
		if (payload instanceof QDataBaseEntityMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QDataBaseEntityMessage) payload,
					new HashMap<String, BaseEntity>(), filterAttributes));
		} else if (payload instanceof QBulkMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QBulkMessage) payload, filterAttributes));
		} else if (payload instanceof QwandaMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QwandaMessage) payload, filterAttributes));
		} else
			return payload;
	}

	public static QDataBaseEntityMessage privacyFilter(BaseEntity user, QDataBaseEntityMessage msg,
			Map<String, BaseEntity> uniquePeople, final String[] filterAttributes) {
		ArrayList<BaseEntity> bes = new ArrayList<BaseEntity>();
		if (uniquePeople != null) {
			for (BaseEntity be : msg.getItems()) {
				if (be != null) {

					if (!uniquePeople.containsKey(be.getCode())) {
						if (be.getCode().equals(msg.getParentCode())) {
							// get the latest parent code from api to ensure links are ok?
							try {
								be = QwandaUtils.getBaseEntityByCode(be.getCode(), msg.getToken());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						be = privacyFilter(user, be, filterAttributes);
						uniquePeople.put(be.getCode(), be);
						bes.add(be);
					} else {
						/*
						 * Avoid sending the attributes again for the same BaseEntity, so sending
						 * without attributes
						 */
						BaseEntity slimBaseEntity = new BaseEntity(be.getCode(), be.getName());
						/*
						 * Setting the links again but Adam don't want it to be send as it increasing
						 * the size of BE. Frontend should create links based on the parentCode of
						 * baseEntity not the links. This requires work in the frontend. But currently
						 * the GRP_NEW_ITEMS are being sent without any links so it doesn't show any
						 * internships.
						 */
						slimBaseEntity.setLinks(be.getLinks());
						bes.add(slimBaseEntity);
					}
				}
			}
			msg.setItems(bes.toArray(new BaseEntity[bes.size()]));
		}
		return msg;
	}

	public static QwandaMessage privacyFilter(BaseEntity user, QwandaMessage msg, final String[] filterAttributes) {
		privacyFilter(user, msg.askData, filterAttributes);
		return msg;
	}

	public static QBulkMessage privacyFilter(BaseEntity user, QBulkMessage msg, final String[] filterAttributes) {
		Map<String, BaseEntity> uniqueBes = new HashMap<String, BaseEntity>();
		for (QDataBaseEntityMessage beMsg : msg.getMessages()) {
			beMsg = privacyFilter(user, beMsg, uniqueBes, filterAttributes);
		}
		return msg;
	}

	public static BaseEntity privacyFilter(BaseEntity user, BaseEntity be) {
		final String[] filterStrArray = { "PRI_FIRSTNAME", "PRI_LASTNAME", "PRI_MOBILE", "PRI_DRIVER", "PRI_OWNER",
				"PRI_IMAGE_URL", "PRI_CODE", "PRI_NAME", "PRI_USERNAME", "PRI_DRIVER_RATING" };

		return privacyFilter(user, be, filterStrArray);
	}

	public static BaseEntity privacyFilter(BaseEntity user, BaseEntity be, final String[] filterAttributes) {
		Set<EntityAttribute> allowedAttributes = new HashSet<EntityAttribute>();
		for (EntityAttribute entityAttribute : be.getBaseEntityAttributes()) {
			// log.info("ATTRIBUTE:"+entityAttribute.getAttributeCode()+(entityAttribute.getPrivacyFlag()?"PRIVACYFLAG=TRUE":"PRIVACYFLAG=FALSE"));
			if ((be.getCode().startsWith("PER_")) && (!be.getCode().equals(user.getCode()))) {
				String attributeCode = entityAttribute.getAttributeCode();

				if (Arrays.stream(filterAttributes).anyMatch(x -> x.equals(attributeCode))) {

					allowedAttributes.add(entityAttribute);
				} else {
					if (attributeCode.startsWith("PRI_IS_")) {
						allowedAttributes.add(entityAttribute);// allow all roles
					}
					if (attributeCode.startsWith("LNK_")) {
						allowedAttributes.add(entityAttribute);// allow attributes that starts with "LNK_"
					}
				}
			} else {
				if (!entityAttribute.getPrivacyFlag()) { // don't allow privacy flag attributes to get through
					allowedAttributes.add(entityAttribute);
				}
			}
		}
		be.setBaseEntityAttributes(allowedAttributes);

		return be;
	}

	public static Boolean checkIfAttributeValueContainsString(BaseEntity baseentity, String attributeCode,
			String checkIfPresentStr) {

		Boolean isContainsValue = false;

		if (baseentity != null && attributeCode != null && checkIfPresentStr != null) {
			String attributeValue = baseentity.getValue(attributeCode, null);

			if (attributeValue != null && attributeValue.toLowerCase().contains(checkIfPresentStr.toLowerCase())) {
				return true;
			}
		}

		return isContainsValue;
	}

	public default void write(final String channel, final Object payload) throws NamingException {
		log.info("MOCK EVT BUS WRITE: " + channel + ":" + payload);
	}

	public default void send(final String channel, final Object payload) throws NamingException {
		log.info("MOCK EVT BUS SEND: " + channel + ":" + payload);
	}

	public default void writeMsg(final String channel, final Object msg) throws NamingException {

		if ((GennySettings.forceEventBusApi) && (!VertxUtils.cachedEnabled)) {
			try {
				String json = msg.toString();
				JsonParser parser = new JsonParser();
				com.google.gson.JsonObject event = parser.parse(json).getAsJsonObject();

				event.addProperty("eventbus", "WRITE");
//				if (("DATA_MSG".equals(event.get("msg_type").getAsString()))
//						&& ("BaseEntity".equals(event.get("data_type").getAsString()))) {
//					com.google.gson.JsonArray items = event.getAsJsonArray("items");
//					for (JsonElement pa : items) {
//						com.google.gson.JsonObject item = pa.getAsJsonObject();
//						if (item.get("code").getAsString().startsWith("THM_")) {
//							com.google.gson.JsonArray eas = item.getAsJsonArray("baseEntityAttributes");
//							for (JsonElement ea : eas) {
//								com.google.gson.JsonObject eaItem = ea.getAsJsonObject();
//								String valueString = eaItem.get("valueString").getAsString();
//								if (valueString != null) {
//									if (valueString.startsWith("{")) { // TODO hack
//										com.google.gson.JsonObject vs = parser.parse(valueString).getAsJsonObject();
//										eaItem.add("valueString", vs); // set as Json to make fronternd read it
//									}
//								}
//							}
//						}
//					}
//					event.add("items", items);
//					json = event.getAsString();
//				}
				json = msg.toString();
//				json = json.replace("\\\"","\"");
//				json = json.replace("\"{\"","{\"");
//				json = json.replace("\"}\"","\"}");
//				json = json.replace("}\",","},");
//				json = json.replace("\"{","{");
				QwandaUtils.apiPostEntity(GennySettings.bridgeServiceUrl + "?channel=" + channel, json,
						event.get("token").getAsString());
			} catch (Exception e) {
				String json2 = msg.toString();
				log.error("Error in posting message to bridge eventbus:" + channel + ":" + msg);
			}

		} else {
			write(channel, msg);

		}

	}

	public default void sendMsg(final String channel, final Object msg) throws NamingException {
		String json = msg.toString();
		JsonObject event = new JsonObject(json);

		if (GennySettings.forceEventBusApi) {
			try {
				event.put("eventbus", "SEND");
				QwandaUtils.apiPostEntity(GennySettings.bridgeServiceUrl + "?channel=" + channel, json,
						event.getString("token"));
			} catch (Exception e) {
				log.error("Error in posting message to bridge eventbus:" + event);
			}

		} else {
			send(channel, msg);

		}

	}


	
	public default void publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes) {
		try {
			// Actually Send ....
			switch (channel) {
			case "event":
			case "events":
				sendMsg("events", payload);
				break;
			case "data":
				writeMsg("data", payload);
				break;

			case "webdata":
				payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
				writeMsg("webdata", payload);
				break;
			case "cmds":
			case "webcmds":
				payload = EventBusInterface.privacyFilter(user,payload, filterAttributes); 
				writeMsg("webcmds", payload);
				break;
			case "services":
				writeMsg("services", payload);
				break;
			case "messages":
				writeMsg("messages", payload);
				break;
			case "statefulmessages":
				writeMsg("statefulmessages", payload);
				break;
			case "signals":
				writeMsg("signals", payload);
				break;

			default:
				payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
				sendMsg(channel, payload);
				// log.error("Channel does not exist: " + channel);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
