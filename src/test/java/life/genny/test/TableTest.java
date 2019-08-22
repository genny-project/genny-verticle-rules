package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.junit.Test;


import life.genny.qwanda.entity.SearchEntity;
import life.genny.utils.TableUtils;

public class TableTest {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@Test
	public void getTableColumnsTest()
	{
		
        SearchEntity searchBE = new SearchEntity("SBE_PEOPLE","People")
  	     .addSort("PRI_CREATED","Created",SearchEntity.Sort.DESC)
   	     .addFilter("PRI_CODE",SearchEntity.StringFilter.LIKE,"PER_%")
   	     .addColumn("PRI_FIRSTNAME", "First Name")
   	     .addColumn("PRI_LASTNAME", "Last Name")
  	     .setPageStart(0)
  	     .setPageSize(10000);

		
		Map<String, String> columns = TableUtils.getTableColumns(searchBE);
		log.info(columns);
	}
	
	
	
	
}
