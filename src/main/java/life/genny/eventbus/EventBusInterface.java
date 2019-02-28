package life.genny.eventbus;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.JsonUtils;

public interface EventBusInterface {
	static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static Object privacyFilter(BaseEntity user, Object payload, final String[] filterAttributes) {
		if (payload instanceof QDataBaseEntityMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QDataBaseEntityMessage) payload,
					new HashMap<String, BaseEntity>(), filterAttributes));
		} else if (payload instanceof QBulkMessage) {
			return JsonUtils.toJson(privacyFilter(user, (QBulkMessage) payload, filterAttributes));
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

	public void publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes);
}
