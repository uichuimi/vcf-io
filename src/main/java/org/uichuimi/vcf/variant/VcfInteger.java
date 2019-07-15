package org.uichuimi.vcf.variant;

class VcfInteger extends VcfType<Integer> {

	private static VcfInteger instance;

	private VcfInteger() {
		super(Integer::valueOf);
	}

	public static VcfInteger getInstance() {
		if (instance == null) instance = new VcfInteger();
		return instance;
	}

}
