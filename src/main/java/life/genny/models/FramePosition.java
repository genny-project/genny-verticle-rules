package life.genny.models;

public enum FramePosition {
	NORTH("NORTH"), EAST("EAST"), WEST("WEST"), SOUTH("SOUTH"), CENTRE("CENTRE"), WRAPPER("WRAPPER");

	private final String name;

	private FramePosition(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		// (otherName == null) check is not needed because name.equals(null) returns
		// false
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}