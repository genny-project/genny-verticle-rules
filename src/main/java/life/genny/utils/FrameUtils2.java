package life.genny.utils;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.FrameTuple3;
import life.genny.models.GennyToken;
import life.genny.models.QuestionTheme;
import life.genny.models.Theme;
import life.genny.models.ThemeAttribute;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemeDouble;
import life.genny.models.ThemePosition;
import life.genny.models.ThemeTuple4;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;

import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Link;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.EntityQuestion;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;

public class FrameUtils2 {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static public Boolean showLogs = false;

	static public void toMessage(final Frame3 rootFrame, GennyToken serviceToken) {
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
		toMessage(rootFrame, serviceToken, contextListMap);
		
		// check that the MSG got saved
		
		QDataBaseEntityMessage FRM_MSG = VertxUtils.getObject(serviceToken.getRealm(), "", rootFrame.getCode() + "_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		if (FRM_MSG == null) {
			log.info("ERROR: rootFrame:"+rootFrame.getCode()+" NOT CREATED");
		}
		VertxUtils.putObject(serviceToken.getRealm(), "", rootFrame.getCode(),
				 rootFrame, serviceToken.getToken());

		log.info(rootFrame.getCode()+" RULE SAVED FRAME TO CACHE");
	}
	
	static public void toMessage2(final Frame3 rootFrame, GennyToken serviceToken) {
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
		toMessage(rootFrame, serviceToken, contextListMap);
		
		// check that the MSG got saved
		
		QDataBaseEntityMessage FRM_MSG = VertxUtils.getObject(serviceToken.getRealm(), "", rootFrame.getCode() + "_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		if (FRM_MSG == null) {
			log.info("ERROR: rootFrame:"+rootFrame.getCode()+" NOT CREATED");
		}
		
	}

	static public QDataBaseEntityMessage toMessage(final Frame3 rootFrame, GennyToken serviceToken,
			Set<QDataAskMessage> asks) {
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
		return toMessage(rootFrame, serviceToken, asks, contextListMap);
	}

	static public void toMessage(final Frame3 rootFrame, GennyToken serviceToken,
			Map<String, ContextList> contextListMap) {
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = toMessage(rootFrame, serviceToken, askMsgs, contextListMap);
		String askMsgsStr = JsonUtils.toJson(askMsgs);
		// TODO, this is NOT needed, only enabkled for testing
		VertxUtils.putObject(serviceToken.getRealm(), "", rootFrame.getCode(),
		 rootFrame, serviceToken.getToken());
		BaseEntity ruleEntity = beUtils.getBaseEntityByCode("RUL_"+rootFrame.getCode().toUpperCase());
		if (ruleEntity == null) {
			beUtils.create("RUL_"+rootFrame.getCode().toUpperCase(), "RUL_"+rootFrame.getCode().toUpperCase());
			log.error("!!!!!!!!!!!!!!!!!!!!!!!! RUL_"+rootFrame.getCode().toUpperCase()+" WAS NOT IN DB????");
		}
		beUtils.saveAnswer(new Answer("RUL_"+rootFrame.getCode().toUpperCase(), "RUL_"+rootFrame.getCode().toUpperCase(), "PRI_FRM",JsonUtils.toJson(rootFrame),false));


		VertxUtils.putObject(serviceToken.getRealm(), "", rootFrame.getCode() + "_MSG", msg, serviceToken.getToken());
		beUtils.saveAnswer(new Answer("RUL_"+rootFrame.getCode().toUpperCase(), "RUL_"+rootFrame.getCode().toUpperCase(), "PRI_MSG",JsonUtils.toJson(msg),false));
		if (!askMsgs.isEmpty()) {
			VertxUtils.putObject(serviceToken.getRealm(), "", rootFrame.getCode().toUpperCase() + "_ASKS", askMsgsStr,
				serviceToken.getToken());
			beUtils.saveAnswer(new Answer("RUL_"+rootFrame.getCode().toUpperCase(), "RUL_"+rootFrame.getCode().toUpperCase(), "PRI_ASKS",askMsgsStr,false));

		}
	}

	static public QDataBaseEntityMessage toMessage(final Frame3 rootFrame, GennyToken serviceToken,
			Set<QDataAskMessage> asks, Map<String, ContextList> contextListMap) {

		Set<BaseEntity> baseEntityList = new HashSet<BaseEntity>();
		Set<Ask> askList = new HashSet<>();

		BaseEntity root = getBaseEntity(rootFrame, serviceToken);

		// log.info(root.toString());

		baseEntityList.add(root);

		// Traverse the frame tree and build BaseEntitys and links
		processFrames(rootFrame, serviceToken, baseEntityList, root, askList);

		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(new ArrayList<>(baseEntityList));
		msg.setTotal(msg.getReturnCount()); // fudge the total.
		msg.setReplace(true);

		if (!VertxUtils.cachedEnabled) { // cannot retrieve questions if no service!
			for (Ask ask : askList) {
				String sourceAliasCode = serviceToken.getUserCode();
				String targetAliasCode = serviceToken.getUserCode();
				if ((!ask.getTargetCode().equals(serviceToken.getUserCode()))&&(!ask.getTargetCode().startsWith("QUE_"))) {
					targetAliasCode = ask.getTargetCode();
					log.info("Setting targetAliasCode "+targetAliasCode+" for "+ask.getQuestionCode());
				}
				QDataAskMessage askMsg = null;
				
				try {
					askMsg = QuestionUtils.getAsks(sourceAliasCode, targetAliasCode,
							ask.getQuestionCode(), serviceToken.getToken());
				} catch (NullPointerException e) {
					log.error("Null pointer in getAsks "+ask.getQuestionCode());
				}
				
				if (null == askMsg) {
					askMsg = new QDataAskMessage(new Ask[0]);
				}
				askMsg = processQDataAskMessage(askMsg, ask, serviceToken);

				if ((contextListMap != null) && (!contextListMap.isEmpty())) {
					for (Ask anAsk : askMsg.getItems()) {
						// Check for any associated ContextList to anAsk
						String attributeCode = anAsk.getAttributeCode();
						String targetCode = anAsk.getTargetCode();
						String key = targetCode + ":" + attributeCode;

						if (contextListMap.containsKey(key)) {
							ContextList contextList = contextListMap.get(key);
							anAsk.setContextList(contextList);
						}
					}
				}
				askMsg.setToken(serviceToken.getToken());
				askMsg.setReplace(true);
				asks.add(askMsg);
			}
		}
		msg.setToken(serviceToken.getToken());
		return msg;
	}

	private static QDataAskMessage processQDataAskMessage(QDataAskMessage askMsg, Ask contextAsk,
			GennyToken serviceToken) {
		List<Ask> asks = new ArrayList<Ask>();
		if ((askMsg != null) && (askMsg.getItems() != null)) {
			for (Ask ask : askMsg.getItems()) {
				ask.setQuestionCode(contextAsk.getQuestionCode()); // ?

				ask.setContextList(contextAsk.getContextList());

				// if ask question is not a group then make it a fake group
				// if (!StringUtils.endsWith(ask.getQuestion().getCode(), "_GRP")) {
				// String attributeCode = "QQQ_QUESTION_GROUP_INPUT";
				//
				// /* Get the on-the-fly question attribute */
				// Attribute attribute = RulesUtils.getAttribute(attributeCode,
				// serviceToken.getToken());
				//
				// Question fakeQuestionGrp = new Question(ask.getQuestionCode() + "_GRP",
				// ask.getName(), attribute,
				// false);
				// fakeQuestionGrp.setMandatory(ask.getMandatory());
				// fakeQuestionGrp.setRealm(ask.getRealm());
				// fakeQuestionGrp.setReadonly(ask.getReadonly());
				// fakeQuestionGrp.setOneshot(ask.getOneshot());
				//
				// try {
				// fakeQuestionGrp.addTarget(ask.getQuestion(), 1.0);
				// } catch (BadDataException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// Ask newask = new Ask(fakeQuestionGrp, serviceToken.getUserCode(),
				// serviceToken.getUserCode(), false,
				// 1.0, false, false, true);
				// Ask[] childAsks = new Ask[1];
				// childAsks[0] = ask;
				// newask.setChildAsks(childAsks);
				// asks.add(newask);
				//
				// } else {
				asks.add(ask);
				// }
			}
		}
		Ask[] itemsArray = new Ask[asks.size()];
		itemsArray = asks.toArray(itemsArray);
		askMsg.setItems(itemsArray);
		return askMsg;
	}

	private static BaseEntity getBaseEntity(final Frame3 rootFrame, final GennyToken serviceToken) {
		return getBaseEntity(rootFrame.getCode(), rootFrame.getName(), serviceToken);

	}

	private static BaseEntity getBaseEntity(final String beCode, final String beName, final GennyToken serviceToken) {
		BaseEntity be = null; // VertxUtils.getObject(serviceToken.getRealm(), "", beCode, BaseEntity.class,
		// serviceToken.getToken());
		if (be == null) {
			try {
				if (VertxUtils.cachedEnabled) {
					be = new BaseEntity(beCode, beName);
					be.setRealm(serviceToken.getRealm());
				} else {
					be = QwandaUtils.getBaseEntityByCodeWithAttributes(beCode, serviceToken.getToken());
					if (be == null) {
						try {
							be = QwandaUtils.createBaseEntityByCode(beCode, beName, GennySettings.qwandaServiceUrl,
									serviceToken.getToken());
						} catch (java.lang.NumberFormatException e) {
							be = new BaseEntity(beCode, beName);
						}
					}
				}
			} catch (Exception e) {
				be = QwandaUtils.createBaseEntityByCode(beCode, beName, GennySettings.qwandaServiceUrl,
						serviceToken.getToken());
			}
		}
		be.setLinks(new HashSet<EntityEntity>()); // clear
		return be;

	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processFrames(final Frame3 frame, GennyToken serviceToken, Set<BaseEntity> baseEntityList,
			BaseEntity parent, Set<Ask> askList) {

		if (frame.getFrames().isEmpty()) {
			// create the ask
			if (frame.getQuestionGroup()!=null) {
				processAskAliasEmpty(frame, serviceToken, baseEntityList, parent,askList, frame.getPosition(), 1.0);
			}
		}
		
		// Go through the frames and fetch them
		for (FrameTuple3 frameTuple3 : frame.getFrames()) {
			if (showLogs) {
				log.info("Processing Frame     " + frameTuple3.getFrame().getCode());
			}
			Frame3 childFrame = frameTuple3.getFrame();
			FramePosition position = frameTuple3.getFramePosition();
			Double weight = frameTuple3.getWeight();
			if (frame.getWeight()!=null) {
				weight = frame.getWeight();
			}
			childFrame.setParent(parent); // Set the parent sop that we can link the childs themes to it.

			processAskAlias(frame, serviceToken, baseEntityList, parent, askList, childFrame, position, weight);

		}
	}

	/**
	 * @param frame
	 * @param serviceToken
	 * @param baseEntityList
	 * @param parent
	 * @param askList
	 * @param childFrame
	 * @param position
	 * @param weight
	 */
	private static void processAskAliasEmpty(final Frame3 frame, GennyToken serviceToken, Set<BaseEntity> baseEntityList,
			BaseEntity parent,Set<Ask> askList, FramePosition position, Double weight) {
		BaseEntity childBe = getBaseEntity(frame, serviceToken);



		if (!frame.getThemes().isEmpty()) {
			processThemes(frame, position, serviceToken, baseEntityList, childBe);
			parent.getLinks().addAll(childBe.getLinks());
		}

		if (!frame.getThemeObjects().isEmpty()) {
			processThemeTuples(frame, position, serviceToken, baseEntityList, childBe);
		}

		if (frame.getQuestionGroup() != null) {
			if (showLogs) {
				log.info("Processing Question  " + frame.getQuestionCode());
			}
			if ("PRI_FIRSTNAME".equals(frame.getQuestionCode())) {
				log.info("Detected");
			}
			/* create an ask */
			BaseEntity askBe = new BaseEntity(frame.getQuestionGroup().getCode(),
					frame.getQuestionGroup().getCode());
			askBe.setRealm(parent.getRealm());

			Ask ask = null;

			if (frame.getQuestionName() != null) {
				ask = createVirtualAsk(frame.getQuestionCode(), frame.getQuestionName(),
						frame.getQuestionGroup().getSourceAlias(),
						frame.getQuestionGroup().getTargetAlias(), serviceToken);
				ask.setRealm(parent.getRealm());

			} else {

				ask = QuestionUtils.createQuestionForBaseEntity2(askBe,
						StringUtils.endsWith(askBe.getCode(), "GRP"), serviceToken,
						frame.getQuestionGroup().getSourceAlias(),
						frame.getQuestionGroup().getTargetAlias());
			}

			Map<ContextType, Set<BaseEntity>> contextMap = new HashMap<ContextType, Set<BaseEntity>>();
			Map<ContextType, life.genny.qwanda.VisualControlType> vclMap = new HashMap<ContextType, VisualControlType>();
			/* package up Question Themes */
			if (!frame.getQuestionGroup().getQuestionThemes().isEmpty()) {
				for (QuestionTheme qTheme : frame.getQuestionGroup().getQuestionThemes()) {
					if (showLogs) {
						log.info("Question Theme: " + qTheme.getCode() + ":" + qTheme.getJson());
					}
					processQuestionThemes(askBe, qTheme, serviceToken, ask, baseEntityList, contextMap, vclMap);
					Set<BaseEntity> themeSet = new HashSet<BaseEntity>();
					if (qTheme.getTheme() != null) {
						themeSet.add(qTheme.getTheme().getBaseEntity());
						// Hack
						VisualControlType vcl = null;
						if (!((qTheme.getCode().equals("THM_FORM_DEFAULT"))
								|| (qTheme.getCode().equals("THM_BUTTONS"))
								|| (qTheme.getCode().equals("THM_FORM_CONTAINER_DEFAULT")))) {
							vcl = qTheme.getVcl();
						}
						createVirtualContext(ask, themeSet, ContextType.THEME, vcl, qTheme.getWeight(),qTheme.getDataType());
					}

				}

			}
			Set<EntityQuestion> entityQuestionList = askBe.getQuestions();

			Link linkAsk = new Link(frame.getCode(), frame.getQuestionCode(), "LNK_ASK",
					FramePosition.CENTRE.name());
			linkAsk.setWeight(ask.getWeight());
			EntityQuestion ee = new EntityQuestion(linkAsk);
			entityQuestionList.add(ee);

			childBe.setQuestions(entityQuestionList);
			baseEntityList.add(askBe);
			/* Set the ask to support any sourceAlias and targetAlias */

			askList.add(ask); // add to the ask list

		}
	}

	
	/**
	 * @param frame
	 * @param serviceToken
	 * @param baseEntityList
	 * @param parent
	 * @param askList
	 * @param childFrame
	 * @param position
	 * @param weight
	 */
	private static void processAskAlias(final Frame3 frame, GennyToken serviceToken, Set<BaseEntity> baseEntityList,
			BaseEntity parent, Set<Ask> askList, Frame3 childFrame, FramePosition position, Double weight) {
		BaseEntity childBe = getBaseEntity(childFrame, serviceToken);

		// link to the parent
		EntityEntity link = null;
		Attribute linkFrame = new AttributeLink("LNK_FRAME", "frame");
		link = new EntityEntity(parent, childBe, linkFrame, position.name(), weight);
		if (!parent.getLinks().contains(link)) {
			parent.getLinks().add(link);
		}
		baseEntityList.add(childBe);

		// Traverse the frame tree and build BaseEntitys and links
		if (!childFrame.getFrames().isEmpty()) {
			processFrames(childFrame, serviceToken, baseEntityList, childBe, askList);
		}

		if (!childFrame.getThemes().isEmpty()) {
			processThemes(childFrame, position, serviceToken, baseEntityList, childBe);
		}

		if (!childFrame.getThemeObjects().isEmpty()) {
			processThemeTuples(childFrame, position, serviceToken, baseEntityList, childBe);
		}

		if (childFrame.getQuestionGroup() != null) {
			if (showLogs) {
				log.info("Processing Question  " + childFrame.getQuestionCode());
			}
			if ("PRI_FIRSTNAME".equals(childFrame.getQuestionCode())) {
				log.info("Detected");
			}
			/* create an ask */
			BaseEntity askBe = new BaseEntity(childFrame.getQuestionGroup().getCode(),
					childFrame.getQuestionGroup().getCode());
			askBe.setRealm(parent.getRealm());

			Ask ask = null;

			if (childFrame.getQuestionName() != null) {
				ask = createVirtualAsk(childFrame.getQuestionCode(), childFrame.getQuestionName(),
						childFrame.getQuestionGroup().getSourceAlias(),
						childFrame.getQuestionGroup().getTargetAlias(), serviceToken);
				ask.setRealm(parent.getRealm());

			} else {

				ask = QuestionUtils.createQuestionForBaseEntity2(askBe,
						StringUtils.endsWith(askBe.getCode(), "GRP"), serviceToken,
						childFrame.getQuestionGroup().getSourceAlias(),
						childFrame.getQuestionGroup().getTargetAlias());
			}

			Map<ContextType, Set<BaseEntity>> contextMap = new HashMap<ContextType, Set<BaseEntity>>();
			Map<ContextType, life.genny.qwanda.VisualControlType> vclMap = new HashMap<ContextType, VisualControlType>();
			/* package up Question Themes */
			if (!childFrame.getQuestionGroup().getQuestionThemes().isEmpty()) {
				for (QuestionTheme qTheme : childFrame.getQuestionGroup().getQuestionThemes()) {
					if (showLogs) {
						log.info("Question Theme: " + qTheme.getCode() + ":" + qTheme.getJson());
					}
					processQuestionThemes(askBe, qTheme, serviceToken, ask, baseEntityList, contextMap, vclMap);
					Set<BaseEntity> themeSet = new HashSet<BaseEntity>();
					if (qTheme.getTheme() != null) {
						themeSet.add(qTheme.getTheme().getBaseEntity());
						// Hack
						VisualControlType vcl = null;
						if (!((qTheme.getCode().equals("THM_FORM_DEFAULT"))
								|| (qTheme.getCode().equals("THM_BUTTONS"))
								|| (qTheme.getCode().equals("THM_FORM_CONTAINER_DEFAULT")))) {
							vcl = qTheme.getVcl();
						}
						Ask vAsk = createVirtualContext(ask, themeSet, ContextType.THEME, vcl, qTheme.getWeight(),qTheme.getDataType());
					}

				}

			}
			Set<EntityQuestion> entityQuestionList = askBe.getQuestions();

			Link linkAsk = new Link(childFrame.getCode(), childFrame.getQuestionCode(), "LNK_ASK",
					FramePosition.CENTRE.name());
			linkAsk.setWeight(ask.getWeight());
			EntityQuestion ee = new EntityQuestion(linkAsk);
			entityQuestionList.add(ee);

			childBe.setQuestions(entityQuestionList);
			baseEntityList.add(askBe);
			/* Set the ask to support any sourceAlias and targetAlias */

			askList.add(ask); // add to the ask list

		}
	}

	static public Ask createVirtualAsk(final String questionCode, final String questionName, final String sourceAlias,
			final String targetAlias, GennyToken serviceToken) {
		Attribute attribute = RulesUtils.getAttribute(questionCode, serviceToken.getToken());
		if (attribute == null) {
			attribute = new AttributeText(questionCode, questionName);
			attribute.setRealm(serviceToken.getRealm());
		}
		Boolean readonly = true;
		Boolean hidden = false;
		Boolean disabled = false;
		Double askweight = 1.0;
		Boolean aMandatory = false;
		Question fakeQuestion = new Question(questionCode, questionName, attribute, aMandatory);

		Ask ask = new Ask(fakeQuestion, sourceAlias, targetAlias, aMandatory, askweight, disabled, hidden, readonly);

		return ask;
	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processThemeTuples(final Frame3 frame, FramePosition position, GennyToken gennyToken,
			Set<BaseEntity> baseEntityList, BaseEntity parent) {
		// Go through the theme codes and fetch the
		for (ThemeTuple4 themeTuple4 : frame.getThemeObjects()) {
			if (showLogs) {
				log.info("Processing Theme     " + themeTuple4.getThemeCode());
			}
			String themeCode = themeTuple4.getThemeCode();
			ThemeAttributeType themeAttribute = themeTuple4.getThemeAttributeType();
			if (!themeAttribute.name().equalsIgnoreCase("codeOnly")) {
				JSONObject themeJson = themeTuple4.getJsonObject();
				Double weight = themeTuple4.getWeight();

				BaseEntity themeBe = getBaseEntity(themeCode, themeCode, gennyToken);

				// Attribute attribute = RulesUtils.getAttribute(themeAttribute.name(),
				// gennyToken.getToken());
				Attribute attribute = new Attribute(themeAttribute.name(), themeAttribute.name(),
						new DataType("DTT_THEME"));
				try {
					if (themeBe.containsEntityAttribute(themeAttribute.name())) {
						EntityAttribute themeEA = themeBe.findEntityAttribute(themeAttribute.name()).get();
						String existingSetValue = themeEA.getAsString();
						JSONObject json = new JSONObject(existingSetValue);
						Iterator<String> keys = themeJson.keys();

						while (keys.hasNext()) {
							String key = keys.next();
							Object value = json.get(key);
							json.put(key, value);
						}

						themeEA.setValue(json.toString());
						themeEA.setWeight(weight);
					} else {
						themeBe.addAttribute(new EntityAttribute(themeBe, attribute, weight, themeJson.toString()));
					}
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// link to the parent
				EntityEntity link = null;
				Attribute linkFrame = new AttributeLink("LNK_THEME", "theme");
				link = new EntityEntity(frame.getParent(), themeBe, linkFrame, position.name(), weight);
				if (!frame.getParent().getLinks().contains(link)) {
					frame.getParent().getLinks().add(link);
				}
				baseEntityList.add(themeBe);

			}
		}

	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processThemes(final Frame3 frame, FramePosition position, GennyToken gennyToken,
			Set<BaseEntity> baseEntityList, BaseEntity parent) {

		// Go through the theme codes and fetch the
		for (ThemeDouble themeTuple2 : frame.getThemes()) {
			if (showLogs) {
				log.info("Processing Theme     " + themeTuple2.getTheme().getCode());
			}
			Theme theme = themeTuple2.getTheme();
			Double weight = themeTuple2.getWeight();
			ThemePosition themePosition = themeTuple2.getThemePosition();

			if (theme == null) {
				log.error("null pointer!");
				return;
			}
			BaseEntity themeBe = null;
			try {
				themeBe = getBaseEntity(theme.getCode(), theme.getCode(), gennyToken);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if ((theme != null)&&(theme.getAttributes()!=null)&&(!theme.getAttributes().isEmpty())) {
			
			for (ThemeAttribute themeAttribute : theme.getAttributes()) {
				Attribute attribute = null;

				if (themeAttribute.getCode() == null) {
					log.info("themeAttribute code is null");
				} else {
					if (!VertxUtils.cachedEnabled) {
						attribute = RulesUtils.getAttribute(themeAttribute.getCode(), gennyToken.getToken());
					}
				}

				if (attribute == null) {
					attribute = new Attribute(themeAttribute.getCode(), themeAttribute.getCode(),
							new DataType("DTT_THEME"));
				}

				try {
					if (themeBe.containsEntityAttribute(themeAttribute.getCode())) {
						EntityAttribute themeEA = themeBe.findEntityAttribute(themeAttribute.getCode()).get();
						String existingSetValue = themeEA.getAsString();
						JSONObject json = new JSONObject(existingSetValue);
						JSONObject merged = new JSONObject(json, JSONObject.getNames(json));
						JSONObject jo = themeAttribute.getJsonObject();
						for (Object key : jo.names()/* JSONObject.getNames(themeAttribute.getJsonObject()) */) {
							merged.put((String) key, themeAttribute.getJsonObject().get((String) key));
						}

						themeEA.setValue(merged.toString());
						themeEA.setWeight(weight);
					} else {
						if (attribute.dataType.getClassName().equals(Boolean.class.getCanonicalName())) {
							themeBe.addAttribute(
									new EntityAttribute(themeBe, attribute, weight, themeAttribute.getValueBoolean()));
						} else if (attribute.dataType.getClassName().equals(Double.class.getCanonicalName())) {
							themeBe.addAttribute(
									new EntityAttribute(themeBe, attribute, weight, themeAttribute.getValueDouble()));
						} else if (attribute.dataType.getClassName().equals(String.class.getCanonicalName())) {
							themeBe.addAttribute(
									new EntityAttribute(themeBe, attribute, weight, themeAttribute.getValueString()));
						} else {
							themeBe.addAttribute(new EntityAttribute(themeBe, new Attribute(themeAttribute.getCode(),
									themeAttribute.getCode(), new DataType("DTT_THEME")), weight,
									themeAttribute.getJson()));
						}
					}
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// link to the parent
				EntityEntity link = null;
				Attribute linkFrame = new AttributeLink("LNK_THEME", "theme");

				if (theme.getDirectLink()) {
					link = new EntityEntity(parent, themeBe, linkFrame, themePosition.name(), weight);
					if (!parent.getLinks().contains(link)) {
						parent.getLinks().add(link);
					}

				} else {

					link = new EntityEntity(frame.getParent(), themeBe, linkFrame, position.name(), weight);
					if (!frame.getParent().getLinks().contains(link)) {
						frame.getParent().getLinks().add(link);
					}
				}
				baseEntityList.add(themeBe);

			}
			} else {
				if (theme==null) {
					log.error("Theme is null");
				} else {
				log.warn("Theme has no attributes ");
				}
			}
		}

	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processQuestionThemes(final BaseEntity fquestion, QuestionTheme qTheme, GennyToken gennyToken,
			Ask ask, Set<BaseEntity> baseEntityList, Map<ContextType, Set<BaseEntity>> contextMap,
			Map<ContextType, VisualControlType> vclMap) {

		if (qTheme.getTheme() != null) {
			Theme theme = qTheme.getTheme();
			theme.setRealm(fquestion.getRealm());
			if (showLogs) {
				log.info("Processing Theme     " + theme.getCode());
			}
			Double weight = qTheme.getWeight();

			String existingSetValue = "";
			EntityAttribute themeEA = null;
			if (theme.containsEntityAttribute(ThemeAttributeType.PRI_CONTENT.name())) {
				themeEA = theme.findEntityAttribute(ThemeAttributeType.PRI_CONTENT.name()).get();
				existingSetValue = themeEA.getAsString();

			} else {
				Attribute attribute = new Attribute(ThemeAttributeType.PRI_CONTENT.name(),
						ThemeAttributeType.PRI_CONTENT.name(), new DataType("DTT_THEME"));
				existingSetValue = (new JSONObject()).toString();
				themeEA = new EntityAttribute(theme, attribute, weight, existingSetValue);
				try {
					theme.addAttribute(new EntityAttribute(theme, attribute, weight, existingSetValue));
					themeEA = theme.findEntityAttribute(ThemeAttributeType.PRI_CONTENT.name()).get();
				} catch (BadDataException e) {

				}
			}

			if ((theme.getAttributes() != null) && (!theme.getAttributes().isEmpty()))
				for (ThemeAttribute themeAttribute : theme.getAttributes()) {
					existingSetValue = themeEA.getAsString();
					JSONObject json = new JSONObject(existingSetValue);
					JSONObject merged = json;
					JSONObject jo = themeAttribute.getJsonObject();
					if (!jo.toString().equals("{}")) {
						for (Object key : jo.names()/* JSONObject.getNames(themeAttribute.getJsonObject()) */) {
							merged.put((String) key, themeAttribute.getJsonObject().get((String) key));
						}
					}

					themeEA.setValue(merged.toString());
					themeEA.setWeight(weight);

				}

			BaseEntity themeBe = theme.getBaseEntity();

			// link to the parent
			EntityEntity link = null;
			Attribute linkFrame = new AttributeLink("LNK_THEME", "theme");
			link = new EntityEntity(fquestion, themeBe, linkFrame, weight);
			if (!fquestion.getLinks().contains(link)) {
				fquestion.getLinks().add(link);
			}

			baseEntityList.add(themeBe);

			// Add Contexts
			ContextType contextType = qTheme.getContextType();
			VisualControlType vcl = qTheme.getVcl();

			List<BaseEntity> themeList = new ArrayList<BaseEntity>();
			themeList.add(themeBe);

			if (!contextMap.containsKey(contextType)) {
				contextMap.put(contextType, new HashSet<BaseEntity>());
			}
			
			contextMap.get(contextType).add(themeBe);
			vclMap.put(contextType, vcl);

		}

	}

	/**
	 * Embeds the list of contexts (themes, icon) into an ask and also publishes the
	 * themes
	 *
	 * @param ask
	 * @param themes
	 * @param linkCode
	 * @param weight
	 * @return
	 */
	public static Ask createVirtualContext(Ask ask, Set<BaseEntity> themes, ContextType linkCode,
			life.genny.qwanda.VisualControlType visualControlType, Double weight) {
		return createVirtualContext(ask, themes, linkCode,
				visualControlType, weight,null);
	}
	/**
	 * Embeds the list of contexts (themes, icon) into an ask and also publishes the
	 * themes
	 *
	 * @param ask
	 * @param themes
	 * @param linkCode
	 * @param weight
	 * @return
	 */
	public static Ask createVirtualContext(Ask ask, Set<BaseEntity> themes, ContextType linkCode,
			life.genny.qwanda.VisualControlType visualControlType, Double weight, DataType dataType) {

		List<Context> completeContext = new ArrayList<>();

		for (BaseEntity theme : themes) {
			Context context = new Context(linkCode, theme, visualControlType, weight);
			if (dataType != null) {
				context.setDataType(dataType.getTypeName());
			}
			context.setRealm(ask.getRealm());
			completeContext.add(context);

		}

		ContextList contextList = ask.getContextList();
		if (contextList != null) {
			List<Context> contexts = contextList.getContexts();
			if (contexts.isEmpty()) {
				contexts = new ArrayList<>();
				contexts.addAll(completeContext);
			} else {
				contexts.addAll(completeContext);
			}
			contextList = new ContextList(contexts);
		} else {
			List<Context> contexts = new ArrayList<>();
			contexts.addAll(completeContext);
			contextList = new ContextList(contexts);
		}
		ask.setContextList(contextList);
		return ask;
	}

}