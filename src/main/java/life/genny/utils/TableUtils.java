package life.genny.utils;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Question;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;

public class TableUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	BaseEntityUtils beUtils = null;
	
	private TableUtils(BaseEntityUtils beUtils) {
		this.beUtils = beUtils;
	}

	public static Map<String, String> getTableColumns(SearchEntity searchBe) {

		Map<String, String> columns = new HashMap<>();

		for (EntityAttribute ea : searchBe.getBaseEntityAttributes()) {
			String attributeCode = ea.getAttributeCode();
			String attributeName = ea.getAttributeName();
			if (attributeCode.startsWith("COL_")) {
				columns.put(attributeCode.split("COL_")[1], attributeName);
			} else if (attributeCode.startsWith("CAL_")) {
				columns.put(attributeCode.split("CAL_")[1], attributeName);
			} else if (attributeCode.startsWith("QUE_")) {
				columns.put(attributeCode, attributeName);
			}

		}

		log.info("the Columns is :: " + columns);
		return columns;
	}
	
	
//	public Ask generateTableHeaderAsks(SearchEntity searchBe) {
//
//		List<Ask> asks = new ArrayList<>();
//
//		/* Validation for Search Attribute */
//		Validation validation = new Validation("VLD_NON_EMPTY", "EmptyandBlankValues", "(?!^$|\\s+)");
//		List<Validation> validations = new ArrayList<>();
//		validations.add(validation);
//		ValidationList searchValidationList = new ValidationList();
//		searchValidationList.setValidationList(validations);
//
//		Attribute eventAttribute = RulesUtils.attributeMap.get("PRI_SORT");
//		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
//
//		/* get table columns */
//		Map<String, String> columns = this.getTableColumns(searchBe);
//
//		/* get vertical display theme */
//		BaseEntity verticalTheme = beUtils.getBaseEntityByCode("THM_DISPLAY_VERTICAL");
//
//		for (Map.Entry<String, String> column : columns.entrySet()) {
//
//			String attributeCode = column.getKey();
//			String attributeName = column.getValue();
//
//			Attribute searchAttribute = new Attribute(attributeCode, attributeName,
//					new DataType("Text", searchValidationList, "Text"));
//
//			/* Initialize Column Header Ask group */
//			Question columnHeaderQuestion = new Question("QUE_" + attributeCode + "_GRP", attributeName,
//					questionAttribute, true);
//			Ask columnHeaderAsk = new Ask(columnHeaderQuestion, this.getUser().getCode(), searchBe.getCode());
//
//			/* creating ask for table header label-sort */
//			Ask columnSortAsk = getAskForTableHeaderSort(searchBe, attributeCode, attributeName, eventAttribute);
//
//			/* creating Ask for table header search input */
//			Question columnSearchQues = new Question("QUE_SEARCH_" + attributeCode, "Search " + attributeName + "..",
//					searchAttribute, false);
//			Ask columnSearchAsk = new Ask(columnSearchQues, this.getUser().getCode(), searchBe.getCode());
//
//			/* adding label-sort & search asks to header-ask Group */
//			List<Ask> tableColumnChildAsks = new ArrayList<>();
//			tableColumnChildAsks.add(columnSortAsk);
//			tableColumnChildAsks.add(columnSearchAsk);
//
//			/* Convert List to Array */
//			Ask[] tableColumnChildAsksArray = tableColumnChildAsks.toArray(new Ask[0]);
//
//			/* set the child asks */
//			columnHeaderAsk.setChildAsks(tableColumnChildAsksArray);
//
//			/* set Vertical Theme to columnHeaderAsk */
//			columnHeaderAsk = this.createVirtualContext(columnHeaderAsk, verticalTheme, ContextType.THEME);
//			asks.add(columnHeaderAsk);
//		}
//
//		/* Convert List to Array */
//		Ask[] asksArray = asks.toArray(new Ask[0]);
//
//		/*
//		 * we create a table-header ask grp and set all the column asks as it's childAsk
//		 */
//		Question tableHeaderQuestion = new Question("QUE_TABLE_HEADER_GRP", "Table Header Question Group",
//				questionAttribute, true);
//
//		Ask tableHeaderAsk = new Ask(tableHeaderQuestion, this.getUser().getCode(), searchBe.getCode());
//		tableHeaderAsk.setChildAsks(asksArray);
//
//		return tableHeaderAsk;
//	}
	
}