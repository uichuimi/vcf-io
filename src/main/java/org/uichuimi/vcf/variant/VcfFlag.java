package org.uichuimi.vcf.variant;

class VcfFlag extends VcfType<Boolean> {

	private static VcfFlag instance;

	private VcfFlag() {
		super(s -> true);
	}

	public static VcfFlag getInstance() {
		if (instance == null) instance = new VcfFlag();
		return instance;
	}

}
