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
import life.genny.models.GennyToken;
import life.genny.models.QuestionTheme;
import life.genny.models.Theme;
import life.genny.models.ThemeAttribute;
import life.genny.models.ThemeAttributeType;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;

import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Link;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.EntityQuestion;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.QwandaUtils;

public class FrameUtils2 {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static public QDataBaseEntityMessage toMessage(final Frame3 rootFrame, GennyToken serviceToken,
			Set<QDataAskMessage> asks) {

		Set<BaseEntity> baseEntityList = new HashSet<BaseEntity>();
		Set<Ask> askList = new HashSet<>();

		BaseEntity root = getBaseEntity(rootFrame, serviceToken);

		log.info(root.toString());

		baseEntityList.add(root);

		// Traverse the frame tree and build BaseEntitys and links
		processFrames(rootFrame, serviceToken, baseEntityList, root, askList);

		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(new ArrayList<>(baseEntityList));
		msg.setTotal(msg.getReturnCount()); // fudge the total.
		msg.setReplace(true);

		for (Ask ask : askList) {
			QDataAskMessage askMsg = QuestionUtils.getAsks(serviceToken.getUserCode(),"PRJ_INTERNMATCH",
					ask.getQuestionCode(), serviceToken.getToken());
			askMsg = processQDataAskMessage(askMsg, ask, serviceToken);

			asks.add(askMsg);
		}
		return msg;
	}

	private static QDataAskMessage processQDataAskMessage(QDataAskMessage askMsg, Ask contextAsk,
			GennyToken serviceToken) {
		List<Ask> asks = new ArrayList<Ask>();
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
		Ask[] itemsArray = new Ask[asks.size()];
		itemsArray = asks.toArray(itemsArray);
		askMsg.setItems(itemsArray);
		return askMsg;
	}

	private static BaseEntity getBaseEntity(final Frame3 rootFrame, final GennyToken serviceToken) {
		return getBaseEntity(rootFrame.getCode(), rootFrame.getName(), serviceToken);

	}

	private static BaseEntity getBaseEntity(final String beCode, final String beName, final GennyToken serviceToken) {
		BaseEntity be = null; //VertxUtils.getObject(serviceToken.getRealm(), "", beCode, BaseEntity.class,
		// serviceToken.getToken());
		if ("THM_NOT_INHERITBALE".equals(beCode)) {
			log.info("here");
		}
		if (be == null) {
			try {
				be = QwandaUtils.getBaseEntityByCodeWithAttributes(beCode,
				 serviceToken.getToken());
				if (be == null) {
					try {
						be = QwandaUtils.createBaseEntityByCode(beCode, beName, GennySettings.qwandaServiceUrl,
								serviceToken.getToken());
					} catch (java.lang.NumberFormatException e) {
						be = new BaseEntity(beCode, beName);
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

		// Go through the frames and fetch them
		for (Tuple3<Frame3, FramePosition, Double> frameTuple3 : frame.getFrames()) {
			System.out.println("Processing Frame     " + frameTuple3._1.getCode());
			Frame3 childFrame = frameTuple3._1;
			FramePosition position = frameTuple3._2;
			Double weight = frameTuple3._3;

			childFrame.setParent(parent); // Set the parent sop that we can link the childs themes to it.

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

			if (childFrame.getQuestionGroup().isPresent()) {
				System.out.println("Processing Question  " + childFrame.getQuestionCode());

				/* create an ask */
				BaseEntity askBe = new BaseEntity(childFrame.getQuestionGroup().get().getCode(),
						childFrame.getQuestionGroup().get().getCode());
				askBe.setRealm(parent.getRealm());

				Ask ask = QuestionUtils.createQuestionForBaseEntity2(askBe,
						StringUtils.endsWith(askBe.getCode(), "GRP"), serviceToken,childFrame.getQuestionGroup().get().getSourceAlias(),childFrame.getQuestionGroup().get().getTargetAlias());

				Map<ContextType, Set<BaseEntity>> contextMap = new HashMap<ContextType, Set<BaseEntity>>();
				Map<ContextType, life.genny.qwanda.VisualControlType> vclMap = new HashMap<ContextType, VisualControlType>();
				/* package up Question Themes */
				if (!childFrame.getQuestionGroup().get().getQuestionThemes().isEmpty()) {
					for (QuestionTheme qTheme : childFrame.getQuestionGroup().get().getQuestionThemes()) {
						System.out.println("Question Theme: " + qTheme.getCode() + ":" + qTheme.getJson());
						processQuestionThemes(askBe, qTheme, serviceToken, ask, baseEntityList, contextMap, vclMap);
						Set<BaseEntity> themeSet = new HashSet<BaseEntity>();
						if (qTheme.getTheme().isPresent()) {
							themeSet.add(qTheme.getTheme().get().getBaseEntity());
							// Hack
							VisualControlType vcl = null;
							if (!((qTheme.getCode().equals("THM_FORM_DEFAULT")) || (qTheme.getCode().equals("THM_BUTTONS"))
									|| (qTheme.getCode().equals("THM_FORM_CONTAINER_DEFAULT")))) {
								vcl = qTheme.getVcl();
							}
							createVirtualContext(ask, themeSet, ContextType.THEME, vcl, qTheme.getWeight());
						}

					}

				}
				Set<EntityQuestion> entityQuestionList = askBe.getQuestions();

				Link linkAsk = new Link(frame.getCode(), childFrame.getQuestionCode(), "LNK_ASK",
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
		for (Tuple4<String, ThemeAttributeType, JSONObject, Double> themeTuple4 : frame.getThemeObjects()) {
			System.out.println("Processing Theme     " + themeTuple4._1);
			String themeCode = themeTuple4._1;
			ThemeAttributeType themeAttribute = themeTuple4._2;
			if (!themeAttribute.name().equalsIgnoreCase("codeOnly")) {
				JSONObject themeJson = themeTuple4._3;
				Double weight = themeTuple4._4;

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
		for (Tuple2<Theme, Double> themeTuple2 : frame.getThemes()) {
			System.out.println("Processing Theme     " + themeTuple2._1.getCode());
			Theme theme = themeTuple2._1;
			Double weight = themeTuple2._2;

			BaseEntity themeBe = getBaseEntity(theme.getCode(), theme.getCode(), gennyToken);

			for (ThemeAttribute themeAttribute : theme.getAttributes()) {
				Attribute attribute = null;


				attribute = RulesUtils.getAttribute(themeAttribute.getCode(),gennyToken.getToken());

				if (attribute == null) { attribute = new Attribute(themeAttribute.getCode(), themeAttribute.getCode(),
						new DataType("DTT_THEME"));
				}

				try {
					if (themeBe.containsEntityAttribute(themeAttribute.getCode())) {
						if ("PRI_IS_INHERITABLE".equals(themeAttribute.getCode())) {
							log.info("here");
						}
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
							themeBe.addAttribute(new EntityAttribute(themeBe, attribute, weight, themeAttribute.getValueBoolean().get()));
						} else 	if (attribute.dataType.getClassName().equals(Double.class.getCanonicalName())) {
							themeBe.addAttribute(new EntityAttribute(themeBe, attribute, weight, themeAttribute.getValueDouble()));
						} else 	if (attribute.dataType.getClassName().equals(String.class.getCanonicalName())) {
							themeBe.addAttribute(new EntityAttribute(themeBe, attribute, weight, themeAttribute.getValueString()));
						} else{
							themeBe.addAttribute(new EntityAttribute(themeBe,  new Attribute(themeAttribute.getCode(), themeAttribute.getCode(),
									new DataType("DTT_THEME")), weight, themeAttribute.getJson()));
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
					link = new EntityEntity(parent, themeBe, linkFrame, "FRAME", weight);
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
		if ("THM_FORM_CONTAINER_DEFAULT".equals(qTheme.getCode())) {
			log.info("info");
		}

		if (qTheme.getTheme().isPresent()) {
			Theme theme = qTheme.getTheme().get();
			theme.setRealm(fquestion.getRealm());

			System.out.println("Processing Theme     " + theme.getCode());
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

			if ("THM_FORM_INPUT_DEFAULT".equals(qTheme.getCode())) {
				log.info("hre");
				;
			}

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

		List<Context> completeContext = new ArrayList<>();

		for (BaseEntity theme : themes) {
			Context context = new Context(linkCode, theme, visualControlType, weight);
			context.setRealm(ask.getRealm());
			completeContext.add(context);

			/* publish the theme baseentity message */
			// QDataBaseEntityMessage themeMsg = new QDataBaseEntityMessage(theme);
			// publishCmd(themeMsg);
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
