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
import java.util.stream.Collectors;

import javax.persistence.Transient;

import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
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
	public static final String CAP_CODE_PREFIX = "PRM_";
	public static final String ROLE_BE_PREFIX = "ROL_";

	// TODO: Confirm we want DEFs to have capabilities as well
	public static final String[] ACCEPTED_CAP_PREFIXES = {ROLE_BE_PREFIX, "PER_", "DEF_"};

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
		List<EntityAttribute> perms = parentRole.findPrefixEntityAttributes(CAP_CODE_PREFIX);
		for (EntityAttribute permissionEA : perms) {
			Attribute permission = permissionEA.getAttribute();
			CapabilityMode[] modes = getCapModesFromString(permissionEA.getValue());
			ret = addCapabilityToBaseEntity(ret,permission.getCode(),modes);
		}
		return ret;
	}

	public Attribute addCapability(final String rawCapabilityCode, final String name) {
		String cleanCapabilityCode = cleanCapabilityCode(rawCapabilityCode);
		log.info("Setting Capability : " + cleanCapabilityCode + " : " + name);
		Attribute attribute = RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).get(cleanCapabilityCode);
		log.info("preexisting: " + (attribute != null));
		if (attribute != null) {
			capabilityManifest.add(attribute);
			return attribute;
		} else {
			// create new attribute
			attribute = new AttributeText(cleanCapabilityCode, name);
			// save to database and cache

			try {
				beUtils.saveAttribute(attribute, beUtils.getServiceToken().getToken());
				// no roles would have this attribute yet
				// return
				capabilityManifest.add(attribute);
				Attribute newAttrib = RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).get(cleanCapabilityCode);
				log.info("new attrib: " + (newAttrib != null));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return attribute;

		}
	}

	@Deprecated
	/**
	 * Deprecated since 10.0.0. To be removed 10.2.0. Use {@link CapabilityUtilsRefactored#addCapabilityToBaseEntity(BaseEntity, String, boolean, CapabilityMode...)} instead
	 * @param role
	 * @param rawCapabilityCode
	 * @param modes
	 * @return
	 */
	public BaseEntity addCapabilityRole(BaseEntity role, final String rawCapabilityCode, final CapabilityMode... modes) {
		return addCapabilityToBaseEntity(role, rawCapabilityCode, true, modes);
		
	}

	public BaseEntity addCapabilityToBaseEntity(BaseEntity targetBe, final String rawCapabilityCode, final CapabilityMode... modeArray) {
		return addCapabilityToBaseEntity(targetBe, rawCapabilityCode, true, modeArray);
	}

	public BaseEntity addCapabilityToBaseEntity(BaseEntity targetBe, final String rawCapabilityCode, final boolean cascade, final CapabilityMode... modeArray) {
		// Ensure the capability is well defined
		String cleanCapabilityCode = cleanCapabilityCode(rawCapabilityCode);
		// Check the user token has required capabilities
		if (!hasCapability(cleanCapabilityCode,modeArray)) {
			log.error(beUtils.getGennyToken().getUserCode() + " is NOT ALLOWED TO ADD CAP: " + cleanCapabilityCode + " TO BASE ENTITITY: " + targetBe.getCode());
			return targetBe;
		}
		// Convert array modes to a unique set of modes
		Set<CapabilityMode> modes = Arrays.asList(modeArray).stream().collect(Collectors.toSet());
		
		if(cascade) {
			CapabilityMode highestMode = getHighestPriorityCap(modeArray);
			// Add all lesser modes to the mode list
			modes.addAll(CapabilityMode.getLesserModes(highestMode));
		}

		String modeString = getModeString(modes);
		log.info("facts Adding: " + cleanCapabilityCode + " to " + targetBe.getCode() + " modes: " +  modeString);

		/* Construct answer with Source, Target, Attribute Code, Value */
		Answer answer = new Answer(beUtils.getServiceToken().getUserCode(), targetBe.getCode(), cleanCapabilityCode, modeString);
		Attribute capabilityAttribute = RulesUtils.getAttribute(cleanCapabilityCode, beUtils.getServiceToken().getToken());
		answer.setAttribute(capabilityAttribute);
		targetBe = beUtils.saveAnswer(answer);

		String[] attributeCodes = targetBe.getBaseEntityAttributes().stream().map((entityAttribute) -> {
			return entityAttribute.getAttributeCode();
		}).collect(Collectors.toList()).toArray(new String[0]);
		log.info(targetBe.getCode() + " attribs: " + attributeCodes);

		updateCachedRoleSet(targetBe.getCode(), cleanCapabilityCode, modes);
		return targetBe;
	}

	private CapabilityMode[] getCapabilitiesFromCache(final String baseEntityCode, final String cleanCapabilityCode) {

		// Check the base entity is allowed to have capabilities

		GennyToken token = beUtils.getGennyToken();
		String key = getCacheKey(token.getRealm(), baseEntityCode, cleanCapabilityCode);
		JsonObject object = VertxUtils.readCachedJson(token.getRealm(), key);
		if("error".equals(object.getString("status"))) {
			log.error("Error reading cache for realm: " + token.getRealm() + " with key: " + key);
			return null;
		}

		String modeString = object.getString("value");
		return getCapModesFromString(modeString);
	}

	private JsonObject updateCachedRoleSet(final String beCode, final String cleanCapabilityCode, final Set<CapabilityMode> modes) {
		return updateCachedRoleSet(beCode, cleanCapabilityCode, modes.toArray(new CapabilityMode[0]));
	}

	/**
	 * @param role
	 * @param capabilityCode
	 * @param mode
	 */
	private JsonObject updateCachedRoleSet(final String beCode, final String cleanCapabilityCode, final CapabilityMode... modes) {
		GennyToken token = beUtils.getGennyToken();
		String key = getCacheKey(token.getRealm(), beCode, cleanCapabilityCode);
		String modesString = getModeString(modes);
		// if no cache then create
		return VertxUtils.writeCachedJson(token.getRealm(), key, modesString, token.getToken());
	}

	/**
	 * Go through a list of capability modes and check that the token can manipulate the modes for the provided capabilityCode
	 * @param rawCapabilityCode capabilityCode to check against (will be cleaned with clean capability function)
	 * @param modes array of modes to check against
	 * @return whether or not the token can manipulate all the supplied modes for the supplied capabilityCode
	 * 
	 * @see {@link CapabilityMode}
	 * @see {@link CapabilityUtilsRefactored#cleanCapabilityCode(String)}
	 */
	public boolean hasCapability(final String rawCapabilityCode, final CapabilityMode... checkModes) {
		return hasCapability(false, rawCapabilityCode, checkModes);
	}

	/**
	 * Go through a list of CapabilityModes and check that the User BaseEntity has at least one or all (requiresAll depending) 
	 * of the CapabilityModes in the aforementioned list for a given CapabilityCode.
	 * @param requiresAll whether or not to check for every capability mode in the modes array for the capability code (Default <b>true</b>)
	 * @param rawCapabilityCode capabilityCode to check against (will be cleaned with clean capability function)
	 * @param modes array of modes to check against
	 * @return whether or not the token can manipulate all the supplied modes for the supplied capabilityCode
	 * 
	 * @see {@link CapabilityMode}
	 * @see {@link CapabilityUtilsRefactored#cleanCapabilityCode(String)}
	 */
	public boolean hasCapability(boolean requiresAll, final String rawCapabilityCode, final CapabilityMode... checkModes) {
		// allow keycloak admin, service and devs to do anything
		if (beUtils.getGennyToken().hasRole("admin")||beUtils.getGennyToken().hasRole("dev")||(beUtils.tokenIsServiceUser())) {
			log.info("User is admin, dev or service user -> bypassing hasCap check");
			return true;
		}
		final String cleanCapabilityCode = cleanCapabilityCode(rawCapabilityCode);
		BaseEntity user = beUtils.getUserBaseEntity();

		// Look through all the user entity attributes for the capability code. If it is there check the cache with the roleCode as the userCode
		// TODO: Will need to revisit this implementation with Jasper
		
		Optional<EntityAttribute> lnkRole = user.findEntityAttribute(LNK_ROLE_CODE);
		
		// Make a list for the modes that have been found in the user's various roles. To be used if we require all
		Set<CapabilityMode> foundModes = new HashSet<>();
		Set<CapabilityMode> checkModeSet = Arrays.asList(checkModes).stream().collect(Collectors.toSet());

		if(lnkRole.isPresent()) {
			String rolesValue = lnkRole.get().getValueString();
			try {
				// Look through cache using each role
				JsonArray roleArray = new JsonArray(rolesValue);

				// 1. For each role, look for the target capability and check the modes associated with the capability
				// 2. If there is a capability from the modes in the role that is one of the check modes, add it to foundModes
				for(int i = 0; i < roleArray.size(); i++) {
					String roleCode = roleArray.getString(i);
					
					CapabilityMode[] modes = getCapabilitiesFromCache(roleCode, cleanCapabilityCode);
					List<CapabilityMode> modeList = Arrays.asList(modes);
					for(CapabilityMode mode : modeList) {
						boolean modeInCheckSet = checkModeSet.contains(mode);
						if(modeInCheckSet) {
							if(requiresAll) {
								foundModes.add(mode);
							} else {
								// We only require one mode to be found in the role so we can return true here
								log.info("User: " + user.getCode() + " has role " + roleCode + " that has " + cleanCapabilityCode + ":" + mode.name());
								return true;
							}
						}
						// if(!checkModeSet.contains(mode))
						// 	return false;
					}
				}

			// There is a malformed LNK_ROLE Attribute, so we assume they don't have the capability in their roles
			} catch(DecodeException exception) {
				log.error("Error decoding LNK_ROLE for BaseEntity: " + user.getCode());
				log.error("Value: " + rolesValue + ". Expected: a json array of roles");
			}
		}

		Optional<EntityAttribute> targetCapabilityOptional = user.findEntityAttribute(cleanCapabilityCode);
		if(targetCapabilityOptional.isPresent()) {
			CapabilityMode[] modes = getCapModesFromString(targetCapabilityOptional.get().getValueString());
			for(CapabilityMode mode : modes) {
				boolean modeInCheckSet = checkModeSet.contains(mode);
				if(modeInCheckSet) {
					if(requiresAll) {
						foundModes.add(mode);
					} else {
						// We only require one mode to be found in the person so we can return true here
						log.info("User: " + user.getCode() + " has " + cleanCapabilityCode + ":" + mode.name() + " in attributes");
						return true;
					}
				}
			}
		}

		// if we found a mode earlier and we don't require all then we returned true
		// if we require all then we require that the foundModes set is the same as the checkModeSet
		boolean hasAll = foundModes.equals(checkModeSet);
		if(requiresAll && !hasAll) {
			Set<CapabilityMode> intersection = new HashSet<>(foundModes);
			intersection.retainAll(checkModeSet);
			for(CapabilityMode checkMode : checkModeSet) {
				if(!intersection.contains(checkMode))
					log.info("User: " + user.getCode() + " missing CapMode: " + cleanCapabilityCode + ":" + checkMode.name());
			}
		}
		return hasAll;
	}

	public void process() {
		List<Attribute> existingCapability = new ArrayList<Attribute>();

		for (String existingAttributeCode : RulesUtils.realmAttributeMap.get(this.beUtils.getGennyToken().getRealm()).keySet()) {
			if (existingAttributeCode.startsWith(CAP_CODE_PREFIX)) {
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
	 * @param userToken
	 * @param user
	 * @return
	 */
	public static List<AllowedSafe> generateAlloweds(GennyToken userToken, BaseEntity user) {
		List<AllowedSafe> allowables = new CopyOnWriteArrayList<AllowedSafe>();

		// Look for user capabilities
		List<EntityAttribute> capabilities = user.findPrefixEntityAttributes(CAP_CODE_PREFIX);
		for(EntityAttribute capability : capabilities) {
			String modeString = capability.getValueString();
			if(modeString != null) {
				CapabilityMode[] modes = getCapModesFromString(modeString);
				String cleanCapabilityCode = cleanCapabilityCode(capability.getAttributeCode());
				allowables.add(new AllowedSafe(cleanCapabilityCode, modes));
			}
		}

		Optional<EntityAttribute> LNK_ROLEOpt = user.findEntityAttribute(LNK_ROLE_CODE);

		JsonArray roleCodesArray = null;

		if(LNK_ROLEOpt.isPresent()) {
			roleCodesArray = new JsonArray(LNK_ROLEOpt.get().getValueString());
		} else {
			roleCodesArray = new JsonArray("[]");
			log.warn("Could not find " + LNK_ROLE_CODE + " in user: " + user.getCode());
		}
		
		// Add keycloak roles
		// for (String role : userToken.getUserRoles()) {
		// 	roleCodesArray.add(ROLE_BE_PREFIX + role.toUpperCase());
		// }

		for(int i = 0; i < roleCodesArray.size(); i++) {
			String roleBECode = roleCodesArray.getString(i);

			BaseEntity roleBE = VertxUtils.readFromDDT(userToken.getRealm(), roleBECode, userToken.getToken());
			if(roleBE == null) {
				log.warn("facts: could not find roleBe: " + roleBECode + " in cache: " + userToken.getRealm());
				continue;
			}
			
			// Go through all the entity 
			capabilities = roleBE.findPrefixEntityAttributes(CAP_CODE_PREFIX);
			for (EntityAttribute ea : capabilities) {
				String modeString = null;
				Boolean ignore = false;

				String cleanCapabilityCode = cleanCapabilityCode(ea.getAttributeCode());
				try {
					Object val = ea.getValue();
					if (val instanceof Boolean) {
						log.error("capability attributeCode=" + cleanCapabilityCode + " is BOOLEAN??????");
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
					allowables.add(new AllowedSafe(cleanCapabilityCode, modes));
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

	public static CapabilityMode getHighestPriorityCap(final CapabilityMode... modeArray) {
		CapabilityMode highestMode = modeArray[0];
		for(CapabilityMode mode : modeArray) {
			if(mode.greaterThan(highestMode))
				highestMode = mode;
		}

		return highestMode;
	}
	
	public static String getModeString(Set<CapabilityMode> modes) {
		return getModeString(modes.toArray(new CapabilityMode[0]));
	}

	public static String getModeString(CapabilityMode... modes) {
		String modeString = "[";
		for(CapabilityMode mode : modes) {
			modeString += "\"" + mode.name() + "\"" + ",";
		}

		return modeString.substring(0, modeString.length() - 1) + "]";
	}

	public static CapabilityMode[] getCapModesFromString(String modeString) {
		if(!modeString.startsWith("["))
			modeString = "[\"" + modeString;
		if(!modeString.endsWith("]"))
			modeString += "\"]";
		log.info("facts Getting array of: " + modeString);
		JsonArray array = null;
		try {
			array = new JsonArray(modeString);
		} catch(Exception e) {
			e.printStackTrace();
			return new CapabilityMode[0];
		}
		CapabilityMode[] modes = new CapabilityMode[array.size()];

		for(int i = 0; i < array.size(); i++) {
			modes[i] = CapabilityMode.valueOf(array.getString(i));
		}

		return modes;
	}

	/**
	 * Clean a given capability code. This will return the capability code after this process:
	 * <ul>
		* <li> prepends the capability code prefix if not already there</li>
		* <li> checks whether or not the capability code has a capability mode in its name. If this is the 
		*		case it will be reflected in the logs </li>
	 * <ul>
	 * @param rawCapabilityCode
	 * @return
	 */
	public static String cleanCapabilityCode(final String rawCapabilityCode) {
		String cleanCapabilityCode = rawCapabilityCode.toUpperCase();
		if(!cleanCapabilityCode.startsWith(CAP_CODE_PREFIX)) {
			cleanCapabilityCode = CAP_CODE_PREFIX + cleanCapabilityCode;
		}

		String[] components = cleanCapabilityCode.split("_");
		// Should be of the form PRM_<OWN/OTHER>_<CODE>
		/* 
		* 1. PRM
		* 2. OWN or OTHER
		* 3. CODE
		*/

		final boolean CHECK_OWN = false;
		if(CHECK_OWN) {
			if(components.length < 3) {
				log.warn("facts Capability Code: " + rawCapabilityCode + " missing OWN/OTHER declaration.");
			} else {
				Boolean affectsOwn = "OWN".equals(components[1]);
				Boolean affectsOther = "OTHER".equals(components[1]);

				if(!affectsOwn && !affectsOther) {
					log.warn("facts Capability Code: " + rawCapabilityCode + " has malformed OWN/OTHER declaration.");
				}
			}
		}

		// Check capability code doesn't have mode keywords
		for(CapabilityMode mode : CapabilityMode.values()) {
			if(cleanCapabilityCode.contains(mode.name()))
				log.warn("facts CapabilityCode: " + rawCapabilityCode + " has CapabilityMode: " + mode.name() + " in its name! This is bad convention and should be removed");
		}

		return cleanCapabilityCode;
	}

	/**
	 * Get a cache key for a specified realm, base entity code (can be a role or a person) and capability code
	 * @param realm
	 * @param beCode
	 * @param capCode
	 * @return
	 */
	private static String getCacheKey(String realm, String beCode, String capCode) {
		return realm + ":" + beCode + ":" + capCode;
	}

	/**
	 * To be used in the drools to write a role base entity to cache
	 * @param role the role base entity to write to the cache
	 * 
	 * @return returns the response given by the cache from writing the role BaseEntity to the cache
	 * @see {@link VertxUtils#writeCachedJson(String, String, String, String)}
	 * @see {@link BaseEntity}
	 */
	public JsonObject writeRoleToCache(BaseEntity role) {
		log.info("facts writing role: " + role.getCode() + " to cache");


		// Debug logging
		log.info("Capabilities:");
		for(EntityAttribute attribute : role.getBaseEntityAttributes()) {
			if(!attribute.getAttributeCode().startsWith(CAP_CODE_PREFIX)) {
				continue;
			}

			log.info("facts Capability: " + attribute.getAttributeCode());
		}

		// Write the role to cache
		JsonObject response = VertxUtils.writeCachedJson(getBeUtils().getServiceToken().getRealm(), role.getCode(), JsonUtils.toJson(role), getBeUtils().getServiceToken().getToken());
        
		// Check for success
		Boolean success = "ok".equalsIgnoreCase(response.getString("status"));
		if(!success) {
			log.error("Error writing Role: " + role.getCode() + " to cache: " + getBeUtils().getServiceToken().getRealm());
			log.error("Json: " + JsonUtils.toJson(role));
		}
		return response;
	}

	/**
	 * To be used to set the weight of a role base entity. Will not update the PRI_WEIGHT if the BE code does not start with ROL_
	 * @param role role base entity to set the weight of
	 * @param weight weight to set (higher weight holds higher priority in the heirarchy)
	 * @return the response from {@link BaseEntityUtils#saveAnswer(Answer)} if the role is a role BaseEntity
	 * (Starts with {@link CapabilityUtilsRefactored#ROLE_BE_PREFIX})
	 */
	public BaseEntity setRoleWeight(BaseEntity role, double weight) {
		if(!isRole(role)) {
			log.error("Attempted to set Role Weight of non role base entity!: " + role.getCode());
			log.error("Not setting PRI_WEIGHT in " + role.getCode());
			log.error("Check the relevant DROOLs to make sure this is accurate!");
			return role;
		}
		return beUtils.saveAnswer(new Answer(getBeUtils().getServiceToken().getUserCode(), role.getCode(), "PRI_WEIGHT", weight,false,true));
	}

	/**
	 * Determine whether or not the specified base entity is a role
	 * @param baseEntity base entity to check
	 * @return whether or not the base entity is a role
	 * 
	 * @see {@link CapabilityUtilsRefactored#ROLE_BE_PREFIX}
	 * @see {@link BaseEntity#getCode()}
	 */
	private static boolean isRole(BaseEntity baseEntity) {
		return isRole(baseEntity.getCode());
	}

	private static boolean isRole(String beCode) {
		return beCode.startsWith(ROLE_BE_PREFIX);
	}

	/**
	 * 
	 * Check whether a base entity is allowed to have capabilities stored in the base entity
	 * @param baseEntity BaseEntity to check
	 * @return
	 * 
	 * @see {@link BaseEntity}
	 * @see {@link CapabilityUtilsRefactored#ACCEPTED_CAP_PREFIXES}
	 */
	public static boolean isAllowedToHaveCapabilities(BaseEntity baseEntity) {
		return isAllowedToHaveCapabilities(baseEntity.getCode());
	}

	/**
	 * Check whether a base entity is allowed to have capabilities stored in the base entity
	 * @param beCode BaseEntity code to check
	 * @return
	 * @see {@link BaseEntity#getCode()}
	 * @see {@link CapabilityUtilsRefactored#ACCEPTED_CAP_PREFIXES}
	 */
	public static Boolean isAllowedToHaveCapabilities(String beCode) {
		for( String prefix : ACCEPTED_CAP_PREFIXES ) {
			if(beCode.startsWith(prefix))
				return true;
		}

		return false;
	}

	public void recursivelyCheckSidebarAskForCapability(Ask ask) {

		ArrayList<Ask> askList = new ArrayList<>();

		for (Ask childAsk : ask.getChildAsks()) {
			String code = "SIDEBAR_" + childAsk.getQuestionCode();

			if (hasCapability(code, CapabilityMode.VIEW)) {

				recursivelyCheckSidebarAskForCapability(childAsk);
				askList.add(childAsk);
			}
		}

		Ask[] items = askList.toArray(new Ask[askList.size()]);
		ask.setChildAsks(items);
	}

	@Override
	public String toString() {
		return "CapabilityUtils [" + (capabilityManifest != null ? "capabilityManifest=" + capabilityManifest : "")
				+ "]";
	}
}
