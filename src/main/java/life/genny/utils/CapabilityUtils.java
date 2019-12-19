package life.genny.utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.datatype.CapabilityMode;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;

public class CapabilityUtils implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	List<Attribute> capabilityManifest = new ArrayList<Attribute>();

	private BaseEntityUtils beUtils;

	public CapabilityUtils(BaseEntityUtils beUtils) {
		this.beUtils = beUtils;
	}

	public Attribute addCapability(final String capabilityCode, final String name) {
		String fullCapabilityCode = "PRM_" + capabilityCode.toUpperCase();
		log.info("Setting Capability : " + fullCapabilityCode + " : " + name);
		Attribute attribute = RulesUtils.attributeMap.get(fullCapabilityCode);
		if (attribute != null) {
			capabilityManifest.add(attribute);
			return attribute;
		} else {
			// create new attribute
			attribute = new AttributeBoolean(fullCapabilityCode, name);
			// save to database and cache

			try {
				beUtils.saveAttribute(attribute, beUtils.getServiceToken().getToken());
				// no roles would have this attribute yet
				// return
				capabilityManifest.add(attribute);
				return attribute;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}
	}

	public BaseEntity addCapabilityToRole(BaseEntity role, final String capabilityCode, final CapabilityMode mode) {
		// Check if the userToken is allowed to do this!

		if (!hasCapability(capabilityCode,mode)) {
			log.error(beUtils.getGennyToken().getUserCode()+" is NOT ALLOWED TO ADD THIS CAPABILITY TO A ROLE :"+role.getCode());
			return role;
		}
		/* Construct answer with Source, Target, Attribute Code, Value */
		Answer answer = new Answer(beUtils.getServiceToken().getUserCode(), role.getCode(), "PRM_" + capabilityCode,
				mode.toString());
		Attribute capabilityAttribute = RulesUtils.getAttribute("PRM_" + capabilityCode,
				beUtils.getServiceToken().getToken());
		answer.setAttribute(capabilityAttribute);
		beUtils.saveAnswer(answer);

		// Now update the list of roles associated with the key
		String key = beUtils.getGennyToken().getRealm() + ":" + capabilityCode + ":" + mode.name();
		// Look up from cache
		JsonObject json = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
				beUtils.getGennyToken().getToken());
		String roleCodesString = null;
		// if no cache then create
		
		if ("error".equals(json.getString("status"))) {
			roleCodesString = "";
		} else {
			roleCodesString = json.getString("value");
		}
		String[] roleCodes = roleCodesString.split(",");
		Set<String> roleCodeSet = new HashSet<>(Arrays. asList(roleCodes));
		if (!roleCodeSet.contains(key)) {
			
		}

		return role;
	}

	public boolean hasCapability(final String capabilityCode, final CapabilityMode mode) {
		// allow keycloak admin and devcs to do anything
		if (beUtils.getGennyToken().hasRole("admin")||beUtils.getGennyToken().hasRole("dev")||("PER_SERVICE".equals(beUtils.getGennyToken().getUserCode()))) {
			return true;
		}
		// Create a capabilityCode and mode combined unique key
		String key = beUtils.getGennyToken().getRealm() + ":" + capabilityCode + ":" + mode.name();
		// Look up from cache
		JsonObject json = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
				beUtils.getGennyToken().getToken());
		// if no cache then return false
		if ("error".equals(json.getString("status"))) {
			return false;
		}

		// else get the list of roles associated with the key
		String roleCodesString = json.getString("value");
		String roleCodes[] = roleCodesString.split(",");

		// check if the user has any of these roles
		String userCode = beUtils.getGennyToken().getUserCode();
		BaseEntity user = beUtils.getBaseEntityByCode(userCode);
		for (String roleCode : roleCodes) {
			if (user.getBaseEntityAttributes().parallelStream()
					.anyMatch(ti -> ti.getAttributeCode().equals(roleCode))) {
				return true;
			}
		}

		return false;
	}

	public void process() {
		List<Attribute> existingCapability = new ArrayList<Attribute>();

		for (String existingAttributeCode : RulesUtils.attributeMap.keySet()) {
			if (existingAttributeCode.startsWith("PRM_")) {
				existingCapability.add(RulesUtils.attributeMap.get(existingAttributeCode));
			}
		}

		/* Remove any capabilities not in this forced list from roles */
		existingCapability.removeAll(getCapabilityManifest());

		/*
		 * for every capability that exists that is not in the manifest , find all
		 * entityAttributes
		 */
		for (Attribute toBeRemovedCapability : existingCapability) {
			try {
				RulesUtils.attributeMap.remove(toBeRemovedCapability.getCode()); // remove from cache
				if (!VertxUtils.cachedEnabled) { // only post if not in junit
					QwandaUtils.apiDelete(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/attributes/"
							+ toBeRemovedCapability.getCode(), beUtils.getServiceToken().getToken());
				}
				/* update all the roles that use this attribute by reloading them into cache */
				QDataBaseEntityMessage rolesMsg = VertxUtils.getObject(beUtils.getServiceToken().getRealm(), "ROLES",
						beUtils.getServiceToken().getRealm(), QDataBaseEntityMessage.class);
				if (rolesMsg != null) {

					for (BaseEntity role : rolesMsg.getItems()) {
						role.removeAttribute(toBeRemovedCapability.getCode());
						/* Now update the db role to only have the attributes we want left */
						if (!VertxUtils.cachedEnabled) { // only post if not in junit
							QwandaUtils.apiPutEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/force",
									JsonUtils.toJson(role), beUtils.getServiceToken().getToken());
						}

					}
				}

			} catch (IOException e) {
				/* TODO Auto-generated catch block */
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the beUtils
	 */
	public BaseEntityUtils getBeUtils() {
		return beUtils;
	}

	/**
	 * @return the capabilityManifest
	 */
	public List<Attribute> getCapabilityManifest() {
		return capabilityManifest;
	}

	/**
	 * @param capabilityManifest the capabilityManifest to set
	 */
	public void setCapabilityManifest(List<Attribute> capabilityManifest) {
		this.capabilityManifest = capabilityManifest;
	}

}
