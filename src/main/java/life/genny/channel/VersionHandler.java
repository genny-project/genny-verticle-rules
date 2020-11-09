package life.genny.channel;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import io.vertx.ext.web.RoutingContext;
import life.genny.qwandautils.GitUtils;

public class VersionHandler {
  
      protected static final Logger log = org.apache.logging.log4j.LogManager
          .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
  
      public static final String GIT_VERSION_PROPERTIES = "GitVersion.properties";
  
      public static final String PROJECT_DEPENDENCIES = "project_dependencies";
	  
	  public static void apiGetVersionHandler(final RoutingContext routingContext) {
	    
	    routingContext.request().bodyHandler(body -> {
	        Properties properties = new Properties();
	        String versionString = "";
	        try {
	          properties.load(Thread.currentThread().getContextClassLoader().getResource(GIT_VERSION_PROPERTIES)
	              .openStream());
	          String projectDependencies = properties.getProperty(PROJECT_DEPENDENCIES);
	          versionString = GitUtils.getGitVersionString(projectDependencies);
	        } catch (IOException e) {
	            log.error("Error reading GitVersion.properties", e);
	        } catch (NullPointerException ne) {
	        	log.error("Error reading GitVersion.properties", ne);
	        }
	    
	        routingContext.response().putHeader("Content-Type", "application/json");
	        routingContext.response().end(versionString);  
	      });
	      
	    }
}
