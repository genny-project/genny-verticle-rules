package life.genny.security;


import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.auth.jwt.JWTAuth;



public class JWTUtils {


  private static String PERMISSIONS_CLAIM_KEY = "realm_access/roles";
  private static String RS256 = "RS256";


  public static JWTAuth getProvider(Vertx vertx,final String realm) {


    // Get Configuration Options with paratameters settled
    JWTAuthOptions c = new JWTAuthOptions()
        .addPubSecKey(new PubSecKeyOptions().setAlgorithm(RS256)

            .setPublicKey(CertPublicKey.INSTANCE.encodedToBase64(realm)))

        .setPermissionsClaimKey(PERMISSIONS_CLAIM_KEY);
    // JWTAuth provider = JWTAuth.create(
    // CurrentVtxCtx.getCurrentCtx().getClusterVtx(), c);

    JWTAuth provider = JWTAuth.create(vertx, c);
    return provider;
  }

}
