package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Link;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityQuestion;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaMessage;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.VertxUtils;

public class QuestionUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private QuestionUtils() {
	}

	public static Boolean doesQuestionGroupExist(String realm,String sourceCode, String targetCode, final String questionCode,
			GennyToken token) {

		/* we grab the question group using the questionCode */
		QDataAskMessage questions = getAsks(realm,sourceCode, targetCode, questionCode, token);

		/* we check if the question payload is not empty */
		if (questions != null) {

			/* we check if the question group contains at least one question */
			if (questions.getItems() != null && questions.getItems().length > 0) {

				Ask firstQuestion = questions.getItems()[0];

				/* we check if the question is a question group */
				if (firstQuestion.getAttributeCode().contains("QQQ_QUESTION_GROUP_BUTTON_SUBMIT")) {

					/* we see if this group contains at least one question */
					return firstQuestion.getChildAsks().length > 0;
				} else {

					/* if it is an ask we return true */
					return true;
				}
			}
		}

		/* we return false otherwise */
		return false;
	}

	public static void setCachedQuestionsRecursively(Ask ask, GennyToken token) {
//		if (((ask.getChildAsks() != null) && (ask.getChildAsks().length > 0)) 
//		|| (ask.getAttributeCode().equals("QQQ_QUESTION_GROUP"))) {
			
		if (ask.getAttributeCode().equals("QQQ_QUESTION_GROUP")) {
			for (Ask childAsk : ask.getChildAsks()) {
				setCachedQuestionsRecursively(childAsk, token);
			}
		} else {
			Question question = ask.getQuestion();
			String questionCode = question.getCode();
			
			JsonObject jsonQuestion = VertxUtils.readCachedJson(token.getRealm(), questionCode, token);
			if ("ok".equalsIgnoreCase(jsonQuestion.getString("status"))) {
				Question cachedQuestion = JsonUtils.fromJson(jsonQuestion.getString("value"), Question.class);
				// Make sure we grab the icon too
				if (question.getIcon() != null) {
					if (!question.getIcon().equals(cachedQuestion.getIcon())) {
						cachedQuestion.setIcon(question.getIcon());
					}
				}
				ask.setQuestion(cachedQuestion);
				ask.setContextList(cachedQuestion.getContextList());
			}
		}
	}

	public static QDataAskMessage getAsks(String realm,String sourceCode, String targetCode, String questionCode, GennyToken token) {

		String json;
		try {

			json = QwandaUtils.apiGet(GennySettings.fyodorServiceUrl + "/qwanda/baseentitys/" + sourceCode + "/asks2/"
					+ questionCode + "/" + targetCode, token);
			if (json != null) {
				if (!json.contains("<title>Error")) {
					QDataAskMessage msg = JsonUtils.fromJson(json, QDataAskMessage.class);

					if (true) {
						// Identify all the attributeCodes and build up a working active Set
						Set<String> activeAttributeCodes = new HashSet<String>();
						for (Ask ask : msg.getItems()) {
							activeAttributeCodes.addAll(getAttributeCodes(ask));

							// Go down through the child asks and get cached questions
							setCachedQuestionsRecursively(ask, token);
						}
						// Now fetch the set from cache and add it....
						Type type = new TypeToken<Set<String>>() {
						}.getType();
						Set<String> activeAttributesSet = VertxUtils.getObject(token.getRealm(), "",
								"ACTIVE_ATTRIBUTES", type, token);
						if (activeAttributesSet == null) {
							activeAttributesSet = new HashSet<String>();
						}
						activeAttributesSet.addAll(activeAttributeCodes);

						VertxUtils.putObject(token.getRealm(), "", "ACTIVE_ATTRIBUTES", activeAttributesSet, token);

						log.debug("Total Active AttributeCodes = " + activeAttributesSet.size());
					}
					return msg;
				}
			}
		} catch (ClientProtocolException e) {
			log.info(e.getMessage());
		} catch (IOException e) {
			log.info(e.getMessage());
		}

		return null;
	}

	private static Set<String> getAttributeCodes(Ask ask) {
		Set<String> activeCodes = new HashSet<String>();
		activeCodes.add(ask.getAttributeCode());
		if ((ask.getChildAsks() != null) && (ask.getChildAsks().length > 0)) {
			for (Ask childAsk : ask.getChildAsks()) {
				activeCodes.addAll(getAttributeCodes(childAsk));
			}
		}
		return activeCodes;
	}

	public static QwandaMessage getQuestions(String realm,String sourceCode, String targetCode, String questionCode, GennyToken token)
			throws ClientProtocolException, IOException {
		return getQuestions(realm,sourceCode, targetCode, questionCode, token, null, true);
	}

	public static QwandaMessage getQuestions(final String realm,String sourceCode, String targetCode, String questionCode, GennyToken gennyToken,
			String stakeholderCode, Boolean pushSelection) throws ClientProtocolException, IOException {

		QBulkMessage bulk = new QBulkMessage();
		QwandaMessage qwandaMessage = new QwandaMessage();
		
		gennyToken.setProjectCode(realm);


		long startTime2 = System.nanoTime();

		QDataAskMessage questions = getAsks(realm,sourceCode, targetCode, questionCode, gennyToken);
		long endTime2 = System.nanoTime();
		double difference2 = (endTime2 - startTime2) / 1e6; // get ms
		RulesUtils.println("getAsks fetch Time = " + difference2 + " ms");

		if (questions != null) {

			/*
			 * if we have the questions, we loop through the asks and send the required data
			 * to front end
			 */
			long startTime = System.nanoTime();
			Ask[] asks = questions.getItems();
			if (asks != null && pushSelection) {
				QBulkMessage askData = sendAsksRequiredData(realm,asks, gennyToken, stakeholderCode);
				for (QDataBaseEntityMessage message : askData.getMessages()) {
					bulk.add(message);
				}
			}
			long endTime = System.nanoTime();
			double difference = (endTime - startTime) / 1e6; // get ms
			RulesUtils.println("sendAsksRequiredData fetch Time = " + difference + " ms");

			qwandaMessage.askData = bulk;
			qwandaMessage.asks = questions;

			return qwandaMessage;

		} else {
			log.error("Questions Msg is null " + sourceCode + "/asks2/" + questionCode + "/" + targetCode);
		}

		return null;
	}

	public static QwandaMessage askQuestions(final String realm,final String sourceCode, final String targetCode,
			final String questionGroupCode, GennyToken token) {
		return askQuestions(realm,sourceCode, targetCode, questionGroupCode, token, null, true);
	}

	public static QwandaMessage askQuestions(final String realm,final String sourceCode, final String targetCode,
			final String questionGroupCode, GennyToken token, String stakeholderCode) {
		return askQuestions(realm,sourceCode, targetCode, questionGroupCode, token, stakeholderCode, true);
	}

	public static QwandaMessage askQuestions(String realm,final String sourceCode, final String targetCode,
			final String questionGroupCode, Boolean pushSelection) {
		return askQuestions(realm,sourceCode, targetCode, questionGroupCode, null, null, pushSelection);
	}

	public static QwandaMessage askQuestions(String realm,final String sourceCode, final String targetCode,
			final String questionGroupCode, GennyToken token, Boolean pushSelection) {
		return askQuestions(realm,sourceCode, targetCode, questionGroupCode, token, null, pushSelection);
	}

	public static QwandaMessage askQuestions(final String realm,final String sourceCode, final String targetCode,
			final String questionGroupCode, final GennyToken token, final String stakeholderCode,
			final Boolean pushSelection) {

		try {

			/* if sending the questions worked, we ask user */
			return getQuestions(realm,sourceCode, targetCode, questionGroupCode, token, stakeholderCode, pushSelection);

		} catch (Exception e) {
			log.info("Ask questions exception: ");
			e.printStackTrace();
			return null;
		}
	}

	public static QwandaMessage setCustomQuestion(QwandaMessage questions, String questionAttributeCode,
			String customTemporaryQuestion) {

		if (questions != null && questionAttributeCode != null) {
			Ask[] askArr = questions.asks.getItems();
			if (askArr != null && askArr.length > 0) {
				for (Ask ask : askArr) {
					Ask[] childAskArr = ask.getChildAsks();
					if (childAskArr != null && childAskArr.length > 0) {
						for (Ask childAsk : childAskArr) {
							log.info("child ask code :: " + childAsk.getAttributeCode() + ", child ask name :: "
									+ childAsk.getName());
							if (childAsk.getAttributeCode().equals(questionAttributeCode)) {
								if (customTemporaryQuestion != null) {
									childAsk.getQuestion().setName(customTemporaryQuestion);
									return questions;
								}
							}
						}
					}
				}
			}
		}
		return questions;
	}

	private static QBulkMessage sendAsksRequiredData(final String realm,Ask[] asks, GennyToken token, String stakeholderCode) {

		QBulkMessage bulk = new QBulkMessage();
		token.setProjectCode(realm);

		/* we loop through the asks and send the required data if necessary */
		for (Ask ask : asks) {

			/*
			 * we get the attribute code. if it starts with "LNK_" it means it is a dropdown
			 * selection.
			 */

			String attributeCode = ask.getAttributeCode();
			if (attributeCode != null && attributeCode.startsWith("LNK_")) {

				/* we get the attribute validation to get the group code */
				Attribute attribute = RulesUtils.getAttribute(attributeCode, token);
				if (attribute != null) {

					/* grab the group in the validation */
					DataType attributeDataType = attribute.getDataType();
					if (attributeDataType != null) {

						List<Validation> validations = attributeDataType.getValidationList();

						/* we loop through the validations */
						for (Validation validation : validations) {

							List<String> validationStrings = validation.getSelectionBaseEntityGroupList();

							if (validationStrings != null) {
								for (String validationString : validationStrings) {

									if (validationString.startsWith("GRP_")) {

										/* Grab the parent */
										BaseEntity parent = CacheUtils.getBaseEntity(validationString, token);

										/* we have a GRP. we push it to FE */
										List<BaseEntity> bes = CacheUtils.getChildren(validationString, 2, token);
										List<BaseEntity> filteredBes = null;

										if (bes != null && bes.isEmpty() == false) {

											/* hard coding this for now. sorry */
											if ("LNK_LOAD_LISTS".equals(attributeCode) && stakeholderCode != null) {

												/* we filter load you only are a stakeholder of */
												filteredBes = bes.stream().filter(baseEntity -> {
													return baseEntity.getValue("PRI_AUTHOR", "")
															.equals(stakeholderCode);
												}).collect(Collectors.toList());
											} else {
												filteredBes = bes;
											}

											/* create message for base entities required for the validation */
											QDataBaseEntityMessage beMessage = new QDataBaseEntityMessage(filteredBes);
											beMessage.setLinkCode("LNK_CORE");
											beMessage.setParentCode(validationString);
											beMessage.setReplace(true);
											bulk.add(beMessage);

											/* create message for parent */
											QDataBaseEntityMessage parentMessage = new QDataBaseEntityMessage(parent);
											bulk.add(parentMessage);
										}
									}
								}
							}
						}
					}
				}
			}

			/* recursive call */
			Ask[] childAsks = ask.getChildAsks();
			if (childAsks != null && childAsks.length > 0) {

				QBulkMessage newBulk = sendAsksRequiredData(realm,childAsks, token, stakeholderCode);
				for (QDataBaseEntityMessage msg : newBulk.getMessages()) {
					bulk.add(msg);
				}
			}
		}

		return bulk;
	}

	// TODO: THIS IS A DUPLICATE METHOD WILL REVIEW LATER
	// public static Ask createQuestionForBaseEntity(BaseEntity be, Boolean isQuestionGroup, GennyToken token) {

	// 	/* Get the service token */
	// 	GennyToken serviceToken = RulesUtils.generateServiceToken(be.getRealm(), token);

	// 	/* creating attribute code according to the value of isQuestionGroup */
	// 	String attributeCode = isQuestionGroup ? "QQQ_QUESTION_GROUP_INPUT" : "PRI_EVENT";

	// 	/* Get the on-the-fly question attribute */
	// 	Attribute attribute = RulesUtils.getAttribute(attributeCode, serviceToken);
	// 	log.debug("createQuestionForBaseEntity method, attribute ::" + JsonUtils.toJson(attribute));

	// 	/*
	// 	 * creating suffix according to value of isQuestionGroup. If it is a
	// 	 * question-group, suffix "_GRP" is required"
	// 	 */
	// 	String questionSuffix = isQuestionGroup ? "_GRP" : "";

	// 	/* We generate the question */
	// 	Question newQuestion = new Question("QUE_" + be.getCode() + questionSuffix, be.getName(), attribute, false);
	// 	log.debug("createQuestionForBaseEntity method, newQuestion ::" + JsonUtils.toJson(newQuestion));

	// 	/* We generate the ask */
	// 	return new Ask(newQuestion, be.getCode(), be.getCode(), false, 1.0, false, false, true);
	// }

	public static Ask createQuestionForBaseEntity(BaseEntity be, Boolean isQuestionGroup, GennyToken serviceToken) {

		/* creating attribute code according to the value of isQuestionGroup */
		String attributeCode = isQuestionGroup ? "QQQ_QUESTION_GROUP_INPUT" : "PRI_EVENT";

		/* Get the on-the-fly question attribute */
		Attribute attribute = RulesUtils.getAttribute(attributeCode, serviceToken);
		log.debug("createQuestionForBaseEntity method, attribute ::" + JsonUtils.toJson(attribute));

		/*
		 * creating suffix according to value of isQuestionGroup. If it is a
		 * question-group, suffix "_GRP" is required"
		 */
		String questionSuffix = isQuestionGroup ? "_GRP" : "";

		/* We generate the question */
		Question newQuestion = new Question("QUE_" + be.getCode() + questionSuffix, be.getName(), attribute, false);
		log.debug("createQuestionForBaseEntity method, newQuestion ::" + JsonUtils.toJson(newQuestion));

		/* We generate the ask */
		return new Ask(newQuestion, be.getCode(), be.getCode(), false, 1.0, false, false, true);
	}

	public static Ask createQuestionForBaseEntity2(BaseEntity be, Boolean isQuestionGroup, GennyToken serviceToken,
			final String sourceAlias, final String targetAlias) {

		/* creating attribute code according to the value of isQuestionGroup */
		String attributeCode = isQuestionGroup ? "QQQ_QUESTION_GROUP_INPUT" : "PRI_EVENT";

		/* Get the on-the-fly question attribute */
		Attribute attribute = null;
		if (!VertxUtils.cachedEnabled) {
			attribute = RulesUtils.getAttribute(attributeCode, serviceToken);
			log.debug("createQuestionForBaseEntity method, attribute ::" + JsonUtils.toJson(attribute));
		} else {
			attribute = new Attribute(attributeCode, attributeCode, new DataType("DTT_THEME")); // this helps junit
																								// testing
		}

		if (attribute == null) {
			log.error("Attribute DOES NOT EXIST! " + attributeCode + " creating temp");
			// ugly
			attribute = new Attribute(attributeCode, attributeCode, new DataType("DTT_THEME")); // this helps junit
																								// testing

		}

		/* We generate the question */
		Question newQuestion = new Question(be.getCode(), be.getName(), attribute, false);
		log.debug("createQuestionForBaseEntity method, newQuestion ::" + JsonUtils.toJson(newQuestion));

		/* We generate the ask */
		Ask ask = new Ask(newQuestion, (sourceAlias != null ? sourceAlias : be.getCode()),
				(targetAlias != null ? targetAlias : be.getCode()), false, 1.0, false, false, true);
		ask.setRealm(serviceToken.getRealm());
		return ask;

	}

	public static Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode,
			VisualControlType visualControlType, Double weight) {

		List<Context> completeContext = new ArrayList<>();

		Context context = new Context(linkCode, theme, visualControlType, weight);
		completeContext.add(context);

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

	public static BaseEntity createVirtualLink(BaseEntity source, Ask ask, String linkCode, String linkValue) {

		if (source != null) {

			Set<EntityQuestion> entityQuestionList = source.getQuestions();

			Link link = new Link(source.getCode(), ask.getQuestion().getCode(), linkCode, linkValue);
			link.setWeight(ask.getWeight());
			EntityQuestion ee = new EntityQuestion(link);
			entityQuestionList.add(ee);

			source.setQuestions(entityQuestionList);
		}
		return source;
	}

	public static Question upsertQuestion(Question question, GennyToken token) {
		if (question != null) {
			String json = null;
			try {
				json = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/questions",
						JsonUtils.toJson(question), token);
			} catch (IOException e) {
				log.info("Caught IOException trying to upsert question: " + question.getCode());
			}
			if (json != null) {
				if (!json.contains("<title>Error")) {
					log.info("Error upserting question: " + question.getCode());
				}
			}
		} else {
			log.info("Question must not be null!");
		}
		return question;
	}

	static public Question getQuestion(String questionCode, GennyToken userToken) {

		Question q = null;
		Integer retry = 2;
		while (retry >= 0) { // Sometimes q is read properly from cache
			JsonObject jsonQ = VertxUtils.readCachedJson(userToken.getRealm(), questionCode, userToken);
			q = JsonUtils.fromJson(jsonQ.getString("value"), Question.class);
			if (q == null) {
				retry--;

			} else {
				break;
			}

		}

		if (q == null) {
			log.warn("COULD NOT READ " + questionCode + " from cache!!! Aborting (after having tried 2 times");
			String qJson;
			try {
				qJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl+"/qwanda/questioncodes/"+questionCode, userToken);
				if (!StringUtils.isBlank(qJson)) {
					q = JsonUtils.fromJson(qJson, Question.class);
					VertxUtils.writeCachedJson(userToken.getRealm(), questionCode, JsonUtils.toJson(q),userToken);
					log.info("WRITTEN " + questionCode + " tocache!!! Fetched from database");
					return q;
				} else {
					log.error("Questionutils could not find question "+questionCode+" in database");
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		} else {
			return q;
		}
	}

	/**
	 * Generate Question group for a baseEntity
	 *
	 * @param baseEntity the baseEntity to create for
	 * @param beUtils the utility to use
	 */
	public static Ask generateAskGroupUsingBaseEntity(BaseEntity baseEntity, BaseEntityUtils beUtils) {

		// init tokens
		GennyToken userToken = beUtils.getGennyToken();
		String token = userToken.getToken();
		userToken.setProjectCode(baseEntity.getRealm());

		// grab def entity
		BaseEntity defBE = beUtils.getDEF(baseEntity);

		// create GRP ask
		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", userToken);
		Question question = new Question("QUE_EDIT_GRP", "Edit " + baseEntity.getCode() + " : " + baseEntity.getName(), questionAttribute);
		Ask ask = new Ask(question, userToken.getUserCode(), baseEntity.getCode());

		List<Ask> childAsks = new ArrayList<>();
		QDataBaseEntityMessage entityMessage = new QDataBaseEntityMessage();
		entityMessage.setToken(token);
		entityMessage.setReplace(true);

		// create a child ask for every valid atribute
		baseEntity.getBaseEntityAttributes().stream()
			.filter(ea -> defBE.containsEntityAttribute("ATT_" + ea.getAttributeCode()))
			.forEach((ea) -> {

				String questionCode = "QUE_" + StringUtils.removeStart(StringUtils.removeStart(ea.getAttributeCode(), "PRI_"), "LNK_");

				Question childQues = new Question(questionCode, ea.getAttribute().getName(), ea.getAttribute());
				Ask childAsk = new Ask(childQues, userToken.getUserCode(), baseEntity.getCode());

				childAsks.add(childAsk);

				if (ea.getAttributeCode().startsWith("LNK_")) {
					if (ea.getValueString() != null) {

						String[] codes = beUtils.cleanUpAttributeValue(ea.getValueString()).split(",");

						for (String code : codes) {
							BaseEntity link = beUtils.getBaseEntityByCode(code);
							entityMessage.add(link);
						}
					}
				}

				// if (defBE.containsEntityAttribute("SER_" + ea.getAttributeCode())) {
				// 	SearchUtils.performDropdownSearch(childAsk, userToken);
				// }
			});

		// set child asks
		ask.setChildAsks(childAsks.toArray(new Ask[childAsks.size()]));

		VertxUtils.writeMsg("webdata", entityMessage);

		return ask;
	}
	
}
