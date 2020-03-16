package life.genny.utils;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.bootxport.bootx.GoogleImportService;
import life.genny.bootxport.bootx.XlsxImport;
import life.genny.bootxport.bootx.XlsxImportOnline;
import life.genny.models.BaseEntityImport;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.QwandaUtils;

public class ImportUtils {

	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static List<BaseEntityImport> importGoogleDoc(final String id, String sheetName,
			Map<String, String> fieldMapping) {
		Map<String, String> fieldMapping2 = new HashMap<String, String>();
		for (String field : fieldMapping.keySet()) {
			fieldMapping2.put(field.toLowerCase(), fieldMapping.get(field));
			fieldMapping2.put(field, fieldMapping.get(field));
		}
		fieldMapping = fieldMapping2;
		List<BaseEntityImport> beImportList = new ArrayList<BaseEntityImport>();
		log.info("Importing " + id);
		try {
			GoogleImportService gs = GoogleImportService.getInstance();
			XlsxImport xlsImport = new XlsxImportOnline(gs.getService());
			// Realm realm = new Realm(xlsImport,id);
//			    realm.getDataUnits().stream()
//			        .forEach(data -> log.info(data.questions.size()));
			Set<String> keys = new HashSet<String>();
			for (String field : fieldMapping.keySet()) {
				keys.add(field);
			}
			Map<String, Map<String, String>> mapData = xlsImport.mappingRawToHeaderAndValuesFmt(id, sheetName, keys);
			Integer rowIndex = 0;
			for (Map<String, String> row : mapData.values())

			{
				BaseEntityImport beImport = new BaseEntityImport();

				String uniqueCodeField = null;
				String ukf = fieldMapping.get("UNIQUE_KEY_FIELD".toLowerCase());
				if (ukf != null) {
					uniqueCodeField = ukf.toLowerCase();
				}

				String uniqueCode = null;
				String prefix = fieldMapping.get("PREFIX".toLowerCase());
				if (prefix == null) {
					prefix = "PER_";
				} else {
					prefix = prefix.toUpperCase();
				}

				if (uniqueCodeField == null) {
					uniqueCode = QwandaUtils.getUniqueId(prefix);
				} else {
					uniqueCode = row.get(uniqueCodeField.toLowerCase());
					if (uniqueCode == null) {
						uniqueCode = QwandaUtils.getUniqueId(prefix);
					} else {
						String key = uniqueCodeField.toLowerCase(); // company abn (format: xx xxx xxx xxx)
						uniqueCode = prefix + row.get(key);
						uniqueCode = uniqueCode.toUpperCase();
						String mappedRaw2field = fieldMapping.get(uniqueCodeField.toLowerCase());
						String mappedField = fieldMapping.get(mappedRaw2field);
						if ("PRI_EMAIL".equalsIgnoreCase(mappedRaw2field)) {
							uniqueCode = QwandaUtils.getNormalisedUsername(uniqueCode);
						}
						uniqueCode = uniqueCode.replaceAll("[^A-Z0-9\\-\\_]", "");
						// remove non alpha digits
						uniqueCode = QwandaUtils.getNormalisedUsername(uniqueCode);
					}

				}
				String nameField = fieldMapping.get("NAME_KEY_FIELD");
				String beName = uniqueCode;
				if (StringUtils.isBlank(nameField)) {
					nameField = "PRI_NAME";
				} else {
					nameField = nameField.toLowerCase();
				}

				beName = row.get(nameField);
				if (StringUtils.isBlank(beName)) {
					beName = uniqueCode;
				}

				beImport.setCode(uniqueCode);
				beImport.setName(uniqueCode);
				for (String col : row.keySet()) {
					String val = row.get(col.trim());
					if (val != null) {
						val = val.trim();
					}
					String attributeCode = fieldMapping.get(col);
					if (attributeCode != null) {
						// we now have attributeCode and the value
						Tuple2<String, String> pair = Tuple.of(attributeCode, val);
						beImport.getAttributeValuePairList().add(pair);
						if ("PRI_NAME".equals(attributeCode)) {
							beImport.setName(val);
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

	public static BaseEntity processAttribute(BaseEntityUtils beUtils, String targetCode,
			Tuple2<String, String> attributeCodeValue, String importAttributeCode, String matchAttributeCode,
			String prefixFilter, String linkAttributeCode, List<Answer> answers) {
		if (attributeCodeValue._1.startsWith(importAttributeCode)) {
			QDataBaseEntityMessage foundMsg = QwandaUtils.findBaseEntityByAttributeCodeLikeValue(
					beUtils.getServiceToken().getRealm(), beUtils.getServiceToken().getToken(), matchAttributeCode,
					attributeCodeValue._2);

			if (foundMsg != null) {
				/* create link */
				if (foundMsg.getItems() != null) {
					if (foundMsg.getItems().length > 0) {
						BaseEntity result = foundMsg.getItems()[0];
						String linkValue = "[\"" + result.getCode() + "\"]";
						System.out.println("Setting up " + linkAttributeCode + " with value " + linkValue);
						Answer linkAnswer = new Answer(beUtils.getGennyToken().getUserCode(), targetCode,
								linkAttributeCode, linkValue);
						beUtils.saveAnswer(linkAnswer);
						return result;
					}
				}
			} else {
				System.out.println("ERROR: Cannot find [" + attributeCodeValue._2 + "] for attributeCode PRI_NAME");
			}
		}
		return null;
	}

	public static BaseEntity linkToFind(BaseEntityUtils beUtils, String targetCode,
			Tuple2<String, String> attributeCodeValue, String importAttributeCode, String matchAttributeCode,
			String prefixFilter, String linkAttributeCode, List<Answer> answers) {
		if (attributeCodeValue._1.startsWith(importAttributeCode)) {
			QDataBaseEntityMessage foundMsg = QwandaUtils.findBaseEntityByAttributeCodeLikeValue(
					beUtils.getServiceToken().getRealm(), beUtils.getServiceToken().getToken(), matchAttributeCode,
					attributeCodeValue._2);

			if (foundMsg != null) {
				/* create link */
				if (foundMsg.getItems() != null) {
					if (foundMsg.getItems().length > 0) {
						BaseEntity result = foundMsg.getItems()[0];
						if (beUtils == null) {
							System.out.println("BeUtils is bad in linkToFind code");
						} else {
							if (result != null) {
								String linkValue = "[\"" + targetCode + "\"]";
								System.out.println(
										"Setting up link for result " + linkAttributeCode + " with value " + linkValue);
								Answer linkAnswer = new Answer(beUtils.getGennyToken().getUserCode(), result.getCode(),
										linkAttributeCode, linkValue);
								beUtils.saveAnswer(linkAnswer);
								System.out.println("Set up " + linkAttributeCode + " with value " + matchAttributeCode
										+ ":" + attributeCodeValue._2);
								return result;
							} else {
								System.out.println("Could not Set up " + linkAttributeCode + " with value "
										+ matchAttributeCode + ":" + attributeCodeValue._2);
							}
						}
					}
				}
			} else {
				System.out.println("ERROR: Cannot find [" + attributeCodeValue._2 + "] for attributeCode PRI_NAME");
			}
		}
		return null;
	}
}
