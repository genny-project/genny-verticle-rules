package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.kie.api.KieBase;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.MessageProducer;
import life.genny.channel.DistMap;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusVertx;
import life.genny.eventbus.WildflyCacheInterface;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;


public class VertxUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static boolean cachedEnabled = true;
	
	static public EventBusInterface eb;
	
	static final String DEFAULT_TOKEN = "DUMMY";
	static final String[] DEFAULT_FILTER_ARRAY = { "PRI_FIRSTNAME", "PRI_LASTNAME", "PRI_MOBILE",
			"PRI_IMAGE_URL", "PRI_CODE", "PRI_NAME", "PRI_USERNAME" };



	public enum ESubscriptionType {
		DIRECT, TRIGGER;

	}
	
	public static GennyCacheInterface cacheInterface = null;
	
	public static void init(EventBusInterface eventBusInterface, GennyCacheInterface gennyCacheInterface)
	{
		if (gennyCacheInterface == null) {
			log.error("NULL CACHEINTERFACE SUPPLUED IN INIT");
		}
			eb = eventBusInterface;
			cacheInterface = gennyCacheInterface;

	}
	

	static Map<String, String> localCache = new ConcurrentHashMap<String, String>();
	static Map<String, MessageProducer<JsonObject>> localMessageProducerCache = new ConcurrentHashMap<String, MessageProducer<JsonObject>>();

	static public void setRealmFilterArray(final String realm, final String[] filterArray)
	{
		 putStringArray(realm, "FILTER", "PRIVACY",
					filterArray);
	}
	
	static public String[] getRealmFilterArray(final String realm)
	{
		String[] result = getStringArray(realm, "FILTER", "PRIVACY");
		if (result == null) {
			return DEFAULT_FILTER_ARRAY;
		} else {
			return result;
		}
}

	static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Class clazz) {
		return getObject(realm, keyPrefix, key, clazz, DEFAULT_TOKEN);
	}

	static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Class clazz,
			final String token) {
		T item = null;
		JsonObject json = readCachedJson(realm + ":" + keyPrefix + ":" + key, token);
		if (json.getString("status").equalsIgnoreCase("ok")) {
			JsonObject data = json.getJsonObject("value");
			if (data == null) {
				log.error("BAD DATA IS NULL IN GETOBJECT , json = "+json);
				return null;
			} else {
				item = (T) JsonUtils.fromJson(data.toString(), clazz);
				return item;
			}
			
		} else {
			return null;
		}

	}

	static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Type clazz) {
		return getObject(realm, keyPrefix, key, clazz, DEFAULT_TOKEN);
	}

	static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Type clazz,
			final String token) {
		T item = null;
		JsonObject json = readCachedJson(realm + ":" + keyPrefix + ":" + key, token);
		if (json.getString("status").equalsIgnoreCase("ok")) {
			JsonObject data = json.getJsonObject("value");
			if (data == null) {
				log.error("BAD DATA IS NULL IN GETOBJECT , json = "+json);
				return null;
			} else {
				item = (T) JsonUtils.fromJson(data.toString(), clazz);
				return item;
			}
		} else {
			return null; 
		}
	}

	static public void putObject(final String realm, final String keyPrefix, final String key, final Object obj) {
		putObject(realm, keyPrefix, key, obj, DEFAULT_TOKEN);
	}

	static public void putObject(final String realm, final String keyPrefix, final String key, final Object obj,
			final String token) {
		String data = JsonUtils.toJson(obj);
		data = data.replaceAll("\\\"", "\"");
		data = data.replaceAll("\\n", "\n");
		writeCachedJson(realm + ":" + keyPrefix + ":" + key, data, token);
	}

	static public JsonObject readCachedJson(final String key) {
		return readCachedJson(key, DEFAULT_TOKEN);
	}

	static public JsonObject readCachedJson(final String key, final String token) {
		JsonObject result = null;

		if (!(GennySettings.devMode  && cacheInterface instanceof WildflyCacheInterface)/*|| (!GennySettings.isCacheServer)*/) {
			String ret = null;
			JsonObject retj = null;
			try {
				log.info("VERTX READING DIRECTLY FROM CACHE! USING "+(GennySettings.isCacheServer?" LOCAL DDT":"CLIENT "));
				ret = (String) cacheInterface.readCache(key, token);
				if (ret != null) {
					//TODO : HACK. The worst
					ret = ret.replaceAll("\\\"", "\"");
					ret = ret.replaceAll("\\n", "\n");
				}
			//	log.info("VERTX READ CACHED JSON FIXED STRING !"+ret);
				retj = new JsonObject(ret);

			} catch (Exception e) {
				log.error("Cache is  null "+e.getLocalizedMessage());
			}

			if (ret != null) {
				result = new JsonObject().put("status", "ok").put("value", retj);
			} else { 
				result = new JsonObject().put("status", "error").put("value", ret);
			}
		} else {
			String resultStr = null;
			try {
				log.info("VERTX READING  FROM CACHE API!");
				resultStr = QwandaUtils.apiGet(GennySettings.ddtUrl + "/read/" + key, token);
				result = new JsonObject(resultStr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return result;
	}

	static public JsonObject writeCachedJson(final String key, final String value) {
		return writeCachedJson(key, value, "DUMMY");
	}

	static public JsonObject writeCachedJson(final String key, final String value, final String token) {

		return writeCachedJson(key, value, token, 0L);

	}
	
	static public JsonObject writeCachedJson(final String key, String value, final String token, long ttl_seconds) {
		if (!(GennySettings.devMode  && cacheInterface instanceof WildflyCacheInterface)/*|| (!GennySettings.isCacheServer)*/) {
			log.info("WRITING USING "+(GennySettings.isCacheServer?" LOCAL DDT":"CLIENT ")+"  "+key);
			// TODO: HACK
			value = value.replaceAll("\\\"", "\"");
			value = value.replaceAll("\\n", "\n");

			cacheInterface.writeCache(key, value, token,ttl_seconds);


		} else {
			try {
				log.info("WRITING TO CACHE USING API! "+key);
				QwandaUtils.apiPostEntity(GennySettings.ddtUrl + "/write", value, token);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		JsonObject ok = new JsonObject().put("status", "ok");
		return ok;

	}
	
	static public void clearDDT()
	{
		cacheInterface.clear();
	}

	static public BaseEntity readFromDDT(final String code, final boolean withAttributes, final String token) {
		BaseEntity be = null;
		JsonObject json = readCachedJson(code);
		if ("ok".equals(json.getString("status"))) {
			//log.info("Read from DDT and is OK "+json);
			JsonObject jo = json.getJsonObject("value");
			//log.info("Read from DDT2 and is OK "+jo);
			be = JsonUtils.fromJson(jo.toString(), BaseEntity.class);
			if (be.getCode()==null) {
				log.error("readFromDDT baseEntity has null code! json is ["+json.getString("value")+"]");
			}
		} else {
			// fetch normally
			log.info("Cache MISS for " + code+" with attributes");
			try {
				if (withAttributes) {
					be = QwandaUtils.getBaseEntityByCodeWithAttributes(code, token);
					String savedJson = JsonUtils.toJson(be);
				//	log.info("WRITING TO CACHE AFTER API "+savedJson);
				//	writeCachedJson(code, savedJson,token);
				} else {
					be = QwandaUtils.getBaseEntityByCode(code, token);
				}
			} catch (Exception e) {
				// Okay, this is bad. Usually the code is not in the database but in keycloak
				// So lets leave it to the rules to sort out... (new user)
				log.error("BE " + code + " is NOT IN CACHE OR DB " + e.getLocalizedMessage());
				return null;

			}
		}
		return be;
	}

	static boolean cacheDisabled = System.getenv("NO_CACHE") != null ? true : false;

	static public BaseEntity readFromDDT(final String code, final String token) {
		// if ("PER_SHARONCROW66_AT_GMAILCOM".equals(code)) {
		// System.out.println("DEBUG");
		// }
		return readFromDDT(code, true,token);
}

	static public void subscribeAdmin(final String realm, final String adminUserCode) {
		final String SUBADMIN = "SUBADMIN";
		// Subscribe to a code
		Set<String> adminSet = getSetString(realm, SUBADMIN, "ADMINS");
		adminSet.add(adminUserCode);
		putSetString(realm, SUBADMIN, "ADMINS", adminSet);
	}
	
	static public void unsubscribeAdmin(final String realm, final String adminUserCode) {
		final String SUBADMIN = "SUBADMIN";
		// Subscribe to a code
		Set<String> adminSet = getSetString(realm, SUBADMIN, "ADMINS");
		adminSet.remove(adminUserCode);
		putSetString(realm, SUBADMIN, "ADMINS", adminSet);
	}

	
	static public void subscribe(final String realm, final String subscriptionCode, final String userCode) {
		final String SUB = "SUB";
		// Subscribe to a code
		Set<String> subscriberSet = getSetString(realm, SUB, subscriptionCode);
		subscriberSet.add(userCode);
		putSetString(realm, SUB, subscriptionCode, subscriberSet);
	}

	static public void subscribe(final String realm, final List<BaseEntity> watchList, final String userCode) {
		final String SUB = "SUB";
		// Subscribe to a code
		for (BaseEntity be : watchList) {
			Set<String> subscriberSet = getSetString(realm, SUB, be.getCode());
			subscriberSet.add(userCode);
			putSetString(realm, SUB, be.getCode(), subscriberSet);
		}
	}

	static public void subscribe(final String realm, final BaseEntity be, final String userCode) {
		final String SUB = "SUB";
		// Subscribe to a code
		Set<String> subscriberSet = getSetString(realm, SUB, be.getCode());
		subscriberSet.add(userCode);
		putSetString(realm, SUB, be.getCode(), subscriberSet);

	}
	
	/*
	 * Subscribe list of users to the be
	 */
	static public void subscribe(final String realm, final BaseEntity be, final String[] SubscribersCodeArray) {
		final String SUB = "SUB";
		// Subscribe to a code
		//Set<String> subscriberSet = getSetString(realm, SUB, be.getCode());
		//subscriberSet.add(userCode);
		Set<String> subscriberSet = new HashSet<String>(Arrays.asList(SubscribersCodeArray));
		putSetString(realm, SUB, be.getCode(), subscriberSet);

	}

	static public void unsubscribe(final String realm, final String subscriptionCode, final Set<String> userSet) {
		final String SUB = "SUB";
		// Subscribe to a code
		Set<String> subscriberSet = getSetString(realm, SUB, subscriptionCode);
		subscriberSet.removeAll(userSet);

		putSetString(realm, SUB, subscriptionCode, subscriberSet);
	}

	static public String[] getSubscribers(final String realm, final String subscriptionCode) {
		final String SUB = "SUB";
		// Subscribe to a code
		String[] resultArray = getObject(realm, SUB, subscriptionCode, String[].class);
		
		String[] resultAdmins = getObject(realm, "SUBADMIN", "ADMINS",String[].class);
		 String[] result = (String[]) ArrayUtils.addAll(resultArray, resultAdmins);
		return result;

	}

	static public void subscribeEvent(final String realm, final String subscriptionCode, final QEventMessage msg) {
		final String SUBEVT = "SUBEVT";
		// Subscribe to a code
		Set<String> subscriberSet = getSetString(realm, SUBEVT, subscriptionCode);
		subscriberSet.add(JsonUtils.toJson(msg));
		putSetString(realm, SUBEVT, subscriptionCode, subscriberSet);
	}

	static public QEventMessage[] getSubscribedEvents(final String realm, final String subscriptionCode) {
		final String SUBEVT = "SUBEVT";
		// Subscribe to a code
		String[] resultArray = getObject(realm, SUBEVT, subscriptionCode, String[].class);
		QEventMessage[] msgs = new QEventMessage[resultArray.length];
		int i = 0;
		for (String result : resultArray) {
			msgs[i] = JsonUtils.fromJson(result, QEventMessage.class);
			i++;
		}
		return msgs;
}

	static public Set<String> getSetString(final String realm, final String keyPrefix, final String key) {
		String[] resultArray = getObject(realm, keyPrefix, key, String[].class);
		if (resultArray == null) {
			return new HashSet<String>();
		}
		return Sets.newHashSet(resultArray);
	}

	static public void putSetString(final String realm, final String keyPrefix, final String key, final Set set) {
		String[] strArray = (String[]) FluentIterable.from(set).toArray(String.class);
		putObject(realm, keyPrefix, key, strArray);
	}
	
	static public void putStringArray(final String realm, final String keyPrefix, final String key, final String[] string) {
		putObject(realm, keyPrefix, key, string);
	}
	
	static public String[] getStringArray(final String realm, final String keyPrefix, final String key) {
		String[] resultArray = getObject(realm, keyPrefix, key, String[].class);
		if (resultArray == null) {
			return null;
		}
		
		return resultArray;
	}


	static public void putMap(final String realm, final String keyPrefix, final String key, final Map<String,String> map) {
		putObject(realm, keyPrefix, key, map);
	}
	
	static public Map<String,String> getMap(final String realm, final String keyPrefix, final String key) {
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> myMap = getObject(realm, keyPrefix, key, type);
		return myMap;
	}

	public static void putMessageProducer(String sessionState, MessageProducer<JsonObject> toSessionChannel) {
		System.out.println("Registering SessionChannel to "+sessionState);
		localMessageProducerCache.put(sessionState, toSessionChannel);

	}

	public static MessageProducer<JsonObject> getMessageProducer(String sessionState) {

		return localMessageProducerCache.get(sessionState);

	}
	
	static public void publish(BaseEntity user, String channel, Object payload) {

		publish(user,channel,payload,DEFAULT_FILTER_ARRAY);
	}

	
	static public void publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes) {
		

		
		eb.publish(user, channel, payload, filterAttributes);
	}

	static public Object privacyFilter(BaseEntity user, Object payload, final String[] filterAttributes) {
		if (payload instanceof QDataBaseEntityMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QDataBaseEntityMessage) payload,new HashMap<String, BaseEntity>(), filterAttributes));
		} else if (payload instanceof QBulkMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QBulkMessage) payload,filterAttributes));
		} else
			return payload;
	}


	
	static public QDataBaseEntityMessage privacyFilter(BaseEntity user, QDataBaseEntityMessage msg,
			Map<String, BaseEntity> uniquePeople, final String[] filterAttributes) {
		ArrayList<BaseEntity> bes = new ArrayList<BaseEntity>();
		for (BaseEntity be : msg.getItems()) {
			if (uniquePeople != null && be.getCode() != null && !uniquePeople.containsKey(be.getCode())) {
				
				be = privacyFilter(user, be, filterAttributes);
				uniquePeople.put(be.getCode(), be);
				bes.add(be);
			} 
			else {
				/* Avoid sending the attributes again for the same BaseEntity, so sending without attributes */
				BaseEntity slimBaseEntity = new BaseEntity(be.getCode(), be.getName());
				/* Setting the links again but Adam don't want it to be send as it increasing the size of BE.
				 * Frontend should create links based on the parentCode of baseEntity not the links. This requires work in the frontend.
				 * But currently the GRP_NEW_ITEMS are being sent without any links so it doesn't show any internships.
				 */
				slimBaseEntity.setLinks(be.getLinks());
				bes.add(slimBaseEntity);
			}
		}
		msg.setItems(bes.toArray(new BaseEntity[bes.size()]));
		return msg;
	}
	
	static public QBulkMessage privacyFilter(BaseEntity user,QBulkMessage msg, final String[] filterAttributes) {
		Map<String, BaseEntity> uniqueBes = new HashMap<String, BaseEntity>();
		for (QDataBaseEntityMessage beMsg : msg.getMessages()) {
			beMsg = privacyFilter(user,beMsg, uniqueBes,filterAttributes);
		}
		return msg;
}

	static public BaseEntity privacyFilter(BaseEntity user, BaseEntity be) {
		final String[] filterStrArray = { "PRI_FIRSTNAME", "PRI_LASTNAME", "PRI_MOBILE", "PRI_DRIVER", "PRI_OWNER",
				"PRI_IMAGE_URL", "PRI_CODE", "PRI_NAME", "PRI_USERNAME", "PRI_DRIVER_RATING" };

		return privacyFilter(user, be, filterStrArray);
	}

	static public BaseEntity privacyFilter(BaseEntity user, BaseEntity be, final String[] filterAttributes) {
		Set<EntityAttribute> allowedAttributes = new HashSet<EntityAttribute>();
		for (EntityAttribute entityAttribute : be.getBaseEntityAttributes()) {
			// System.out.println("ATTRIBUTE:"+entityAttribute.getAttributeCode()+(entityAttribute.getPrivacyFlag()?"PRIVACYFLAG=TRUE":"PRIVACYFLAG=FALSE"));
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

	public static String apiPostEntity(final String postUrl, final String entityString, final String authToken, final Consumer<String> callback)
			throws IOException {
		{
			String responseString = "ok";
			
			return responseString;
		}
	}
	
	public static String apiPostEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		{
			return apiPostEntity(postUrl, entityString, authToken, null);
		}
	}
	
}
