package org.uichuimi.vcf.variant;

import java.util.function.Function;

class VcfString extends VcfType<String> {

	private static VcfString instance;

	private VcfString() {
		super(Function.identity());
	}

	public static VcfString getInstance() {
		if (instance == null) instance = new VcfString();
		return instance;
	}

}
