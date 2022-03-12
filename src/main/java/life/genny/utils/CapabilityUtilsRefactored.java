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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.Allowed;
import life.genny.qwanda.datatype.CapabilityMode;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;

public class CapabilityUtilsRefactored implements Serializable {
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

	public CapabilityUtilsRefactored(BaseEntityUtils beUtils) {
		this.beUtils = beUtils;
	}

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

	public Attribute addCapability(final String capabilityCode, final String name) {
		String fullCapabilityCode = "PRM_" + capabilityCode.toUpperCase();
		log.info("Setting Capability : " + fullCapabilityCode + " : " + name);
		Attribute attribute = RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).get(fullCapabilityCode);
		if (attribute != null) {
			capabilityManifest.add(attribute);
			return attribute;
		} else {
			// create new attribute
			attribute = new AttributeText(fullCapabilityCode, name);
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

	public BaseEntity addCapabilityToRole(BaseEntity role, final String rawCapabilityCode, final CapabilityMode... modes) {
		// Ensure the capability is well defined
		String cleanCapabilityCode = cleanCapabilityCode(rawCapabilityCode);

		// Check the user token has required capabilities
		if (!hasCapability(cleanCapabilityCode,modes)) {
			log.error(beUtils.getGennyToken().getUserCode() + " is NOT ALLOWED TO ADD CAP: " + cleanCapabilityCode + " TO ROLE: " + role.getCode());
			return role;
		}


		updateCachedRoleSet(role.getCode(), cleanCapabilityCode, modes);


		return null;
	}

	/**
	 * @param role
	 * @param capabilityCode
	 * @param mode
	 */
	private JsonObject updateCachedRoleSet(final String roleCode, final String cleanCapabilityCode, final CapabilityMode... modes) {
		GennyToken token = beUtils.getGennyToken();
		String key = getCacheKey(token.getRealm(), roleCode, cleanCapabilityCode);
		String modesString = getModeString(modes);
		
		log.info("updateCachedRoleSet test:: " + key);
		// if no cache then create
		return VertxUtils.writeCachedJson(token.getRealm(), key, modesString, token.getToken());
	}

	/**
	 * Go through a list of capability modes and check that the token can manipulate the modes for the provided capabilityCode
	 * @param capabilityCode capabilityCode to check against
	 * @param modes array of modes to check against
	 * @return whether or not the token can manipulate the supplied modes for the supplied capabilityCode
	 */
	public boolean hasCapability(final String capabilityCode, final CapabilityMode... modes) {
		for(CapabilityMode mode : modes) {
			if(!hasCapability(capabilityCode, mode))
				return false;
		}

		return true;
	}

	public boolean hasCapability(final String capabilityCode, final CapabilityMode mode) {
		// allow keycloak admin and devcs to do anything
		if (beUtils.getGennyToken().hasRole("admin")||beUtils.getGennyToken().hasRole("dev")||("service".equals(beUtils.getGennyToken().getUsername()))) {
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
				BaseEntity roleBE = VertxUtils.readFromDDT(userToken.getRealm(), roleBeCode, userToken.getToken());
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

	private static String getModeString(CapabilityMode... modes) {
		String modeString = "[";
		for(CapabilityMode mode : modes) {
			modeString += "\"" + mode.name() + "\"" + ",";
		}

		return modeString.substring(0, modeString.length() - 1) + "]";
	}

	private static CapabilityMode[] getCapModesFromString(String modeString) {
		JsonArray array = new JsonArray(modeString);
		CapabilityMode[] modes = new CapabilityMode[array.size()];

		for(int i = 0; i < array.size(); i++) {
			modes[i] = CapabilityMode.valueOf(array.getString(i));
		}

		return modes;
	}

	private static String cleanCapabilityCode(final String rawCapabilityCode) {
		String cleanCapabilityCode = rawCapabilityCode.toUpperCase();
		if(!cleanCapabilityCode.startsWith(CAP_MODE_PREFIX)) {
			cleanCapabilityCode = CAP_MODE_PREFIX + cleanCapabilityCode;
		}

		String[] components = cleanCapabilityCode.split("_");
		// Should be of the form PRM_<OWN/OTHER>_<CODE>
		/* 
		* 1. PRM
		* 2. OWN or OTHER
		* 3. CODE
		*/
		if(components.length < 3) {
			log.warn("Capability Code: " + rawCapabilityCode + " missing OWN/OTHER declaration.");
		} else {
			Boolean affectsOwn = "OWN".equals(components[1]);
			Boolean affectsOther = "OTHER".equals(components[1]);

			if(!affectsOwn && !affectsOther) {
				log.warn("Capability Code: " + rawCapabilityCode + " has malformed OWN/OTHER declaration.");
			}
		}

		return cleanCapabilityCode;
	}

	private static String getCacheKey(String realm, String roleCode, String capCode) {
		return realm + ":" + roleCode + ":" + capCode;
	}
}
