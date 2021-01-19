package org.uichuimi.vcf.variant;

class VcfFloat extends VcfType<Float> {

	private static VcfFloat instance;

	private VcfFloat() {
		super(raw -> {
			final float f = java.lang.Float.parseFloat(raw);
			return java.lang.Float.isFinite(f) ? f : null;
		});
	}

	public static VcfFloat getInstance() {
		if (instance == null) instance = new VcfFloat();
		return instance;
	}

}
