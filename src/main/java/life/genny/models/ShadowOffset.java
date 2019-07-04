package life.genny.models;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;

@Immutable
public final class ShadowOffset {

	private Optional<Integer> width = Optional.empty();
	private Optional<Integer> height = Optional.empty();


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
			managedInstance.width = Optional.of(value);
			return this;
		}

		public Builder height(Integer value) {
			managedInstance.height = Optional.of(value);
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

		if (width.isPresent()) json.put("width", width.get());
		if (height.isPresent()) json.put("height", height.get());
		

		return json;
	}

	public String getJson() {
		return getJsonObject().toString();
	}

}