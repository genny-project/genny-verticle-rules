package life.genny.models;


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
public class Frame3 extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String questionCode;
	private Optional<QuestionGroup> questionGroup = Optional.empty();
	private FramePosition position;
	private BaseEntity parent;
	private List<Tuple4<String, ThemeAttributeType,  JSONObject, Double>> themeObjects = new ArrayList<Tuple4<String, ThemeAttributeType, JSONObject, Double>>();
	private List<Tuple2<Theme,Double>> themes = new ArrayList<Tuple2<Theme,Double>>();
	
	private List<Tuple3<String, FramePosition, Double>> frameCodes = new ArrayList<Tuple3<String, FramePosition, Double>>();
	private List<Tuple3<Frame3, FramePosition, Double>> frames = new ArrayList<Tuple3<Frame3, FramePosition, Double>>();


	private List<Frame3> frame3s;
	private List<Theme> theme3s;

	/**
	 * static factory method for builder
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
	 * @return the questionGroup
	 */
	public Optional<QuestionGroup> getQuestionGroup() {
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
	public List<Tuple4<String, ThemeAttributeType, JSONObject, Double>> getThemeObjects() {
		return themeObjects;
	}

	/**
	 * @return the themes
	 */
	public List<Tuple2<Theme, Double>> getThemes() {
		return themes;
	}

	/**
	 * @return the frameCodes
	 */
	public List<Tuple3<String, FramePosition, Double>> getFrameCodes() {
		return frameCodes;
	}

	/**
	 * @return the frames
	 */
	public List<Tuple3<Frame3, FramePosition, Double>> getFrames() {
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
			Tuple3<Frame3, FramePosition, Double> frameTuple = Tuple.of(managedInstance, FramePosition.CENTRE, b.frameWeight);
			b.managedInstance.frames.add(frameTuple);
			b.frameWeight = b.frameWeight + 1.0;

			parentBuilder = b;
			callback = c;
		}
		
		public Builder(Frame3.Builder b, Consumer<Frame3> c, String frameCode,FramePosition position) {
			managedInstance.setCode(frameCode);
			Tuple3<Frame3, FramePosition, Double> frameTuple = Tuple.of(managedInstance, position, b.frameWeight);
			b.managedInstance.frames.add(frameTuple);
			b.frameWeight = b.frameWeight + 1.0;

			parentBuilder = b;
			callback = c;
		}

		public Builder(Frame3.Builder b, Consumer<Frame3> c, Frame3 frame,FramePosition position) {
			managedInstance = frame;
			Tuple3<Frame3, FramePosition, Double> frameTuple = Tuple.of(managedInstance, position, b.frameWeight);
			b.managedInstance.frames.add(frameTuple);
			b.frameWeight = b.frameWeight + 1.0;

			parentBuilder = b;
			callback = c;
		}
		
		

		
		/**
		 * fluent setter for frameCodes in the list
		 * 
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(String code) {
		
			return addFrame(code,FramePosition.CENTRE);
		}

		/**
		 * fluent setter for frameCodes in the list
		 * 
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(String code,FramePosition position) {
			if (managedInstance.frame3s == null) {
				managedInstance.frame3s = new ArrayList<Frame3>();
			}
			Consumer<Frame3> f = obj -> { managedInstance.frame3s.add(obj);};
			return new Frame3.Builder(this, f,code,position);
		}

		/**
		 * fluent setter for frames in the list
		 * 
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(Frame3 frame) {
			return addFrame(frame,FramePosition.CENTRE);
		}

		/**
		 * fluent setter for frames in the list
		 * 
		 * @param none
		 * @return
		 */
		public Frame3.Builder addFrame(Frame3 frame,FramePosition position) {
			if (managedInstance.frame3s == null) {
				managedInstance.frame3s = new ArrayList<Frame3>();
			}
			Consumer<Frame3> f = obj -> { managedInstance.frame3s.add(obj);};
			return new Frame3.Builder(this, f,frame,position);
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
			Consumer<Theme> f = obj -> { managedInstance.theme3s.add(obj);};
			managedInstance.themes.add(Tuple.of(theme,themeWeight));
			themeWeight = themeWeight - 1.0;

			return new Theme.Builder(this, f, theme);		
		}
		
		
		/**
		 * fluent setter for themes in the list
		 * 
		 * @param none
		 * @return
		 */
		public Theme.Builder addTheme(String themeCode) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> { managedInstance.theme3s.add(obj);};
			Theme theme = VertxUtils.getObject(managedInstance.getRealm(), "", themeCode, Theme.class);
			theme.setDirectLink(true);
			managedInstance.themes.add(Tuple.of(theme,themeWeight));
			themeWeight = themeWeight - 1.0;

			return new Theme.Builder(this, f, theme);		
		}		
		
		/**
		 * fluent setter for themes in the list
		 * 
		 * @param none
		 * @return
		 */
		public Theme.Builder addTheme(Theme theme) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> { managedInstance.theme3s.add(obj);};
			theme.setDirectLink(true);
			managedInstance.themes.add(Tuple.of(theme,themeWeight));
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
			Consumer<Theme> f = obj -> { managedInstance.theme3s.add(obj);};
			String themeCode = "THM_"+UUID.randomUUID().toString().substring(0, 25);
			Theme theme = Theme.builder(themeCode).build();
			managedInstance.themes.add(Tuple.of(theme,themeWeight));
			themeWeight = themeWeight - 1.0;

		
			return new Theme.Builder(this, f,theme);
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
			Consumer<Theme> f = obj -> { managedInstance.theme3s.add(obj);};
			ThemeAttributeType codeOnly = ThemeAttributeType.codeOnly;
			Tuple4<String, ThemeAttributeType, JSONObject, Double> theme = Tuple.of(themeCode, codeOnly, new JSONObject("{\"codeOnly\":true}"),
					themeWeight);

			managedInstance.themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;
			
			

			return new Theme.Builder(this, f,themeCode);
//			return addTheme(themeCode,ThemeAttributeType.PRI_CONTENT,ThemeAttributeType.codeOnly,new JSONObject("{\"codeOnly\":true}"));
		}
		
		public Theme.Builder addThemeParent(final String themeCode, String property, Object value) {
			return addThemeParent(themeCode,ThemeAttributeType.PRI_CONTENT,property,value);
		}
		
		public Theme.Builder addThemeParent(final String themeCode, ThemeAttributeType attributeCode, String property, Object value) {
			if (managedInstance.theme3s == null) {
				managedInstance.theme3s = new ArrayList<Theme>();
			}
			Consumer<Theme> f = obj -> { managedInstance.theme3s.add(obj);};
			
			JSONObject keyValue = new JSONObject();
			
			keyValue.put(property, value);
			
			Tuple4<String, ThemeAttributeType, JSONObject, Double> theme = Tuple.of(themeCode, attributeCode, keyValue,
					themeWeight);
			managedInstance.themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;
			return new Theme.Builder(this, f,themeCode);
		}		
		
		/**
		 * more fluent setter for QuestionGroup
		 * @return
		 */
		public QuestionGroup.Builder question(final String questionCode) {
			Consumer<QuestionGroup> f = obj -> { managedInstance.questionGroup = Optional.of(obj);};
			managedInstance.questionCode = questionCode;
			return new QuestionGroup.Builder(this, f,questionCode);
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