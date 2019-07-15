package life.genny.models;

import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;

import com.google.gson.annotations.Expose;

@Immutable
public final class ThemeAttribute {
	@Expose
	private String code;

	@Expose
	private String flexDirection = null;
	@Expose
	private Integer flexGrow = null;
	@Expose
	private Integer flexShrink = null;
	@Expose
	private String flexBasis = null;
	@Expose
	private String justifyContent = null;
	@Expose
	private String backgroundColor = null;
	@Expose
	private Integer margin = null;
	@Expose
	private Integer marginBottom = null;
	@Expose
	private Integer marginRight = null;
	@Expose
	private Integer width = null;
	@Expose
	private String widthPercent = null;
	@Expose
	private Integer height = null;
	@Expose
	private String heightPercent = null;
	@Expose
	private Integer maxWidth = null;
	@Expose
	private Integer minWidth = null;
	@Expose
	private Integer padding = null;
	@Expose
	private Integer paddingX = null;
	@Expose
	private Integer paddingY = null;
	@Expose
	private String shadowColor = null;
	@Expose
	private Double shadowOpacity = null;
	@Expose
	private Integer shadowRadius = null;
	@Expose
	private ShadowOffset shadowOffset = null;
	@Expose
	private Integer borderBottomWidth = null;
	@Expose
	private Integer borderWidth = null;
	@Expose
	private String placeholderColor = null;
	@Expose
	private String borderStyle = null;
	@Expose
	private String borderColor = null;
	@Expose
	private String color = null;
	@Expose
	private Integer size = null;
	@Expose
	private String sizeText = null;
	@Expose
	private Boolean bold = null;
	@Expose
	private String fit = null;
	@Expose
	private String overflowX = null;
	@Expose
	private String overflowY = null;
	@Expose
	private String textAlign = null;
	@Expose
	private Boolean valueBoolean = null;
	@Expose
	private Integer valueInteger = null;
	@Expose
	private String valueString = null;
	@Expose
	private Double valueDouble = null;
	@Expose
	private String alignItems = null;

	/**
	 * static factory method for builder
	 */
	public static Builder builder() {
		return new ThemeAttribute.Builder();
	}

	/**
	 * forces use of the Builder
	 */
	private ThemeAttribute() {
	}

	public String getCode() {
		return code;
	}

	/**
	 * @return the flexDirection
	 */
	public String getFlexDirection() {
		return flexDirection;
	}

	/**
	 * @return the flexGrow
	 */
	public Integer getFlexGrow() {
		return flexGrow;
	}

	/**
	 * @return the flexShrink
	 */
	public Integer getFlexShrink() {
		return flexShrink;
	}

	/**
	 * @return the flexBasis
	 */
	public String getFlexBasis() {
		return flexBasis;
	}

	public String getAlignItems() {
		return alignItems;
	}

	/**
	 * @return the justifyContent
	 */
	public String getJustifyContent() {
		return justifyContent;
	}

	/**
	 * @return the backgroundColor
	 */
	public String getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @return the shadowColor
	 */
	public String getShadowColor() {
		return shadowColor;
	}

	/**
	 * @return the shadowOpacity
	 */
	public Double getShadowOpacity() {
		return shadowOpacity;
	}

	/**
	 * @return the borderBottomWidth
	 */
	public Integer getBorderBottomWidth() {
		return borderBottomWidth;
	}

	/**
	 * @return the borderWidth
	 */
	public Integer getBorderWidth() {
		return borderWidth;
	}

	/**
	 * @return the margin
	 */
	public Integer getMargin() {
		return margin;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @return the widthPercent
	 */
	public String getWidthPercent() {
		if (width == null) {
			return widthPercent != null ? widthPercent : "100%";
		} else {
			return width + "";
		}
	}

	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @return the heightPercent
	 */
	public String getHeightPercent() {
		if (height == null) {
			return heightPercent != null ? heightPercent : "100%";
		} else {
			return height + "";
		}
	}

	/**
	 * @return the maxWidth
	 */
	public Integer getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @return the minWidth
	 */
	public Integer getMinWidth() {
		return minWidth;
	}

	/**
	 * @return the padding
	 */
	public Integer getPadding() {
		return padding;
	}

	/**
	 * @return the paddingX
	 */
	public Integer getPaddingX() {
		return paddingX;
	}

	/**
	 * @return the paddingY
	 */
	public Integer getPaddingY() {
		return paddingY;
	}

	/**
	 * @return the shadowRadius
	 */
	public Integer getShadowRadius() {
		return shadowRadius;
	}

	/**
	 * @return the shadowOffset
	 */
	public ShadowOffset getShadowOffset() {
		return shadowOffset;
	}

	/**
	 * @return the placeholderColor
	 */
	public String getPlaceholderColor() {
		return placeholderColor != null ? placeholderColor : "#888";
	}

	/**
	 * @return the borderStyle
	 */
	public String getBorderStyle() {
		return borderStyle != null ? borderStyle : "solid";
	}

	/**
	 * @return the borderColor
	 */
	public String getBorderColor() {
		return borderColor != null ? borderColor : "#ddd";
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color != null ? color : "red";
	}

	/**
	 * @return the marginBottom
	 */
	public Integer getMarginBottom() {
		return marginBottom;
	}

	/**
	 * @return the marginRight
	 */
	public Integer getMarginRight() {
		return marginRight;
	}

	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * @return the textSize
	 */
	public String getTextSize() {
		if (size == null) {

			return sizeText != null ? sizeText : "md";
		} else {
			return size + "";
		}
	}

	/**
	 * @return the bold
	 */
	public Boolean getBold() {
		return bold;
	}

	/**
	 * @return the fit
	 */
	public String getFit() {
		return fit;
	}

	/**
	 * @return the overflowX
	 */
	public String getOverflowX() {
		return overflowX;
	}

	/**
	 * @return the overflowY
	 */
	public String getoverflowY() {
		return overflowY;
	}

	/**
	 * @return the textAlign
	 */
	public String getTextAlign() {
		return textAlign;
	}

	/**
	 * @return the valueBoolean
	 */
	public Boolean getValueBoolean() {
		return valueBoolean;
	}

	/**
	 * @return the valueInteger
	 */
	public Integer getValueInteger() {
		return valueInteger;
	}

	/**
	 * @return the valueString
	 */
	public String getValueString() {
		return valueString;
	}

	/**
	 * @return the valueDouble
	 */
	public Double getValueDouble() {
		return valueDouble;
	}

	public static class Builder {
		private ThemeAttribute managedInstance = new ThemeAttribute();
		private Theme.Builder parentBuilder;
		private Consumer<ThemeAttribute> callback;

		public Builder() {
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, Boolean value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueBoolean = value;
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, Integer value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueInteger = value;
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, String value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueString = value;
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, Double value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueDouble = value;
		}

		public Builder flexDirection(String value) {
			managedInstance.flexDirection = value;
			return this;
		}

		public Builder flexGrow(Integer value) {
			managedInstance.flexGrow = value;
			return this;
		}

		public Builder flexShrink(Integer value) {
			managedInstance.flexShrink = value;
			return this;
		}

		public Builder flexBasis(String value) {
			managedInstance.flexBasis = value;
			return this;
		}

		public Builder justifyContent(String value) {
			managedInstance.justifyContent = value;
			return this;
		}

		public Builder alignItems(String value) {
			managedInstance.alignItems = value;
			return this;
		}

		public Builder backgroundColor(String value) { // Accept Spelling errors
			managedInstance.backgroundColor = value;
			return this;
		}

		public Builder backgroundColour(String value) {
			managedInstance.backgroundColor = value;
			return this;
		}

		public Builder margin(Integer value) {
			managedInstance.margin = value;
			return this;
		}

		public Builder width(Integer value) {
			managedInstance.width = value;
			return this;
		}

		public Builder width(String value) {
			managedInstance.widthPercent = value; // should check format
			return this;
		}

		public Builder height(Integer value) {
			managedInstance.height = value;
			return this;
		}

		public Builder height(String value) {
			managedInstance.heightPercent = value; // should check format
			return this;
		}

		public Builder maxWidth(Integer value) {
			managedInstance.maxWidth = value;
			return this;
		}

		public Builder minWidth(Integer value) {
			managedInstance.minWidth = value;
			return this;
		}

		public Builder padding(Integer value) {
			managedInstance.padding = value;
			return this;
		}

		public Builder paddingX(Integer value) {
			managedInstance.paddingX = value;
			return this;
		}

		public Builder paddingY(Integer value) {
			managedInstance.paddingY = value;
			return this;
		}

		public Builder shadowRadius(Integer value) {
			managedInstance.shadowRadius = value;
			return this;
		}

		public Builder shadowColor(String value) { // Accept Spelling errors
			managedInstance.shadowColor = value;
			return this;
		}

		public Builder shadowColour(String value) {
			managedInstance.shadowColor = value;
			return this;
		}

		public Builder shadowOpacity(Double value) {
			managedInstance.shadowOpacity = value;
			return this;
		}

		public Builder borderBottomWidth(Integer value) {
			managedInstance.borderBottomWidth = value;
			return this;
		}

		public Builder borderWidth(Integer value) {
			managedInstance.borderWidth = value;
			return this;
		}

		public Builder placeholderColor(String value) {
			managedInstance.placeholderColor = value;
			return this;
		}

		public Builder borderStyle(String value) {
			managedInstance.borderStyle = value;
			return this;
		}

		public Builder borderColor(String value) {
			managedInstance.borderColor = value;
			return this;
		}

		public Builder borderColour(String value) {
			managedInstance.borderColor = value;
			return this;
		}

		public Builder color(String value) {
			managedInstance.color = value;
			return this;
		}

		public Builder colour(String value) {
			managedInstance.color = value;
			return this;
		}

		public Builder bold(Boolean value) {
			managedInstance.bold = value;
			return this;
		}

		public Builder fit(String value) {
			managedInstance.fit = value;
			return this;
		}

		public Builder overflowX(String value) {
			managedInstance.overflowX = value;
			return this;
		}

		public Builder overflowY(String value) {
			managedInstance.overflowY = value;
			return this;
		}

		public Builder textAlign(String value) {
			managedInstance.textAlign = value;
			return this;
		}

		public Builder size(Integer value) {
			managedInstance.size = value;
			return this;
		}

		public Builder size(String value) {
			managedInstance.sizeText = value; // should check format
			return this;
		}

		public Builder marginBottom(Integer value) {
			managedInstance.marginBottom = value;
			return this;
		}

		public Builder marginRight(Integer value) {
			managedInstance.marginRight = value;
			return this;
		}

		public Builder valueBoolean(Boolean value) {
			// TODO -> This is terrible hack by me
			managedInstance.valueBoolean = value;
			return this;
		}

		public ThemeAttribute build() {
			return managedInstance;
		}

		/**
		 * more fluent setter for Supplier
		 *
		 * @return
		 */
		public ShadowOffset.Builder shadowOffset() {
			Consumer<ShadowOffset> f = obj -> {
				managedInstance.shadowOffset = obj;
			};
			return new ShadowOffset.Builder(this, f);
		}

		public Theme.Builder end() {
			callback.accept(managedInstance);
			return parentBuilder;
		}

	}

	@Override
	public String toString() {
		return this.getCode();
	}

	public JSONObject getJsonObject() {
		JSONObject json = new JSONObject();
		if (fit != null)
			json.put("fit", fit);
		if (overflowX != null)
			json.put("overflowX", overflowX);
		if (overflowY != null)
			json.put("overflowY", overflowY);
		if (textAlign != null)
			json.put("textAlign", textAlign);
		if (flexDirection != null)
			json.put("flexDirection", flexDirection);
		if (flexGrow != null)
			json.put("flexGrow", flexGrow);
		if (flexShrink != null)
			json.put("flexShrink", flexShrink);
		if (flexBasis != null)
			json.put("flexBasis", flexBasis);
		if (justifyContent != null)
			json.put("justifyContent", justifyContent);
		if (backgroundColor != null)
			json.put("backgroundColor", backgroundColor);
		if (shadowColor != null)
			json.put("shadowColor", shadowColor);
		if (shadowOpacity != null)
			json.put("shadowOpacity", shadowOpacity);
		if (width != null) {
			json.put("width", width);
		} else {
			if (widthPercent != null) {
				json.put("width", widthPercent);
			}
		}

		if (height != null) {
			json.put("height", height);
		} else {
			if (heightPercent != null) {
				json.put("height", heightPercent);
			}
		}
		if (margin != null)
			json.put("margin", margin);
		if (marginRight != null)
			json.put("marginRight", marginRight);
		if (maxWidth != null)
			json.put("maxWidth", maxWidth);
		if (minWidth != null)
			json.put("minWidth", minWidth);
		if (padding != null)
			json.put("padding", padding);
		if (paddingX != null)
			json.put("paddingX", paddingX);
		if (paddingY != null)
			json.put("paddingY", paddingY);
		if (shadowRadius != null)
			json.put("shadowRadius", shadowRadius);
		if (shadowOffset != null)
			json.put("shadowOffset", shadowOffset.getJsonObject());
		if (borderBottomWidth != null)
			json.put("borderBottomWidth", borderBottomWidth);
		if (borderWidth != null)
			json.put("borderWidth", borderWidth);
		if (placeholderColor != null)
			json.put("placeholderColor", placeholderColor);
		if (borderStyle != null)
			json.put("borderStyle", borderStyle);
		if (borderColor != null)
			json.put("borderColor", borderColor);
		if (color != null)
			json.put("color", color);
		if (size != null) {
			json.put("size", size);
		} else {
			if (sizeText != null) {
				json.put("size", sizeText);
			}
		}

		if (bold != null)
			json.put("bold", bold);

		if (valueBoolean != null)
			json.put("valueBoolean", valueBoolean);
		if (valueInteger != null)
			json.put("valueInteger", valueInteger);
		if (valueString != null)
			json.put("valueString", valueString);
		if (valueDouble != null)
			json.put("valueDouble", valueDouble);

		return json;
	}

	public String getJson() {
		return getJsonObject().toString();
	}

}