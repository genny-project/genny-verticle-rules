package life.genny.models;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;



@Immutable
public final class ThemeAttribute {
	private String code;

	
	private Optional<String> flexDirection = Optional.empty();
	private Optional<String> justifyContent = Optional.empty();
	private Optional<String> backgroundColor = Optional.empty();
	private Optional<Integer> margin = Optional.empty();
	private Optional<Integer> marginBottom = Optional.empty();
	private Optional<Integer> marginRight = Optional.empty();
	private Optional<Integer> width = Optional.empty();
	private Optional<String> widthPercent = Optional.empty();
	private Optional<Integer> height = Optional.empty();
	private Optional<String> heightPercent = Optional.empty();
	private Optional<Integer> maxWidth = Optional.empty();
	private Optional<Integer> minWidth = Optional.empty();
	private Optional<Integer> padding = Optional.empty();
	private Optional<String> shadowColor = Optional.empty();
	private Optional<Double> shadowOpacity = Optional.empty();
	private Optional<Integer> shadowRadius = Optional.empty();
	private Optional<ShadowOffset> shadowOffset = Optional.empty();
	private Optional<Integer> borderBottomWidth = Optional.empty();
	private Optional<Integer> borderWidth = Optional.empty();
	private Optional<String> placeholderColor = Optional.empty();
	private Optional<String> borderStyle = Optional.empty();
	private Optional<String> borderColor = Optional.empty();
	private Optional<String> color = Optional.empty();
	private Optional<Integer> size = Optional.empty();
	private Optional<String> sizeText = Optional.empty();
	private Optional<Boolean> bold = Optional.empty();
	private Optional<String> fit = Optional.empty();
	private Optional<String> overflowX = Optional.empty();
	private Optional<String> overflowY = Optional.empty();
	private Optional<String> textAlign = Optional.empty();
	private Optional<Boolean> valueBoolean = Optional.empty();
	private Optional<Integer> valueInteger = Optional.empty();
	private Optional<String> valueString = Optional.empty();
	private Optional<Double> valueDouble = Optional.empty();
	private Optional<String> alignItems = Optional.empty();


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
		return flexDirection.orElse("");
	}

	public String getAlignItems() {
		return alignItems.orElse("");
	}

	/**
	 * @return the justifyContent
	 */
	public String getJustifyContent() {
		return justifyContent.orElse("");
	}

	/**
	 * @return the backgroundColor
	 */
	public String getBackgroundColor() {
		return backgroundColor.orElse("");
	}

	/**
	 * @return the shadowColor
	 */
	public String getShadowColor() {
		return shadowColor.orElse("");
	}

	/**
	 * @return the shadowOpacity
	 */
	public Double getShadowOpacity() {
		return shadowOpacity.orElse(0.0);
	}

	/**
	 * @return the borderBottomWidth
	 */
	public Integer getBorderBottomWidth() {
		return borderBottomWidth.orElse(0);
	}

		/**
	 * @return the borderWidth
	 */
	public Integer getBorderWidth() {
		return borderWidth.orElse(0);
	}

	/**
	 * @return the margin
	 */
	public Integer getMargin() {
		return margin.orElse(0);
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width.orElse(0);
	}

	/**
	 * @return the widthPercent
	 */
	public String getWidthPercent() {
		if (!width.isPresent()) {
			return widthPercent.orElse("100%");
		} else {
			return width.get() + "";
		}
	}

	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height.orElse(0);
	}

	/**
	 * @return the heightPercent
	 */
	public String getHeightPercent() {
		if (!height.isPresent()) {
			return heightPercent.orElse("100%");
		} else {
			return height.get() + "";
		}
	}

	/**
	 * @return the maxWidth
	 */
	public Integer getMaxWidth() {
		return maxWidth.orElse(0);
	}

	/**
	 * @return the minWidth
	 */
	public Integer getMinWidth() {
		return minWidth.orElse(0);
	}

	/**
	 * @return the padding
	 */
	public Integer getPadding() {
		return padding.orElse(0);
	}

	/**
	 * @return the shadowRadius
	 */
	public Integer getShadowRadius() {
		return shadowRadius.orElse(0);
	}

	/**
	 * @return the shadowOffset
	 */
	public Optional<ShadowOffset> getShadowOffset() {
		return shadowOffset;
	}

	/**
	 * @return the placeholderColor
	 */
	public String getPlaceholderColor() {
		return placeholderColor.orElse("#888");
	}

	/**
	 * @return the borderStyle
	 */
	public String getBorderStyle() {
		return borderStyle.orElse("solid");
	}

	/**
	 * @return the borderColor
	 */
	public String getBorderColor() {
		return borderColor.orElse("#ddd");
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color.orElse("red");
	}

	/**
	 * @return the marginBottom
	 */
	public Optional<Integer> getMarginBottom() {
		return marginBottom;
	}

	/**
	 * @return the marginRight
	 */
	public Integer getMarginRight() {
		return marginRight.orElse(0);
	}

	/**
	 * @return the size
	 */
	public Optional<Integer> getSize() {
		return size;
	}

	/**
	 * @return the textSize
	 */
	public String getTextSize() {
		if (!size.isPresent()) {
			return sizeText.orElse("md");
		} else {
			return size.get() + "";
		}
	}

	/**
	 * @return the bold
	 */
	public Optional<Boolean> getBold() {
		return bold;
	}

	/**
	 * @return the fit
	 */
	public Optional<String> getFit() {
		return fit;
	}

	/**
	 * @return the overflowX
	 */
	public Optional<String> getOverflowX() {
		return overflowX;
	}

	/**
	 * @return the overflowY
	 */
	public Optional<String> getoverflowY() {
		return overflowY;
	}

	/**
	 * @return the textAlign
	 */
	public Optional<String> getTextAlign() {
		return textAlign;
	}

	/**
	 * @return the valueBoolean
	 */
	public Optional<Boolean> getValueBoolean() {
		return valueBoolean;
	}

	/**
	 * @return the valueInteger
	 */
	public Optional<Integer> getValueInteger() {
		return valueInteger;
	}

	/**
	 * @return the valueString
	 */
	public Optional<String> getValueString() {
		return valueString;
	}

	/**
	 * @return the valueDouble
	 */
	public Optional<Double> getValueDouble() {
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
			managedInstance.valueBoolean = Optional.of(value);
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, Integer value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueInteger = Optional.of(value);
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, String value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueString = Optional.of(value);
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType, Double value) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
			managedInstance.valueDouble = Optional.of(value);
		}

		public Builder flexDirection(String value) {
			managedInstance.flexDirection = Optional.of(value);
			return this;
		}

		public Builder justifyContent(String value) {
			managedInstance.justifyContent = Optional.of(value);
			return this;
		}

		public Builder alignItems(String value) {
			managedInstance.alignItems = Optional.of(value);
			return this;
		}

		public Builder backgroundColor(String value) { // Accept Spelling errors
			managedInstance.backgroundColor = Optional.of(value);
			return this;
		}

		public Builder backgroundColour(String value) {
			managedInstance.backgroundColor = Optional.of(value);
			return this;
		}

		public Builder margin(Integer value) {
			managedInstance.margin = Optional.of(value);
			return this;
		}

		public Builder width(Integer value) {
			managedInstance.width = Optional.of(value);
			return this;
		}

		public Builder width(String value) {
			managedInstance.widthPercent = Optional.of(value); // should check format
			return this;
		}

		public Builder height(Integer value) {
			managedInstance.height = Optional.of(value);
			return this;
		}

		public Builder height(String value) {
			managedInstance.heightPercent = Optional.of(value); // should check format
			return this;
		}

		public Builder maxWidth(Integer value) {
			managedInstance.maxWidth = Optional.of(value);
			return this;
		}

		public Builder minWidth(Integer value) {
			managedInstance.minWidth = Optional.of(value);
			return this;
		}

		public Builder padding(Integer value) {
			managedInstance.padding = Optional.of(value);
			return this;
		}

		public Builder shadowRadius(Integer value) {
			managedInstance.shadowRadius = Optional.of(value);
			return this;
		}

		public Builder shadowColor(String value) { // Accept Spelling errors
			managedInstance.shadowColor = Optional.of(value);
			return this;
		}

		public Builder shadowColour(String value) {
			managedInstance.shadowColor = Optional.of(value);
			return this;
		}

		public Builder shadowOpacity(Double value) {
			managedInstance.shadowOpacity = Optional.of(value);
			return this;
		}

		public Builder borderBottomWidth(Integer value) {
			managedInstance.borderBottomWidth = Optional.of(value);
			return this;
		}

		public Builder borderWidth(Integer value) {
			managedInstance.borderWidth = Optional.of(value);
			return this;
		}

		public Builder placeholderColor(String value) {
			managedInstance.placeholderColor = Optional.of(value);
			return this;
		}

		public Builder borderStyle(String value) {
			managedInstance.borderStyle = Optional.of(value);
			return this;
		}

		public Builder borderColor(String value) {
			managedInstance.borderColor = Optional.of(value);
			return this;
		}

		public Builder borderColour(String value) {
			managedInstance.borderColor = Optional.of(value);
			return this;
		}

		public Builder color(String value) {
			managedInstance.color = Optional.of(value);
			return this;
		}

		public Builder colour(String value) {
			managedInstance.color = Optional.of(value);
			return this;
		}

		public Builder bold(Boolean value) {
			managedInstance.bold = Optional.of(value);
			return this;
		}

		public Builder fit(String value) {
			managedInstance.fit = Optional.of(value);
			return this;
		}

		public Builder overflowX(String value) {
			managedInstance.overflowX = Optional.of(value);
			return this;
		}

		public Builder overflowY(String value) {
			managedInstance.overflowY = Optional.of(value);
			return this;
		}

		public Builder textAlign(String value) {
			managedInstance.textAlign = Optional.of(value);
			return this;
		}

		public Builder size(Integer value) {
			managedInstance.size = Optional.of(value);
			return this;
		}

		public Builder size(String value) {
			managedInstance.sizeText = Optional.of(value); // should check format
			return this;
		}

		public Builder marginBottom(Integer value) {
			managedInstance.marginBottom = Optional.of(value);
			return this;
		}

		public Builder marginRight(Integer value) {
			managedInstance.marginRight = Optional.of(value);
			return this;
		}
		
		public Builder valueBoolean(Boolean value)
		{
			// TODO -> This is terrible hack by me
			managedInstance.valueBoolean = Optional.of(value);
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
				managedInstance.shadowOffset = Optional.of(obj);
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
		if (fit.isPresent())
			json.put("fit", fit.get());
		if (overflowX.isPresent())
			json.put("overflowX", overflowX.get());
		if (overflowY.isPresent())
			json.put("overflowY", overflowY.get());
		if (textAlign.isPresent())
			json.put("textAlign", textAlign.get());
		if (flexDirection.isPresent())
			json.put("flexDirection", flexDirection.get());
		if (justifyContent.isPresent())
			json.put("justifyContent", justifyContent.get());
		if (backgroundColor.isPresent())
			json.put("backgroundColor", backgroundColor.get());
		if (shadowColor.isPresent())
			json.put("shadowColor", shadowColor.get());
		if (shadowOpacity.isPresent())
			json.put("shadowOpacity", shadowOpacity.get());
		if (width.isPresent()) {
			json.put("width", width.get());
		} else {
			if (widthPercent.isPresent()) {
				json.put("width", widthPercent.get());
			}
		}

		if (height.isPresent()) {
			json.put("height", height.get());
		} else {
			if (heightPercent.isPresent()) {
				json.put("height", heightPercent.get());
			}
		}
		if (margin.isPresent())
			json.put("margin", margin.get());
		if (marginRight.isPresent())
			json.put("marginRight", marginRight.get());
		if (maxWidth.isPresent())
			json.put("maxWidth", maxWidth.get());
		if (minWidth.isPresent())
			json.put("minWidth", minWidth.get());
		if (padding.isPresent())
			json.put("padding", padding.get());
		if (shadowRadius.isPresent())
			json.put("shadowRadius", shadowRadius.get());
		if (shadowOffset.isPresent())
			json.put("shadowOffset", shadowOffset.get().getJsonObject());
		if (borderBottomWidth.isPresent())
			json.put("borderBottomWidth", borderBottomWidth.get());
		if (borderWidth.isPresent())
			json.put("borderWidth", borderWidth.get());
		if (placeholderColor.isPresent())
			json.put("placeholderColor", placeholderColor.get());
		if (borderStyle.isPresent())
			json.put("borderStyle", borderStyle.get());
		if (borderColor.isPresent())
			json.put("borderColor", borderColor.get());
		if (color.isPresent())
			json.put("color", color.get());
		if (size.isPresent()) {
			json.put("size", size.get());
		} else {
			if (sizeText.isPresent()) {
				json.put("size", sizeText.get());
			}
		}

		if (bold.isPresent())
			json.put("bold", bold.get());

		if (valueBoolean.isPresent())
			json.put("valueBoolean", valueBoolean.get());
		if (valueInteger.isPresent())
			json.put("valueInteger", valueInteger.get());
		if (valueString.isPresent())
			json.put("valueString", valueString.get());
		if (valueDouble.isPresent())
			json.put("valueDouble", valueDouble.get());

		return json;
	}

	public String getJson() {
		return getJsonObject().toString();
	}

}