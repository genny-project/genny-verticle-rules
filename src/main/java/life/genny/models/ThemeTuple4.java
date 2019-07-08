package life.genny.models;

import java.io.Serializable;

import org.json.JSONObject;

import com.google.gson.annotations.Expose;

public class ThemeTuple4 implements Serializable {
	@Expose
	String themeCode;
	@Expose
	ThemeAttributeType themeAttributeType;
	@Expose
	JSONObject jsonObject;
	@Expose
	Double weight;
	
	private ThemeTuple4() {}
	
	public ThemeTuple4(String themeCode, ThemeAttributeType themeAttributeType,JSONObject jsonObject, Double weight)
	{
		this.themeCode = themeCode;
		this.themeAttributeType = themeAttributeType;
		this.jsonObject = jsonObject;
		this.weight = weight;
	}

	/**
	 * @return the themeCode
	 */
	public String getThemeCode() {
		return themeCode;
	}

	/**
	 * @param themeCode the themeCode to set
	 */
	public void setThemeCode(String themeCode) {
		this.themeCode = themeCode;
	}

	/**
	 * @return the themeAttributeType
	 */
	public ThemeAttributeType getThemeAttributeType() {
		return themeAttributeType;
	}

	/**
	 * @param themeAttributeType the themeAttributeType to set
	 */
	public void setThemeAttributeType(ThemeAttributeType themeAttributeType) {
		this.themeAttributeType = themeAttributeType;
	}

	/**
	 * @return the jsonObject
	 */
	public JSONObject getJsonObject() {
		return jsonObject;
	}

	/**
	 * @param jsonObject the jsonObject to set
	 */
	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	/**
	 * @return the weight
	 */
	public Double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "ThemeTuple4 [themeCode=" + themeCode + ", themeAttributeType=" + themeAttributeType + ", jsonObject="
				+ jsonObject + ", weight=" + weight + "]";
	}


		
	
}
