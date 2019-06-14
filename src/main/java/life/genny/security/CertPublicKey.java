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

  private final String KID = "kid";
  private final String KEYS = "keys";
  private final String KTY = "kty";
  private final String RS256 = "RS256";
  private final String SIG = "sig";
  private final String N = "n";
  private final String E = "e";

  private Map<String,PublicKey> publicKeyMap = new HashMap<String,PublicKey>();

  

  public PublicKey getPublicKey(final String realm) {
    Optional<PublicKey> ifExist = Optional.ofNullable(publicKeyMap.get(realm));
    PublicKey value = ifExist.orElseGet(()->findPublicKey(realm));
    publicKeyMap.put(realm, value);
    return value;
  }

  public void setPublicKey(final String realm,PublicKey publicKey) {
    this.publicKeyMap.put(realm, publicKey);
  }

  public PublicKey findPublicKey(final String realm) throws NullPointerException {
    JsonObject value = fetchOIDCPubKey(realm);
    
    JsonArray jsonArray = value.getJsonArray(KEYS);
    JsonObject jsonObject = jsonArray.getJsonObject(0);
    JWK jwk = new JWK();
    jwk.setKeyId(jsonObject.getString(KID));
    jwk.setKeyType(jsonObject.getString(KTY));
    jwk.setAlgorithm(jsonObject.getString(RS256));
    jwk.setPublicKeyUse(jsonObject.getString(SIG));
    jwk.setOtherClaims(N, jsonObject.getString(N));
    jwk.setOtherClaims(E, jsonObject.getString(E));
    return JWKParser.create(jwk).toPublicKey();
  }


  public void reload(final String realm) throws NullPointerException {
    PublicKey key = findPublicKey(realm);
    publicKeyMap.put(realm, key);
  }

  public String encodedToBase64(final String realm) {
    return Base64.getEncoder()
        .encodeToString(getPublicKey(realm).getEncoded());
  }

  public static JsonObject fetchOIDCPubKey(final String realm) {

	String projectCode = "PRJ_"+realm.toUpperCase();
	JsonObject jsonObj = VertxUtils.readCachedJson(realm, projectCode);
	if (jsonObj == null) {
		return null;
	}
	if ("error".equals(jsonObj.getString("status"))) {
		return null;
	}
	BaseEntity project = JsonUtils.fromJson(jsonObj.getString("value").toString(), BaseEntity.class);
	String keycloakUrl = project.getValue("ENV_KEYCLOAK_REDIRECTURI","http://keycloak.genny.life");
    String apiGet = null;
    String keycloakCertUrl = 
            keycloakUrl
            + "/auth/realms/"
            + realm
            + "/protocol/openid-connect/certs";
    try {
      apiGet = QwandaUtils.apiGet(keycloakCertUrl, null);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return Json.mapper.convertValue(apiGet, JsonObject.class);
  }

  public static void main(String... strings) {


    System.out.println(CertPublicKey.INSTANCE.encodedToBase64("genny"));

  }

}
