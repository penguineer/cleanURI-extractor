package com.penguineering.cleanuri.extractor.processors;

public enum Metakey {
	ID("id"), NAME("name");

	private final String label;

	private Metakey(String label) {
		if (label == null)
			throw new NullPointerException("Label must not be null!");

		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return this.getLabel();
	}
}
