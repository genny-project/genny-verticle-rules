package life.genny.security;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import io.netty.util.concurrent.Future;
import io.vavr.Tuple;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.VertxUtils;
import rx.Single;

public enum CertPublicKey {

  INSTANCE;

  public String encodedToBase64(final String realm) {
    return fetchOIDCPubKey(realm).getString("public_key");
  }

  public static JsonObject fetchOIDCPubKey(final String realm) {
	String projectCode = "PRJ_"+realm.toUpperCase();
    String apiGet = null;
    String keycloakUrl = GennySettings.keycloakUrl;
	if (keycloakUrl == null) {
     	JsonObject jsonObj = VertxUtils.readCachedJson(realm, projectCode);
    	BaseEntity project = JsonUtils.fromJson(jsonObj.getString("value").toString(), BaseEntity.class);
    	keycloakUrl = project.getValue("ENV_KEYCLOAK_REDIRECTURI",GennySettings.keycloakUrl);
	}
    String keycloakCertUrl = 
            keycloakUrl
            + "/"
            + realm;
    try {
      apiGet = QwandaUtils.apiGet(keycloakCertUrl, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Json.mapper.convertValue(apiGet, JsonObject.class);
  }

//  public static void main(String... strings) {
//    log.info(CertPublicKey.INSTANCE.encodedToBase64("internmatch"));
//  }

}
