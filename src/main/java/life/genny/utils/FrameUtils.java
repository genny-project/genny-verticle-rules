package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import life.genny.models.GennyToken;
import life.genny.qwanda.Link;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.llama.Frame;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.QwandaUtils;

public class FrameUtils {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static public QBulkMessage toMessage(final Frame frame, GennyToken gennyToken) {
		
	
		List<QDataBaseEntityMessage> messages = new ArrayList<QDataBaseEntityMessage>();
		// Traverse the frame tree and build BaseEntitys and links

		BaseEntity root = VertxUtils.getObject(gennyToken.getRealm(), "", frame.getCode(), BaseEntity.class,
				gennyToken.getToken());
		if (root == null) {
			try {
				root = QwandaUtils.getBaseEntityByCodeWithAttributes(frame.getCode(), gennyToken.getToken());
				if (root == null) {
					root = QwandaUtils.createBaseEntityByCode(frame.getCode(), frame.getName(),
							GennySettings.qwandaServiceUrl, gennyToken.getToken());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		log.info(root.toString());


		processCodeFrames(frame, gennyToken, messages, root);
		
		processFrames(frame, gennyToken, messages, root);

		processCodeThemes(frame, gennyToken, messages, root);

		
		// Now fetch the root with all the links
		try {
			root = QwandaUtils.getBaseEntityByCodeWithAttributes(frame.getCode(), gennyToken.getToken());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		QBulkMessage bulkMsg = new QBulkMessage(messages);
		
	
		return bulkMsg;
	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processFrames(final Frame frame, GennyToken gennyToken, List<QDataBaseEntityMessage> messages,
			BaseEntity root) {
		// Go through the frames and fetch them
		for (Tuple2<Frame, Frame.FramePosition> frameTuple2 : frame.getFrames()) {
			Frame theChildFrame = frameTuple2._1;
			Frame.FramePosition position = frameTuple2._2;

			BaseEntity childFrame = VertxUtils.getObject(gennyToken.getRealm(), "", theChildFrame.getCode(), BaseEntity.class,
					gennyToken.getToken());

			if (childFrame == null) {
				theChildFrame.setRealm(gennyToken.getRealm());

				try {
					childFrame = QwandaUtils.getBaseEntityByCodeWithAttributes(theChildFrame.getCode(), gennyToken.getToken());
					if (childFrame == null) {
						Long cid  = QwandaUtils.postBaseEntity(GennySettings.qwandaServiceUrl, gennyToken.getToken(), (BaseEntity)theChildFrame);
						childFrame = QwandaUtils.getBaseEntityByCodeWithAttributes(theChildFrame.getCode(), gennyToken.getToken());
					
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// link to the root
			if (!QwandaUtils.checkIfLinkExists(root.getCode(), "LNK_FRAME", theChildFrame.getCode(), position.name(),
					gennyToken.getToken())) {
				// Create a link
				log.info("No Link detected");
				Link link = new Link(root.getCode(), theChildFrame.getCode(), "LNK_FRAME", position.name());
				try {
					QwandaUtils.postLink(GennySettings.qwandaServiceUrl, gennyToken.getToken(), link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				log.info("Link detected from " + root.getCode() + " to " + childFrame.getCode());
			}

			BaseEntity[] beArray = new BaseEntity[2];
			beArray[0] = root;
			beArray[1] = childFrame;
			QDataBaseEntityMessage baseEntityMessage = new QDataBaseEntityMessage(beArray, root.getCode(),
					"LNK_FRAME", position.name());
			messages.add(baseEntityMessage);
		}
	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processCodeFrames(final Frame frame, GennyToken gennyToken, List<QDataBaseEntityMessage> messages,
			BaseEntity root) {
		// Go through the frame codes and fetch the
		for (Tuple2<String, Frame.FramePosition> frameTuple2 : frame.getFrameCodes()) {
			String frameCode = frameTuple2._1;
			Frame.FramePosition position = frameTuple2._2;

			BaseEntity childFrame = VertxUtils.getObject(gennyToken.getRealm(), "", frameCode, BaseEntity.class,
					gennyToken.getToken());

			if (childFrame == null) {
				childFrame = new BaseEntity(frameCode, frameCode);
				childFrame.setRealm(gennyToken.getRealm());

				try {
					childFrame = QwandaUtils.getBaseEntityByCodeWithAttributes(frameCode, gennyToken.getToken());
					if (childFrame == null) {
						childFrame = QwandaUtils.createBaseEntityByCode(frameCode, frameCode,
								GennySettings.qwandaServiceUrl, gennyToken.getToken());
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// link to the root
			if (!QwandaUtils.checkIfLinkExists(root.getCode(), "LNK_FRAME", frameCode, position.name(),
					gennyToken.getToken())) {
				// Create a link
				log.info("No Link detected");
				Link link = new Link(root.getCode(), frameCode, "LNK_FRAME", position.name());
				try {
					QwandaUtils.postLink(GennySettings.qwandaServiceUrl, gennyToken.getToken(), link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				log.info("Link detected from " + root.getCode() + " to " + childFrame.getCode());
			}

			BaseEntity[] beArray = new BaseEntity[2];
			beArray[0] = root;
			beArray[1] = childFrame;
			QDataBaseEntityMessage baseEntityMessage = new QDataBaseEntityMessage(beArray, root.getCode(),
					"LNK_FRAME", position.name());
			messages.add(baseEntityMessage);
		}
	}
	
	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processCodeThemes(final Frame frame, GennyToken gennyToken, List<QDataBaseEntityMessage> messages,
			BaseEntity root) {
		// Go through the theme codes and fetch the
		for (Tuple1<String> frameTuple2 : frame.getThemeCodes()) {
			String themeCode = frameTuple2._1;

			BaseEntity theme = VertxUtils.getObject(gennyToken.getRealm(), "", themeCode, BaseEntity.class,
					gennyToken.getToken());

			if (theme == null) {
				theme = new BaseEntity(themeCode, themeCode);
				theme.setRealm(gennyToken.getRealm());

				try {
					theme = QwandaUtils.getBaseEntityByCodeWithAttributes(themeCode, gennyToken.getToken());
					if (theme == null) {
						theme = QwandaUtils.createBaseEntityByCode(themeCode, themeCode,
								GennySettings.qwandaServiceUrl, gennyToken.getToken());
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// link to the root
			if (!QwandaUtils.checkIfLinkExists(root.getCode(), "LNK_THEME", themeCode, "link",
					gennyToken.getToken())) {
				// Create a link
				log.info("No Link detected");
				Link link = new Link(root.getCode(), themeCode, "LNK_FRAME", "link");
				try {
					QwandaUtils.postLink(GennySettings.qwandaServiceUrl, gennyToken.getToken(), link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				log.info("Link detected from " + root.getCode() + " to " + theme.getCode());
			}

			BaseEntity[] beArray = new BaseEntity[2];
			beArray[0] = root;
			beArray[1] = theme;
			QDataBaseEntityMessage baseEntityMessage = new QDataBaseEntityMessage(beArray, root.getCode(),
					"LNK_THEME", "link");
			messages.add(baseEntityMessage);
		}
	}
	


}
