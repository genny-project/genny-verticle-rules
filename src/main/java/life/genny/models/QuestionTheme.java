package life.genny.models;


import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;

import com.google.gson.annotations.Expose;

import life.genny.qwanda.ContextType;
import life.genny.qwanda.VisualControlType;


@Immutable
public final class QuestionTheme implements Serializable {
	@Expose
	private String code;
	@Expose
	private Theme theme=null;
	@Expose
	private VisualControlType vcl=null;
	@Expose
	private ContextType contextType=null;
	@Expose
	private Double weight=null;
	

	
	/**
	 * static factory method for builder
	 */
	public static Builder builder() {
		return new QuestionTheme.Builder();
	}
	
	/**
	 * forces use of the Builder
	 */
	private QuestionTheme() {
	}
	
	public String getCode() {
		return code;
	}






	/**
	 * @return the theme
	 */
	public Theme getTheme() {
		return theme;
	}


	/**
	 * @return the vcl
	 */
	public VisualControlType getVcl() {
		if (vcl == null) {
			return VisualControlType.VCL_DEFAULT;
		} else 
		return vcl;
	}


	/**
	 * @return the contextType
	 */
	public ContextType getContextType() {
		if (contextType == null) {
			return ContextType.THEME;
		} else 
		return contextType;
	}


	/**
	 * @return the weight
	 */
	public Double getWeight() {
		if (weight == null) {
			return 1.0;
		} else
		return weight;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}






	public static class Builder {
		private QuestionTheme managedInstance = new QuestionTheme();
		private QuestionGroup.Builder parentBuilder;
		private Consumer<QuestionTheme> callback;


		public Builder() {
		}

		public Builder(QuestionGroup.Builder b, Consumer<QuestionTheme> c, String code) {
			managedInstance.code = code;
			parentBuilder = b;
			callback = c;
		}
		
		public Builder(QuestionGroup.Builder b, Consumer<QuestionTheme> c, Theme theme) {
			managedInstance.theme = theme;
			managedInstance.code = theme.getCode();
			parentBuilder = b;
			callback = c;
		}


		public Builder contextType(ContextType value) {
			managedInstance.contextType = value;
			return this;
		}

		public Builder vcl(VisualControlType value) {
			managedInstance.vcl = value;
			return this;
		}

		public Builder weight(Double value) { 
			managedInstance.weight = value;
			return this;
		}


		
		public QuestionTheme build() {
			return managedInstance;
		}
		


		public QuestionGroup.Builder end() {
			callback.accept(managedInstance);
			return parentBuilder;
		}

	}
	
	
	
	
	@Override
	public String toString() {
		return getJson();
	}

	public JSONObject getJsonObject()
	{
		JSONObject json = new JSONObject();
		json.put("contextCode", code);
		if (contextType != null) json.put("name", contextType);
		if (vcl != null) json.put("visualControlType", vcl);
		if (weight != null) json.put("weight", weight);

		return json;
	}
	
	public String getJson()
	{
		return getJsonObject().toString();
	}
	
}