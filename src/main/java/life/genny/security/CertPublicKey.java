package life.genny.security;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import io.netty.util.concurrent.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.QwandaUtils;
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
  private PublicKey publicKey;

  private final static String KEYCLOAK_CERT_URL ;

  static{
    if (GennySettings.devMode || 
            (GennySettings.defaultLocalIP.equals(GennySettings.hostIP))) {
      KEYCLOAK_CERT_URL =
          System.getenv("KEYCLOAKURL")
          + "/auth/realms/"
          + "genny"
          + "/protocol/openid-connect/certs";
    }else{
      KEYCLOAK_CERT_URL =
          System.getenv("KEYCLOAKURL")
          + "/auth/realms/"
          + GennySettings.mainrealm
          + "/protocol/openid-connect/certs";
    }

  }

  public PublicKey getPublicKey() {
    Optional<PublicKey> ifExist = Optional.ofNullable(publicKey);
    publicKey = ifExist.orElseGet(()->findPublicKey());
    return publicKey;
  }

  public void setPublicKey(PublicKey publicKey) {
    this.publicKey = publicKey;
  }

  public PublicKey findPublicKey() {
    JsonObject value = fetchOIDCPubKey();
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

  public void reload() {
    publicKey = findPublicKey();
  }

  public String encodedToBase64() {
    return Base64.getEncoder()
        .encodeToString(getPublicKey().getEncoded());
  }

  public static JsonObject fetchOIDCPubKey() {

    String apiGet = null;
    try {
      apiGet = QwandaUtils.apiGet(KEYCLOAK_CERT_URL, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Json.mapper.convertValue(apiGet, JsonObject.class);
  }

  public static void main(String... strings) {

    System.out.println(CertPublicKey.INSTANCE.encodedToBase64());
  }

}
