package life.genny.utils;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.bootxport.bootx.GoogleImportService;
import life.genny.bootxport.bootx.XlsxImport;
import life.genny.bootxport.bootx.XlsxImportOnline;
import life.genny.models.BaseEntityImport;
import life.genny.qwandautils.QwandaUtils;

public class ImportUtils {

	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
	public static List<BaseEntityImport> importGoogleDoc(final String id, String sheetName,Map<String,String> fieldMapping)
	{		
		List<BaseEntityImport> beImportList = new ArrayList<BaseEntityImport>();
		log.info("Importing "+id);
		   try {
			   GoogleImportService gs = GoogleImportService.getInstance();
			    XlsxImport xlsImport = new XlsxImportOnline(gs.getService());
		//	    Realm realm = new Realm(xlsImport,id);
//			    realm.getDataUnits().stream()
//			        .forEach(data -> System.out.println(data.questions.size()));
			    Set<String> keys = new HashSet<String>();
			    for (String field : fieldMapping.keySet()) {
			    	keys.add(field);
			    }
			      Map<String, Map<String,String>> mapData = xlsImport.mappingRawToHeaderAndValuesFmt(id, sheetName, keys);
			      Integer rowIndex = 0;
			      for (Map<String,String> row : mapData.values()) 
			    	  
			      {
			    	  BaseEntityImport beImport = new BaseEntityImport();
			    	  // generate a unique UUID
			    	  String uniqueCodeField = row.get("UNIQUE_KEY_FIELD");
			    	  String uniqueCode = null;
			    	  String prefix = row.get("PREFIX");
		    		  if (prefix == null) {
		    			  prefix = "PER_";
		    		  } else {
		    			  prefix = prefix.toUpperCase();
		    		  }

			    	  if (uniqueCodeField == null) {
			    		  uniqueCode = QwandaUtils.getUniqueId(prefix);	
			    	  } else {
			    		  uniqueCode = row.get(uniqueCodeField);
				    	  if (uniqueCode == null) {
				    		  uniqueCode = QwandaUtils.getUniqueId(prefix);	
				    	  }		    	  
				    	  else {
				    		  uniqueCode = prefix+row.get(uniqueCodeField);
				    		  uniqueCode = uniqueCode.toUpperCase();
				    		  // remove non alpha digits
				    		  uniqueCode = QwandaUtils.getNormalisedUsername(uniqueCode);
				    	  }

			    	  }
			    	  beImport.setCode(uniqueCode);
			    	  beImport.setName(uniqueCode);
			    	  for (String col : row.keySet()) {
			    		  String val = row.get(col.trim());
			    		  if (val!=null) {
			    			  val = val.trim();
			    		  }
			    		  String attributeCode = fieldMapping.get(col);
			    		  if (attributeCode != null) {
			    			  // we now have attributeCode and the value
			    			  Tuple2<String,String> pair = Tuple.of(attributeCode, val);
			    			  beImport.getAttributeValuePairList().add(pair);
			    			  if ("PRI_NAME".equals(attributeCode)) {
			    				  beImport.setName(val);
			    			  }
				    		  if ("PRI_EMAIL".equals(attributeCode)) {
				    			  String email = val;
				    			  String uCode = prefix+QwandaUtils.getNormalisedUsername(val);
				    			  beImport.setCode(uCode);
				    		  }
			    		  } else {
			    			 // log.error("Null Attribute Code - ignoring "+col);
			    		  }
			    		  
			    	  }
			    	  beImportList.add(beImport);
			    	  rowIndex++;
			      }
			      
			    } catch (Exception e1) {
			      return beImportList;
			    }

		
		return beImportList;
	}
}
