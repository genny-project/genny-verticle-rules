package life.genny.utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Transient;

import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;

import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.Allowed;
import life.genny.qwanda.datatype.CapabilityMode;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;

public class CapabilityUtils implements Serializable {
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// Capability Attribute Prefix
	public static final String CAP_MODE_PREFIX = "PRM_";


	@Expose
	List<Attribute> capabilityManifest = new ArrayList<Attribute>();

	@Transient
	private BaseEntityUtils beUtils;

	public CapabilityUtils(BaseEntityUtils beUtils) {
		this.beUtils = beUtils;
	}

	@Deprecated
	public BaseEntity inheritRole(BaseEntity role, final BaseEntity parentRole)
	{
		BaseEntity ret = role;
		List<EntityAttribute> perms = parentRole.findPrefixEntityAttributes("PRM_");
		for (EntityAttribute permissionEA : perms) {
			Attribute permission = permissionEA.getAttribute();
			CapabilityMode mode = CapabilityMode.getMode(permissionEA.getValue());
			ret = addCapabilityToRole(ret,permission.getCode(),mode);
		}
		return ret;
	}

	@Deprecated
	public Attribute addCapability(final String capabilityCode, final String name) {
		String fullCapabilityCode = "PRM_" + capabilityCode.toUpperCase();
		log.debug("Setting Capability : " + fullCapabilityCode + " : " + name);
		Attribute attribute = RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).get(fullCapabilityCode);
		if (attribute != null) {
			capabilityManifest.add(attribute);
			return attribute;
		} else {
			// create new attribute
			attribute = new AttributeText(fullCapabilityCode, name);
			// save to database and cache

			try {
				beUtils.saveAttribute(attribute, beUtils.getServiceToken());
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

	@Deprecated
	public BaseEntity addCapabilityToRole(BaseEntity role, final String capabilityCode, final CapabilityMode mode) {
		// Check if the userToken is allowed to do this!

		if (!hasCapability(capabilityCode,mode)) {
			log.error(beUtils.getGennyToken().getUserCode()+" is NOT ALLOWED TO ADD THIS CAPABILITY TO A ROLE :"+role.getCode());
			return role;
		}

		// TODO Ugly hack fix
		String cCode = capabilityCode;
		if (!capabilityCode.startsWith("PRM_")) {
			cCode = "PRM_"+capabilityCode;

		}

		/* Construct answer with Source, Target, Attribute Code, Value */
		Answer answer = new Answer(beUtils.getServiceToken().getUserCode(), role.getCode(), cCode,
				mode.toString());
		Attribute capabilityAttribute = RulesUtils.getAttribute(cCode,
				beUtils.getServiceToken());
		answer.setAttribute(capabilityAttribute);
		role = beUtils.saveAnswer(answer);

		// Now update the list of roles associated with the key
		switch (mode) {

		case NONE: updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.NONE);
					break;
		case VIEW:
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.NONE);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.VIEW);
			break;
		case EDIT:
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.NONE);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.VIEW);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.EDIT);
			break;
		case ADD:
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.NONE);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.VIEW);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.EDIT);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.ADD);
			break;
		case DELETE:
		case SELF:
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.NONE);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.VIEW);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.EDIT);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.ADD);
			updateCachedRoleSet(role.getCode(), cCode, CapabilityMode.DELETE);
			break;
		}
		return role;
	}


	/**
	 * @param role
	 * @param capabilityCode
	 * @param mode
	 */
	private void updateCachedRoleSet(final String roleCode, final String capabilityCode, final String valueString) {
		// Use the roleCode and capability code as a key, with the permitted modes as values
		String key = beUtils.getGennyToken().getRealm() + ":" + roleCode + ":" + capabilityCode;
		log.info("updateCachedRoleSet test:: " + key);
		// Look up from cache
		JsonObject json = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
				beUtils.getGennyToken());
		String roleCodesString = null;
		// if no cache then create

		if ("error".equals(json.getString("status"))) {
			roleCodesString = "";
		} else {
			roleCodesString = json.getString("value");
		}
		String[] roleCodes = roleCodesString.split(",");
		Set<String> roleCodeSet = new HashSet<>(Arrays. asList(roleCodes));
		if (!roleCodeSet.contains(roleCode)) {
			roleCodesString += roleCode+",";
			VertxUtils.writeCachedJson(beUtils.getGennyToken().getRealm(), key, roleCodesString,
					beUtils.getGennyToken());
		}
	}

	/**
	 * @param role
	 * @param capabilityCode
	 * @param mode
	 */
	private void updateCachedRoleSet(final String roleCode, final String capabilityCode, final CapabilityMode mode) {
		String key = beUtils.getGennyToken().getRealm() + ":" + capabilityCode + ":" + mode.name();
		log.debug("updateCachedRoleSet test:: " + key);
		// Look up from cache
		JsonObject json = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
				beUtils.getGennyToken());
		String roleCodesString = null;
		// if no cache then create

		if ("error".equals(json.getString("status"))) {
			roleCodesString = "";
		} else {
			roleCodesString = json.getString("value");
		}
		String[] roleCodes = roleCodesString.split(",");
		Set<String> roleCodeSet = new HashSet<>(Arrays. asList(roleCodes));
		if (!roleCodeSet.contains(roleCode)) {
			roleCodesString += roleCode+",";
			VertxUtils.writeCachedJson(beUtils.getGennyToken().getRealm(), key, roleCodesString,
					beUtils.getGennyToken());
		}
	}

	/**
	 * Go through a list of capability modes and check that the token can manipulate the modes for the provided capabilityCode
	 * @param capabilityCode capabilityCode to check against
	 * @param modes array of modes to check against
	 * @return whether or not the token can manipulate the supplied modes for the supplied capabilityCode
	 */
	@Deprecated
	public boolean hasCapability(final String capabilityCode, final CapabilityMode... modes) {
		for(CapabilityMode mode : modes) {
			if(!hasCapability(capabilityCode, mode))
				return false;
		}

		return true;
	}

	@Deprecated
	public boolean hasCapability(final String capabilityCode, final CapabilityMode mode) {
		// allow keycloak admin and devcs to do anything
		if (beUtils.getGennyToken().hasRole("admin")||beUtils.getGennyToken().hasRole("dev")||("service".equals(beUtils.getGennyToken().getUsername()))) {
			return true;
		}
		// Create a capabilityCode and mode combined unique key
		String key = beUtils.getGennyToken().getRealm() + ":" + capabilityCode + ":" + mode.name();
		// Look up from cache
		JsonObject json = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
				beUtils.getGennyToken());
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

	// NOTE: This is temporary until ROL_ attributes are properly in place
	@Deprecated
	public boolean hasCapabilityThroughPriIs(String capabilityCode, final CapabilityMode mode) {String cCode = capabilityCode;
		if (!capabilityCode.startsWith("PRM_")) {
			capabilityCode = "PRM_"+capabilityCode;
		}
		// allow keycloak admin and devcs to do anything
		log.debug("hasCapabilityThroughPriIs capabilityCode:: " + capabilityCode + "CapabilityMode" + mode.name());
		
		if (beUtils.getGennyToken().hasRole("admin")||beUtils.getGennyToken().hasRole("dev")||("service".equals(beUtils.getGennyToken().getUsername()))) {
			log.debug("hasCapabilityThroughPriIs:: Inside IF " );
			return true;
		}
		// Create a capabilityCode and mode combined unique key
		String key = beUtils.getGennyToken().getRealm() + ":" + capabilityCode + ":" + mode.name();
		log.info("hasCapabilityThroughPriIs test:: " + key);

		// Look up from cache
		JsonObject json = VertxUtils.readCachedJson(beUtils.getGennyToken().getRealm(), key,
				beUtils.getGennyToken());
		log.info("hasCapabilityThroughPriIs:: Json " + json.toString());
		// check if the user has any of these roles
		String userCode = beUtils.getGennyToken().getUserCode();
		log.info("hasCapabilityThroughPriIs:: userCode " + userCode);

		BaseEntity user = beUtils.getBaseEntityByCode(userCode);
		// if no cache then return false
		BaseEntity defBe = beUtils.getDEF(user);
		log.info("hasCapabilityThroughPriIs:: defBe.getCode " + defBe.getCode());

		//// "HACK HACK HACK"
		log.info("hasCapabilityThroughPriIs:: json.getString " + json.getString("status"));
		if ("error".equals(json.getString("status")) && "DEF_AGENT".equals(defBe.getCode()) ) {
			log.info("hasCapabilityThroughPriIs:: Inside IF " );
			if ("SBE_AVAILABLE_INTERNS".equals(capabilityCode)) {
				log.info("hasCapabilityThroughPriIs:: Inside IF ::" + capabilityCode );
				if ("DEF_INTERN".equals(defBe.getCode()) || "DEF_HOST_CPY_REP".equals(defBe.getCode()) || "DEF_EDU_PROV_REP".equals(defBe.getCode())) {
					log.info("hasCapabilityThroughPriIs:: Inside IF ::" + defBe.getCode());
					log.info("hasCapabilityThroughPriIs:: return false");
					return false ; // don't let these ones see it
				}
			}
			log.info("hasCapabilityThroughPriIs:: return true");
			return true; // TODO ACC THIS IS BAD.
			//return false;
			/// END HACK


		}

		// else get the list of roles associated with the key
		String roleCodesString = json.getString("value");
		if (roleCodesString == null)  {
			log.info("hasCapabilityThroughPriIs:: return false");
			return false;
		}

		String roleCodes[] = roleCodesString.split(",");
		log.info("hasCapabilityThroughPriIs:: roleCodesString " + roleCodesString);

		for (String roleCode : roleCodes) {
			log.info("hasCapabilityThroughPriIs:: roleCode " + roleCode);
			String priIsCode = "PRI_IS_" + roleCode.split("ROL_")[1];
			log.info("hasCapabilityThroughPriIs:: priIsCode " + priIsCode);
			if (user.getBaseEntityAttributes().parallelStream()
					.anyMatch(ti -> ti.getAttributeCode().equals(priIsCode))) {
						log.info("hasCapabilityThroughPriIs:: return true");
				return true;
			}
		}
		log.info("hasCapabilityThroughPriIs:: return false");
		return false;
	}

	@Deprecated
	public void process() {
		List<Attribute> existingCapability = new ArrayList<Attribute>();

		for (String existingAttributeCode : RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).keySet()) {
			if (existingAttributeCode.startsWith("PRM_")) {
				existingCapability.add(RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).get(existingAttributeCode));
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
				RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).remove(toBeRemovedCapability.getCode()); // remove from cache
				if (!VertxUtils.cachedEnabled) { // only post if not in junit
					log.debug("Another API attributes delete!!! "+toBeRemovedCapability.getCode());
					QwandaUtils.apiDelete(GennySettings.fyodorServiceUrl + "/attributes/"+beUtils.getServiceToken().getRealm()+"/"
							+ toBeRemovedCapability.getCode(), beUtils.getServiceToken());
				//	QwandaUtils.apiDelete(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/attributes/"
				//			+ toBeRemovedCapability.getCode(), beUtils.getServiceToken());
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
									JsonUtils.toJson(role), beUtils.getServiceToken());
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

	@Override
	public String toString() {
		return "CapabilityUtils [" + (capabilityManifest != null ? "capabilityManifest=" + capabilityManifest : "")
				+ "]";
	}

	/**
	 * @param userToken
	 * @param user
	 * @return
	 */
	@Deprecated
	static public List<Allowed> generateAlloweds(GennyToken userToken, BaseEntity user) {
		List<EntityAttribute> roles = user.findPrefixEntityAttributes("PRI_IS_");
		List<Allowed> allowable = new CopyOnWriteArrayList<Allowed>();
		for (EntityAttribute role : roles) { // should store in cached map
			Boolean value = false;
			if (role.getValue() instanceof Boolean) {
				value = role.getValue();
			} else {
				if (role.getValue() instanceof String) {
					value = "TRUE".equalsIgnoreCase(role.getValue());
//						log.info(callingWorkflow + " Running rule flow group " + ruleFlowGroup + " #2.5 role value = "
//								+ role.getValue());
				} else {
//						log.info(callingWorkflow + " Running rule flow group " + ruleFlowGroup + " #2.6 role value = "
//								+ role.getValue());
				}
			}
			if (value) {
				String roleBeCode = "ROL_" + role.getAttributeCode().substring("PRI_IS_".length());
				BaseEntity roleBE = VertxUtils.readFromDDT(userToken.getRealm(), roleBeCode, userToken);
				if (roleBE == null) {
					continue;
				}
				// Add the actual role to capabilities
				allowable.add(
						new Allowed(role.getAttributeCode().substring("PRI_IS_".length()), CapabilityMode.VIEW));
//					log.info(callingWorkflow + " got to here before capabilities");
				List<EntityAttribute> capabilities = roleBE.findPrefixEntityAttributes("PRM_");
				for (EntityAttribute ea : capabilities) {
					String modeString = null;
					Boolean ignore = false;
					try {
						Object val = ea.getValue();
						if (val instanceof Boolean) {
							log.error("capability attributeCode=" + ea.getAttributeCode() + " is BOOLEAN??????");
							ignore = true;
						} else {
							modeString = ea.getValue();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (!ignore) {
						CapabilityMode mode = CapabilityMode.getMode(modeString);
						// This is my cunning switch statement that takes into consideration the
						// priority order of the modes... (note, no breaks and it relies upon the fall
						// through)
						switch (mode) {
						case DELETE:
							allowable.add(new Allowed(ea.getAttributeCode().substring(4), CapabilityMode.DELETE));
						case ADD:
							allowable.add(new Allowed(ea.getAttributeCode().substring(4), CapabilityMode.ADD));
						case EDIT:
							allowable.add(new Allowed(ea.getAttributeCode().substring(4), CapabilityMode.EDIT));
						case VIEW:
							allowable.add(new Allowed(ea.getAttributeCode().substring(4), CapabilityMode.VIEW));
						case NONE:
							allowable.add(new Allowed(ea.getAttributeCode().substring(4), CapabilityMode.NONE));
						}
					}

				}
			}
		}

		/* now force the keycloak ones */
		for (String role : userToken.getUserRoles()) {
			allowable.add(
					new Allowed(role.toUpperCase(), CapabilityMode.VIEW));
		}

		return allowable;
	}

	public void recursivelyCheckSidebarAskForCapability(Ask ask) {

		ArrayList<Ask> askList = new ArrayList<>();

		for (Ask childAsk : ask.getChildAsks()) {
			String code = "SIDEBAR_" + childAsk.getQuestionCode();

			if (hasCapabilityThroughPriIs(code, CapabilityMode.VIEW)) {

				recursivelyCheckSidebarAskForCapability(childAsk);
				askList.add(childAsk);
			}
		}

		Ask[] items = askList.toArray(new Ask[askList.size()]);
		ask.setChildAsks(items);
	}
}
