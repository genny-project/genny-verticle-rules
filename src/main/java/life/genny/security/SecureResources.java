package life.genny.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.json.DecodeException;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import life.genny.qwandautils.GennySettings;


public class SecureResources {
	


  /**
   * @return the keycloakJsonMap
   */
  public static Map<String, String> getKeycloakJsonMap() {
		if (keycloakJsonMap==null || keycloakJsonMap.isEmpty()) {
			readFilenamesFromDirectory(GennySettings.realmDir);
		}

    return keycloakJsonMap;
  }

  public static Map<String, String> keycloakJsonMap = new ConcurrentHashMap<String, String>();
  private static String hostIP =
      System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : "127.0.0.1";

  /**
   * @param keycloakJsonMap the keycloakJsonMap to set
   * @return
   */
  public static Future<Void> setKeycloakJsonMap() {
    final Future<Void> fut = Future.future();
    Vertx.currentContext().owner().executeBlocking(exec -> {
      // Load in keycloakJsons
      // readFilenamesFromDirectory("./realm", keycloakJsonMap);
      // update afterwrads
      final List<String> filesList = Vertx.currentContext().owner().fileSystem().readDirBlocking("./realm");

      for (final String dirFileStr : filesList) {
        final String fileStr = new File(dirFileStr).getName();;
        if (!"keycloak-data.json".equalsIgnoreCase(fileStr)) {
        	Vertx.currentContext().owner().fileSystem().readFile(dirFileStr, d -> {
            if (!d.failed()) {
              try {
                System.out.println("Loading in [" + fileStr + "]");
                final String keycloakJsonText =
                    d.result().toString().replaceAll("localhost", hostIP);
                keycloakJsonMap.put(fileStr, keycloakJsonText);
            //    if (GennySettings.devMode) {
                if ("genny.json".equalsIgnoreCase(fileStr)) {
                	keycloakJsonMap.put(GennySettings.mainrealm+".json", keycloakJsonText);
                }
            //    }
                System.out.println("Keycloak json file:"+fileStr+":"+keycloakJsonText);

              } catch (final DecodeException dE) {

              }
            } else {
              System.err.println("Error reading  file!"+fileStr);
            }
          });
        }
      }
      fut.complete();
    }, res -> {
    });
    return fut;
  }

  public static void readFilenamesFromDirectory(final String rootFilePath) {
    final File folder = new File(rootFilePath);
    final File[] listOfFiles = folder.listFiles();
    if (listOfFiles != null) {
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        System.out.println("File " + listOfFiles[i].getName());
        try {
          String keycloakJsonText = getFileAsText(listOfFiles[i]);
          // Handle case where dev is in place with localhost
          keycloakJsonText = keycloakJsonText.replaceAll("localhost", GennySettings.hostIP);
          keycloakJsonMap.put(listOfFiles[i].getName(), keycloakJsonText);
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      } else if (listOfFiles[i].isDirectory()) {
        System.out.println("Directory " + listOfFiles[i].getName());
        readFilenamesFromDirectory(listOfFiles[i].getName());
      }
    }
    } else {
    	System.out.println("No realm files");
    }
  }

  private static String getFileAsText(final File file) throws IOException {
    final BufferedReader in = new BufferedReader(new FileReader(file));
    String ret = "";
    String line = null;
    while ((line = in.readLine()) != null) {
      ret += line;
    }
    in.close();

    return ret;
  }
  
  public static void clear()
  {
	  if (keycloakJsonMap != null) {
		  keycloakJsonMap.clear();
	  }
  }
}
