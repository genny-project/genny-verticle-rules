package life.genny.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ThemeDouble implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	Theme theme;
	@Expose
	Double weight;
	
	private ThemeDouble() {}
	
	public ThemeDouble(Theme theme, Double weight)	
	{
		this.theme = theme;
		this.weight = weight;
	}

	/**
	 * @return the theme
	 */
	public Theme getTheme() {
		return theme;
	}

	/**
	 * @param theme the theme to set
	 */
	public void setTheme(Theme theme) {
		this.theme = theme;
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
		return theme.getCode() + ":" + weight;
	}
	
	
}
