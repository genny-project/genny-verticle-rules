package life.genny.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.google.gson.annotations.Expose;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import life.genny.models.QuestionGroup;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.QuestionGroup.Builder;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.utils.VertxUtils;

@Immutable
public class Frame3 extends BaseEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	private String questionCode;
	@Expose
	private String questionName;
	@Expose
	private QuestionGroup questionGroup = null;
	@Expose
	private FramePosition position;
	@Expose
	private BaseEntity parent;

	@Expose
	private ArrayList<ThemeTuple4> themeObjects = new ArrayList<ThemeTuple4>();
	@Expose
	private ArrayList<ThemeDouble> themes = new ArrayList<ThemeDouble>();
	@Expose
	private ArrayList<StringTuple> frameCodes = new ArrayList<StringTuple>();
	@Expose
	private ArrayList<FrameTuple3> frames = new ArrayList<FrameTuple3>();

	@Expose
	private List<Frame3> frame3s;
	@Expose
	private List<Theme> theme3s;
	@Expose
	private Double weight;
	
	/**
	 * @return the questionCode
	 */
	public void setQuestionCode(final String questionCode) {
		this.questionCode = questionCode;
		if (this.questionGroup!=null) {
			this.questionGroup.setCode(questionCode);
		}
	}

	
	
	public Double getWeight() {
		return weight;
	}



	public void setWeight(Double weight) {
		this.weight = weight;
	}



	/**
	 * static factory method for builder that never needs to load in a theme or a
	 * frame from a code
	 */
	public static Builder builder(final String code) {
		return new Frame3.Builder(code);
	}

	/**
	 * forces use of the Builder
	 */
	private Frame3() {
	}


	
	
	/**
	 * @return the questionCode
	 */
	public String getQuestionCode() {
		return questionCode;
	}

	
	
	/**
	 * @return the questionName
	 */
	public String getQuestionName() {
		return questionName;
	}

	/**
	 * @return the questionGroup
	 */
	public QuestionGroup getQuestionGroup() {
		return questionGroup;
	}

	/**
	 * @return the position
	 */
	public FramePosition getPosition() {
		return position;
	}

	/**
	 * @return the parent
	 */
	public BaseEntity getParent() {
		return parent;
	}

	/**
	 * @return the themeObjects
	 */
	public List<ThemeTuple4> getThemeObjects() {
		return themeObjects;
	}

	/**
	 * @return the themes
	 */
	public ArrayList<ThemeDouble> getThemes() {
		return themes;
	}

	/**
	 * @return the frameCodes
	 */
	public List<StringTuple> getFrameCodes() {
		return frameCodes;
	}

	/**
	 * @return the frames
	 */
	public List<FrameTuple3> getFrames() {
		return frames;
	}

	public List<Frame3> getFrame3s() {
		return Collections.unmodifiableList(frame3s);
	}

	public List<Theme> getTheme3s() {
		return Collections.unmodifiableList(theme3s);
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(BaseEntity parent) {
		this.parent = parent;
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {
		Double frameWeight = 1.0; // used to set the order
		Double themeWeight = 1000.0; // themes weight goes backward

		private Frame3 managedInstance = new Frame3();
		private Frame3.Builder parentBuilder;
		private Consumer<Frame3> callback;

		public Builder(final String code) {
			managedInstance.setCode(code);
			managedInstance.setName(StringUtils.capitalize(code.substring(4)));
		}

		public Builder(Frame3.Builder b, Consumer<Frame3> c, String frameCode) {
			managedInstance.setCode(frameCode);

			FrameTuple3 frameTuple = new FrameTuple3(managedInstance, FramePosition.CENTRE, b.frameWeight);
			b.managedInstance.frames.add(frameTuple);
			b.frameWeight = b.frameWeight + 1.0;
			managedInstance.setWeight(frameWeight);
			parentBuilder = b;
			callback = c;
		}

		public Builder(Frame3.Builder b, Consumer<Frame3> c, String frameCode, FramePosition position) {
			managedInstance.setCode(frameCode);
			FrameTuple3 frameTuple = new FrameTuple3(managedInstance, position, b.frameWeight);
			b.managedInstance.frames.add(frameTuple);
			b.frameWeight = b.frameWeight + 1.0;
			managedInstance.setWeight(frameWeight);

			parentBuilder = b;
			callback = c;
		}

		public Builder(Frame3.Builder b, Consumer<Frame3> c, Frame3 frame, FramePosition position) {
			managedInstance = frame;
			FrameTuple3 frameTuple = new FrameTuple3(managedInstance, position, b.frameWeight);
			b.managedInstance.frames.add(frameTuple);
			b.frameWeight = b.frameWeight + 1.0;
			managedInstance.setWeight(frameWeight);

			parentBuilder = b;
			callback = c;
		}

		/**
		 * fluent setter for frameCodes in the list
		 *
		 * @param none
		 * @return
		 * @throws Exception
		 */
		public Frame3.Builder addFrame(String frameCode, GennyToken serviceToken) throws Exception {

			return addFrame(frameCode, FramePosition.CENTRE, serviceToken);
		}

	
		/**
		 * fluent setter for frameCodes in the list
		 *
		 * @param none
		 * @return
		 * @throws Exception
		 */
		public Frame3.Builder addFrame(String frameCode, FramePosition position, GennyToken serviceToken) throws Exception {
			if (managedInstance.frame3s == null) {
				managedInstance.frame3s = new ArrayList<Frame3>();
			}
			Frame3 frame = null;
			frame = VertxUtils.getObject(serviceToken.getRealm(), "", frameCode, Frame3.class, serviceToken.getToken());
			if (frame != null) {

			} else {
				throw new Exception("Could not load Frame " + frameCode + " - Does it exist yet?");
			}

			Consumer<Frame3> f = obj -> {
				managedInstance.frame3s.add(obj);
			};

			return new Frame3.Builder(this, f, frame, position);
		}

		/**
		 * fluent setter for frames in the list
		 *
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(Frame3 frame) {
			return addFrame(frame, FramePosition.CENTRE);
		}

		/**
		 * fluent setter for frames in the list
		 *
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(Frame3... frames) {
			for (Frame3 frame : frames) {
				addFrame(frame, FramePosition.CENTRE);
			}
			return this;
		}
		
		/**
		 * fluent setter for frames in the list
		 *
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(List<Frame3> frames) {
			for (Frame3 frame : frames) {
				addFrame(frame, FramePosition.CENTRE);
			}
			return this;
		}

		
		/**
		 * fluent setter for frames in the list
		 *
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(Frame3 frame, FramePosition position) {
			if (managedInstance.frame3s == null) {
				managedInstance.frame3s = new ArrayList<Frame3>();
			}
			Consumer<Frame3> f = obj -> {
				managedInstance.frame3s.add(obj);
			};
			return new Frame3.Builder(this, f, frame, position);
		}

		/**
		 * fluent setter for themes in the list
		 *
		 * @param none
		 * @return
		 */
		public Theme.Builder addThemeParent(Theme theme) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};
			managedInstance.themes.add(new ThemeDouble(theme, ThemePosition.FRAME, themeWeight));
			themeWeight = themeWeight - 1.0;

			return new Theme.Builder(this, f, theme);
		}

		/**
		 * fluent setter for themes in the list
		 *
		 * @param none
		 * @return
		 * @throws Exception
		 */
		public Theme.Builder addTheme(String themeCode, GennyToken serviceToken) throws Exception {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};
			Theme theme = VertxUtils.getObject(serviceToken.getRealm(), "", themeCode, Theme.class, serviceToken.getToken());
			if (theme != null) {
				theme.setDirectLink(true);
				managedInstance.themes.add(new ThemeDouble(theme, ThemePosition.FRAME, themeWeight));
				themeWeight = themeWeight - 1.0;
			} else {
				throw new Exception("Could not load Theme " + themeCode + " - Does it exist yet?");
			}
			return new Theme.Builder(this, f, theme);
		}

		/**
		 * fluent setter for themes in the list
		 *
		 * @param none
		 * @return
		 * @throws Exception
		 */
		public Theme.Builder addTheme(String themeCode, ThemePosition themePosition, GennyToken serviceToken)
				throws Exception {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};
			Theme theme = VertxUtils.getObject(serviceToken.getRealm(), "", themeCode, Theme.class, serviceToken.getToken());
			if (theme != null) {
				theme.setDirectLink(true);
				managedInstance.themes.add(new ThemeDouble(theme, themePosition, themeWeight));
				themeWeight = themeWeight - 1.0;
			} else {
				throw new Exception("Could not load Theme " + themeCode + " - Does it exist yet?");
			}
			return new Theme.Builder(this, f, theme);
		}


		/**
		 * fluent setter for themes in the list
		 *
		 * @param none
		 * @return
		 */
		public Theme.Builder addTheme(Theme theme) {
			return addTheme(theme, ThemePosition.FRAME);
		}

		/**
		 * fluent setter for themes in the list
		 *
		 * @param none
		 * @return
		 */
		public Theme.Builder addTheme(Theme theme, ThemePosition themePosition) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};
			theme.setDirectLink(true);
			managedInstance.themes.add(new ThemeDouble(theme, themePosition, themeWeight));
			themeWeight = themeWeight - 1.0;

			return new Theme.Builder(this, f, theme);
		}

		/**
		 * fluent setter for themes in the list
		 *
		 * @param none
		 * @return
		 */
		public Theme.Builder addThemeParent() {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};
			String themeCode = "THM_" + UUID.randomUUID().toString().substring(0, 25);
			Theme theme = Theme.builder(themeCode).build();
			managedInstance.themes.add(new ThemeDouble(theme, ThemePosition.FRAME, themeWeight));
			themeWeight = themeWeight - 1.0;

			return new Theme.Builder(this, f, theme);
		}

		/**
		 * fluent setter for themeCodes in the list
		 *
		 * @param none
		 * @return
		 */
		public Theme.Builder addThemeParent(String themeCode) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};
			ThemeAttributeType codeOnly = ThemeAttributeType.codeOnly;
			ThemeTuple4 theme = new ThemeTuple4(themeCode, codeOnly, new JSONObject("{\"codeOnly\":true}"), themeWeight);

			managedInstance.themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;

			return new Theme.Builder(this, f, themeCode);
			// return
			// addTheme(themeCode,ThemeAttributeType.PRI_CONTENT,ThemeAttributeType.codeOnly,new
			// JSONObject("{\"codeOnly\":true}"));
		}

		public Theme.Builder addThemeParent(final String themeCode, String property, Object value) {
			return addThemeParent(themeCode, ThemeAttributeType.PRI_CONTENT, property, value);
		}

		public Theme.Builder addThemeParent(final String themeCode, ThemeAttributeType attributeCode, String property,
				Object value) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> {
				managedInstance.theme3s.add(obj);
			};

			JSONObject keyValue = new JSONObject();

			keyValue.put(property, value);

			ThemeTuple4 theme = new ThemeTuple4(themeCode, attributeCode, keyValue, themeWeight);
			managedInstance.themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;
			return new Theme.Builder(this, f, themeCode);
		}

		/**
		 * more fluent setter for QuestionGroup
		 *
		 * @return
		 */
		public QuestionGroup.Builder question(final String questionCode) {
			Consumer<QuestionGroup> f = obj -> {
				managedInstance.questionGroup = obj;
			};
			managedInstance.questionCode = questionCode;
			return new QuestionGroup.Builder(this, f, questionCode);
		}
		
		/**
		 * more fluent setter for QuestionGroup
		 *
		 * @return
		 */
		public QuestionGroup.Builder question(final String attributeCode, final String questionName) {
			Consumer<QuestionGroup> f = obj -> {
				managedInstance.questionGroup = obj;
			};
			// Construction virtual Question 

			managedInstance.questionCode = attributeCode;
			managedInstance.questionName = questionName;
			return new QuestionGroup.Builder(this, f, attributeCode, questionName);
		}

		public Frame3.Builder end() {
			callback.accept(managedInstance);
			return parentBuilder;
		}

		public Frame3 build() {
			return managedInstance;
		}

	}

	@Override
	public String toString() {
		return getCode();
	}

}