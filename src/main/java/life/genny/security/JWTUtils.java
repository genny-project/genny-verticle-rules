package life.genny.security;


import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.auth.jwt.JWTAuth;
import life.genny.cluster.CurrentVtxCtx;

public class JWTUtils {


  private static String PERMISSIONS_CLAIM_KEY = "realm_access/roles";
  private static String RS256 = "RS256";

  public static JWTAuth getProvider() {

    // Get Configuration Options with paratameters settled
    JWTAuthOptions c = new JWTAuthOptions()
        .addPubSecKey(new PubSecKeyOptions().setAlgorithm(RS256)
            .setPublicKey(CertPublicKey.INSTANCE.encodedToBase64()))
        .setPermissionsClaimKey(PERMISSIONS_CLAIM_KEY);
    // JWTAuth provider = JWTAuth.create(
    // CurrentVtxCtx.getCurrentCtx().getClusterVtx(), c);

    JWTAuth provider = JWTAuth.create(Vertx.vertx(), c);
    return provider;
  }

}
