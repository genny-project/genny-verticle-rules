package life.genny.security;

import java.util.Arrays;
import java.util.List;

import java.util.Map;

import java.util.function.BiPredicate;

import com.github.javaparser.utils.Log;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.auth.AuthProvider;
import io.vertx.rxjava.ext.auth.User;

import life.genny.qwandautils.KeycloakUtils;


public class TokenIntrospection {

  // static String role = "offline_access";
  static String role = "user";
  static String JWT = "jwt";
  static String NULL = "null";
  
  static Vertx avertx;

  public static BiPredicate<String, String> checkAuth =
      (token, role) -> {


    	// extract the realm from the token
    	  String realm = getRealm(token);
    	  
        JsonObject tokenWrapped = new JsonObject();
        tokenWrapped.put(JWT, token);
        AuthProvider provider = JWTUtils.getProvider(avertx,realm);

        User user = null;
        try {
          user = provider.rxAuthenticate(tokenWrapped).toBlocking()
              .value();
        } catch (RuntimeException e) {
          return false;
        }
        return user.rxIsAuthorised(role).toBlocking().value()
            .booleanValue();
      };


  public static Boolean checkAuthForRoles(Vertx vertx, List<String> roles,
      String token) {

    Boolean checked = false;
    avertx = vertx;
    
    try {
		checked = roles.stream()
		    .anyMatch(role -> checkAuth.test(token, role));
	} catch (NullPointerException e) {
	Log.error("Bad token - force false to CheckAuth");
	}
    
    
    return checked;
  }

  public static List<String> setRoles(String... roles) {
    return Arrays.asList(roles);
  }


  public static String getRealm(final String token)
  {
	  Map<String, Object> serviceDecodedTokenMap = KeycloakUtils.getJsonMap(token);
	return (String)serviceDecodedTokenMap.get("azp");
  }

}
