package life.genny.models;


import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;

import com.google.gson.annotations.Expose;

import life.genny.qwanda.ContextType;
import life.genny.qwanda.VisualControlType;


@Immutable
public final class QuestionTheme {
	@Expose
	private String code;
	@Expose
	private Optional<Theme> theme=Optional.empty();
	@Expose
	private Optional<VisualControlType> vcl=Optional.empty();
	@Expose
	private Optional<ContextType> contextType=Optional.empty();
	@Expose
	private Optional<Double> weight=Optional.empty();
	

	
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
	public Optional<Theme> getTheme() {
		return theme;
	}


	/**
	 * @return the vcl
	 */
	public VisualControlType getVcl() {
		return vcl.orElse(VisualControlType.VCL_INPUT);
	}


	/**
	 * @return the contextType
	 */
	public ContextType getContextType() {
		return contextType.orElse(ContextType.THEME);
	}


	/**
	 * @return the weight
	 */
	public Double getWeight() {
		return weight.orElse(1.0);
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
			managedInstance.theme = Optional.of(theme);
			managedInstance.code = theme.getCode();
			parentBuilder = b;
			callback = c;
		}


		public Builder contextType(ContextType value) {
			managedInstance.contextType = Optional.of(value);
			return this;
		}

		public Builder vcl(VisualControlType value) {
			managedInstance.vcl = Optional.of(value);
			return this;
		}

		public Builder weight(Double value) { 
			managedInstance.weight = Optional.of(value);
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
		if (contextType.isPresent()) json.put("name", contextType.get());
		if (vcl.isPresent()) json.put("visualControlType", vcl.get());
		if (weight.isPresent()) json.put("weight", weight.get());

		return json;
	}
	
	public String getJson()
	{
		return getJsonObject().toString();
	}
	
}