package life.genny.security;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.auth.AuthProvider;
import io.vertx.rxjava.ext.auth.User;

public class TokenIntrospection {

  // static String role = "offline_access";
  static String role = "user";
  static String JWT = "jwt";
  static String NULL = "null";

  public static BiPredicate<String, String> checkAuth =
      (token, role) -> {

        JsonObject tokenWrapped = new JsonObject();
        tokenWrapped.put(JWT, token);
        AuthProvider provider = JWTUtils.getProvider();
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


  public static Boolean checkAuthForRoles(List<String> roles,
      String token) {

    return roles.stream()
        .anyMatch(role -> checkAuth.test(token, role));

  }

  public static List<String> setRoles(String... roles) {
    return Arrays.asList(roles);
  }


}
