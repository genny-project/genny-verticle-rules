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
import org.apache.http.client.ClientProtocolException;
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
		JsonObject json = readCachedJson(realm,keyPrefix + ":" + key, token);
		if (json.getString("status").equalsIgnoreCase("ok")) {
		  String data = json.getString("value");
		  item = (T) JsonUtils.fromJson(data, clazz);
          return item;
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
		JsonObject json = readCachedJson(realm,keyPrefix + ":" + key, token);
		if (json.getString("status").equalsIgnoreCase("ok")) {
		  String data = json.getString("value");
          item = (T) JsonUtils.fromJson(data, clazz);
          return item;
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
		writeCachedJson(realm ,keyPrefix + ":" + key, data, token);
	}

	static public JsonObject readCachedJson(final String realm, final String key) {
		return readCachedJson(realm, key, DEFAULT_TOKEN);
	}

	static public JsonObject readCachedJson(String realm, final String key, final String token) {
		JsonObject result = null;
		
		if (!GennySettings.forceCacheApi) {
			String ret = null;
			try {
				//log.info("VERTX READING DIRECTLY FROM CACHE! USING "+(GennySettings.isCacheServer?" LOCAL DDT":"CLIENT "));
				ret = (String) cacheInterface.readCache(realm, key, token);
			} catch (Exception e) {
	                log.error("Cache is  null");
	                e.printStackTrace();
	            }
			if (ret != null) {
				result = new JsonObject().put("status", "ok").put("value", ret);
			} else { 
				result = new JsonObject().put("status", "error").put("value", ret);
			}
		} else {
			String resultStr = null;
			try {
				//log.info("VERTX READING  FROM CACHE API!");
				resultStr = QwandaUtils.apiGet(GennySettings.ddtUrl + "/read/"+realm+"/" + key, token);
				if (resultStr != null) {
					result = new JsonObject(resultStr);
				} else {
					result = new JsonObject().put("status", "error");
				}

			} catch (IOException e) {
				log.error("Could not read "+key+" from cache");
			}

		}

		return result;
	}

	static public JsonObject writeCachedJson(final String realm, final String key, final String value) {
		return writeCachedJson(realm, key, value, DEFAULT_TOKEN);
	}

	static public JsonObject writeCachedJson(final String realm, final String key, final String value, final String token) {
	  return writeCachedJson(realm, key, value, token, 0L);
	}
	
	static public JsonObject writeCachedJson(String realm, final String key, String value, final String token, long ttl_seconds) {
		if (!GennySettings.forceCacheApi) {
			//log.debug("WRITING USING "+(GennySettings.isCacheServer?" LOCAL DDT":"CLIENT ")+"  "+key);
			if ("genny".equals(realm)) {
				realm = GennySettings.mainrealm;
			}
			cacheInterface.writeCache(realm, key, value, token,ttl_seconds);
		} else {
			try {
				log.debug("WRITING TO CACHE USING API! "+key);
				JsonObject json = new JsonObject();
		        json.put("key", key);
		        json.put("json", value);
		        QwandaUtils.apiPostEntity(GennySettings.ddtUrl + "/write", json.toString(), token);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JsonObject ok = new JsonObject().put("status", "ok");
		return ok;

}
	
	static public void clearDDT(String realm)
	{
		if ("genny".equals(realm)) {
			realm = GennySettings.mainrealm;
		}

		cacheInterface.clear(realm);
	}

	static public BaseEntity readFromDDT(String realm, final String code, final boolean withAttributes, final String token) {
		BaseEntity be = null;

		if ("genny".equals(realm)) {
			realm = GennySettings.mainrealm;
		}

		JsonObject json = readCachedJson(realm, code,token);

		if ("ok".equals(json.getString("status"))) {
		    be = JsonUtils.fromJson(json.getString("value"), BaseEntity.class);
			if (be != null && be.getCode()==null) {
				log.error("readFromDDT baseEntity for realm "+realm+" has null code! json is ["+json.getString("value")+"]");
			}
		} else {
			// fetch normally
			log.info("Cache MISS for " + code+" with attributes in realm  "+realm);
			try {
				if (withAttributes) {
					be = QwandaUtils.getBaseEntityByCodeWithAttributes(code, token);
				} else {
					be = QwandaUtils.getBaseEntityByCode(code, token);
				}
			} catch (Exception e) {
				// Okay, this is bad. Usually the code is not in the database but in keycloak
				// So lets leave it to the rules to sort out... (new user)
				log.error("BE " + code + " for realm "+realm+" is NOT IN CACHE OR DB " + e.getLocalizedMessage());
				return null;

			}
             writeCachedJson(realm, code, JsonUtils.toJson(be));

		}
		return be;
	}

	static boolean cacheDisabled = System.getenv("NO_CACHE") != null ? true : false;

	static public BaseEntity readFromDDT(final String realm, final String code, final String token) {
		// if ("PER_SHARONCROW66_AT_GMAILCOM".equals(code)) {
		// log.info("DEBUG");
		// }

		return readFromDDT(realm, code, true,token);

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
		localMessageProducerCache.put(sessionState, toSessionChannel);

	}

	public static MessageProducer<JsonObject> getMessageProducer(String sessionState) {

		return localMessageProducerCache.get(sessionState);

	}
	
	static public void publish(BaseEntity user, String channel, Object payload) {

		publish(user,channel,payload,DEFAULT_FILTER_ARRAY);
	}

	
	static public JsonObject publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes) {
		
			eb.publish(user, channel, payload, filterAttributes);

		JsonObject ok = new JsonObject().put("status", "ok");
		return ok;

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
	
	public static Set<String> fetchRealmsFromApi()
	{
		List<String> activeRealms = new ArrayList<String>();
		JsonObject ar = VertxUtils.readCachedJson(GennySettings.GENNY_REALM, "REALMS");
		String ars = ar.getString("value");

		if (ars == null) {
			try {
				ars = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/utils/realms", "NOTREQUIRED");
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		ars = ars.replaceAll("\\\"", "\"");
		activeRealms = JsonUtils.fromJson(ars, listType);
		Set<String> realms = new HashSet<>(activeRealms);
		return realms;
	}

	
	
}
