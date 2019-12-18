package life.genny.utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

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

	public BaseEntity addCapabilityToRole(BaseEntity role, final String capabilityCode, final CapabilityMode mode)
	{
	 	/*  Construct answer with Source, Target, Attribute Code, Value */
 		Answer answer = new Answer(beUtils.getServiceToken().getUserCode(), role.getCode() ,"PRM_"+capabilityCode, mode.toString());
 		Attribute capabilityAttribute = RulesUtils.getAttribute("PRM_"+capabilityCode,beUtils.getServiceToken().getToken());
 		answer.setAttribute(capabilityAttribute);
 		beUtils.saveAnswer(answer);

		return role;
	}
	
	public void process()
	{
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
				QwandaUtils.apiDelete(
					GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/attributes/" + toBeRemovedCapability.getCode(),
					beUtils.getServiceToken().getToken());
			}
			/* update all the roles that use this attribute by reloading them into cache */
			QDataBaseEntityMessage rolesMsg = VertxUtils.getObject(beUtils.getServiceToken().getRealm(), "ROLES", beUtils.getServiceToken().getRealm(),
					QDataBaseEntityMessage.class);
			if (rolesMsg != null) {

				for (BaseEntity role : rolesMsg.getItems()) {
					role.removeAttribute(toBeRemovedCapability.getCode());
					/* Now update the db role to only have the attributes we want left */
					if (!VertxUtils.cachedEnabled) { // only post if not in junit	
						QwandaUtils.apiPutEntity(GennySettings.qwandaServiceUrl  + "/qwanda/baseentitys/force",
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
