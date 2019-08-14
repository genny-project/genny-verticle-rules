package life.genny.models;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;

import com.google.gson.annotations.Expose;

@Immutable
public final class ShadowOffset implements Serializable {

	@Expose
	private Integer width = null;
	@Expose
	private Integer height = null;


	/**
	 * static factory method for builder
	 */
	public static Builder builder() {
		return new ShadowOffset.Builder();
	}

	/**
	 * forces use of the Builder
	 */
	private ShadowOffset() {
	}

	public static class Builder {
		private ShadowOffset managedInstance = new ShadowOffset();
		private ThemeAttribute.Builder parentBuilder;
		private Consumer<ShadowOffset> callback;

		public Builder() {
		}

		public Builder(ThemeAttribute.Builder b, Consumer<ShadowOffset> c) {
			parentBuilder = b;
			callback = c;
		}

		public Builder width(Integer value) {
			managedInstance.width = value;
			return this;
		}

		public Builder height(Integer value) {
			managedInstance.height = value;
			return this;
		}

		public ShadowOffset build() {
			return managedInstance;
		}

		public ThemeAttribute.Builder end() {
			callback.accept(managedInstance);
			return parentBuilder;
		}

	}

	@Override
	public String toString() {
		return getJson();
	}

	public JSONObject getJsonObject() {
		JSONObject json = new JSONObject();

		if (width!=null) json.put("width", width);
		if (height!=null) json.put("height", height);
		

		return json;
	}

	public String getJson() {
		return getJsonObject().toString();
	}

}