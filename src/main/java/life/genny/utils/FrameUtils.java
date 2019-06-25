package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple3;
import io.vavr.Tuple4;
import life.genny.models.GennyToken;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.llama.Frame;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.QwandaUtils;

public class FrameUtils {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static public QDataBaseEntityMessage toMessage(final Frame rootFrame, GennyToken gennyToken) {

		List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();

		BaseEntity root = getBaseEntity(rootFrame, gennyToken);

		log.info(root.toString());

		baseEntityList.add(root);
		
		// Traverse the frame tree and build BaseEntitys and links
		processFrames(rootFrame, gennyToken, baseEntityList, root);

		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(baseEntityList);

		return msg;
	}

	private static BaseEntity getBaseEntity(final Frame rootFrame, final GennyToken gennyToken) {
		return getBaseEntity(rootFrame.getCode(), rootFrame.getName(), gennyToken);

	}

	private static BaseEntity getBaseEntity(final String beCode, final String beName, final GennyToken gennyToken) {
		BaseEntity be = VertxUtils.getObject(gennyToken.getRealm(), "", beCode, BaseEntity.class,
				gennyToken.getToken());
		if (be == null) {
			try {
				be = QwandaUtils.getBaseEntityByCodeWithAttributes(beCode, gennyToken.getToken());
				if (be == null) {
					be = QwandaUtils.createBaseEntityByCode(beCode, beName, GennySettings.qwandaServiceUrl,
							gennyToken.getToken());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return be;

	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processFrames(final Frame frame, GennyToken gennyToken, List<BaseEntity> baseEntityList,
			BaseEntity parent) {
		// Go through the frames and fetch them

		for (Tuple3<Frame, Frame.FramePosition, Double> frameTuple3 : frame.getFrames()) {
			System.out.println("Process Frame " + frameTuple3._1.getCode());
			Frame childFrame = frameTuple3._1;
			Frame.FramePosition position = frameTuple3._2;
			Double weight = frameTuple3._3;

			childFrame.setParent(parent); // Set the parent sop that we can link the childs themes to it.

			BaseEntity childBe = getBaseEntity(childFrame, gennyToken);

			// link to the parent
			EntityEntity link = null;
			Attribute linkFrame = new AttributeLink("LNK_FRAME", "frame");
			link = new EntityEntity(parent, childBe, linkFrame, position.name(), weight);
			parent.getLinks().add(link);
			baseEntityList.add(childBe);

			// Traverse the frame tree and build BaseEntitys and links
			if (!childFrame.getFrames().isEmpty()) {
				processFrames(childFrame, gennyToken, baseEntityList, childBe);
			}

			if (!childFrame.getThemeObjects().isEmpty()) {
				processThemes(childFrame, position, gennyToken, baseEntityList, childBe);
			}

			System.out.println("Processed " + frame.getCode());

		}
	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processThemes(final Frame frame, Frame.FramePosition position, GennyToken gennyToken,
			List<BaseEntity> baseEntityList, BaseEntity parent) {
		// Go through the theme codes and fetch the
		for (Tuple4<String, Frame.ThemeAttribute, JSONObject, Double> themeTuple4 : frame.getThemeObjects()) {
			System.out.println("Process Theme " + themeTuple4._1);
			String themeCode = themeTuple4._1;
			Frame.ThemeAttribute themeAttribute = themeTuple4._2;
			if (!themeAttribute.name().equalsIgnoreCase("codeOnly")) {
				JSONObject themeJson = themeTuple4._3;
				Double weight = themeTuple4._4;

				BaseEntity childBe = getBaseEntity(themeCode, themeCode, gennyToken);

				// Attribute attribute = RulesUtils.getAttribute(themeAttribute.name(),
				// gennyToken.getToken());
				Attribute attribute = new Attribute(themeAttribute.name(), themeAttribute.name(),
						new DataType("DTT_THEME"));
				try {
					if (childBe.containsEntityAttribute(themeAttribute.name())) {
						EntityAttribute themeEA = childBe.findEntityAttribute(themeAttribute.name()).get();
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
						childBe.addAttribute(new EntityAttribute(childBe, attribute, weight, themeJson.toString()));
					}
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// link to the parent
				EntityEntity link = null;
				Attribute linkFrame = new AttributeLink("LNK_THEME", "theme");
				link = new EntityEntity(frame.getParent(), childBe, linkFrame, position.name(), weight);
				frame.getParent().getLinks().add(link);
				baseEntityList.add(childBe);

			}
		}
	}

}
