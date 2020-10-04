package life.genny.security;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

import life.genny.models.GennyToken;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.SecurityUtils;

public class EncryptionUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	static public String getEncryptedString(final String originalString,final String securityKey,final GennyToken gennyToken)
	{
		String initVector = GennySettings.dynamicInitVector(gennyToken.getRealm());
		return SecurityUtils.encrypt(securityKey, initVector, originalString).replaceAll("/", "");

	}
	
}
