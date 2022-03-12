package life.genny.utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Transient;

import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.Allowed;
import life.genny.qwanda.datatype.AllowedSafe;
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
	public static final String ROLE_BE_PREFIX = "ROL_";

	public static final String LNK_ROLE_CODE = "LNK_ROLE";


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
		List<EntityAttribute> perms = parentRole.findPrefixEntityAttributes(CAP_MODE_PREFIX);
		for (EntityAttribute permissionEA : perms) {
			Attribute permission = permissionEA.getAttribute();
			CapabilityMode[] modes = getCapModesFromString(permissionEA.getValue());
			ret = addCapabilityToRole(ret,permission.getCode(),modes);
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
		return role;
	}

	private CapabilityMode[] getCapabilitiesFromCache(final String roleCode, final String cleanCapabilityCode) {
		GennyToken token = beUtils.getGennyToken();
		String key = getCacheKey(token.getRealm(), roleCode, cleanCapabilityCode);
		JsonObject object = VertxUtils.readCachedJson(token.getRealm(), key);
		if("error".equals(object.getString("status"))) {
			log.error("Error reading cache for realm: " + token.getRealm() + " with key: " + key);
			return null;
		}

		String modeString = object.getString("value");
		return getCapModesFromString(modeString);
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
	 * @return whether or not the token can manipulate all the supplied modes for the supplied capabilityCode
	 */
	public boolean hasCapability(final String rawCapabilityCode, final CapabilityMode... checkModes) {
		// allow keycloak admin and devcs to do anything
		if (beUtils.getGennyToken().hasRole("admin")||beUtils.getGennyToken().hasRole("dev")||("service".equals(beUtils.getGennyToken().getUsername()))) {
			return true;
		}
		final String cleanCapabilityCode = cleanCapabilityCode(rawCapabilityCode);
		BaseEntity user = beUtils.getUserBaseEntity();

		// Look through all the user entity attributes for the capability code. If it is there check the cache with the roleCode as the userCode
		// TODO: Will need to revisit this implementation with Jasper
		
		Optional<EntityAttribute> lnkRole = user.findEntityAttribute(LNK_ROLE_CODE);
		
		// Make a list for the modes that have been found in the user's various roles
		// TODO: Potentially change this to a system that matches from multiple roles instead of a single role
		// List<CapabilityMode> foundModes = new ArrayList<>();

		if(lnkRole.isPresent()) {
			String rolesValue = lnkRole.get().getValueString();
			try {
				// Look through cache using each role
				JsonArray roleArray = new JsonArray(rolesValue);
				for(int i = 0; i < roleArray.size(); i++) {
					String roleCode = roleArray.getString(i);
					
					CapabilityMode[] modes = getCapabilitiesFromCache(roleCode, cleanCapabilityCode);
					List<CapabilityMode> modeList = Arrays.asList(modes);
					for(CapabilityMode checkMode : checkModes) {
						if(!modeList.contains(checkMode))
							return false;
					}
				}

			// There is a malformed LNK_ROLE Attribute, so we assume they don't have the capability
			} catch(DecodeException exception) {
				log.error("Error decoding LNK_ROLE for BaseEntity: " + user.getCode());
				log.error("Value: " + rolesValue + ". Expected: a json array of roles");
				return false;
			}
		}

		// TODO: Implement user checking
		// Set<EntityAttribute> entityAttributes = user.getBaseEntityAttributes();
		// for(EntityAttribute eAttribute : entityAttributes) {

		// }
		
		// Since we are iterating through an array of modes to check, the above impl will have returned false if any of them were missing
		return true;
	}

	public void process() {
		List<Attribute> existingCapability = new ArrayList<Attribute>();

		for (String existingAttributeCode : RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).keySet()) {
			if (existingAttributeCode.startsWith(CAP_MODE_PREFIX)) {
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
	static public List<AllowedSafe> generateAlloweds(GennyToken userToken, BaseEntity user) {
		List<EntityAttribute> roles = user.findPrefixEntityAttributes("PRI_IS_");
		List<AllowedSafe> allowables = new CopyOnWriteArrayList<AllowedSafe>();

		Optional<EntityAttribute> LNK_ROLEOpt = user.findEntityAttribute(LNK_ROLE_CODE);
		if(LNK_ROLEOpt.isPresent()) {
			JsonArray roleCodesArray = new JsonArray(LNK_ROLEOpt.get().getValueString());

			for(int i = 0; i < roleCodesArray.size(); i++) {
				String roleBECode = roleCodesArray.getString(i);

				BaseEntity roleBE = VertxUtils.readFromDDT(userToken.getRealm(), roleBECode, userToken.getToken());
				if(roleBE == null)
					continue;
				
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
						CapabilityMode[] modes = getCapModesFromString(modeString);
						allowables.add(new AllowedSafe(ea.getAttributeCode(), modes));
					}

				}
			}
		}

		/* now force the keycloak ones */
		for (String role : userToken.getUserRoles()) {
			allowables.add(
					new AllowedSafe(role.toUpperCase(), CapabilityMode.VIEW));
		}

		return allowables;
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
