package org.uichuimi.vcf.variant;

import java.util.function.Function;

class VcfCharacter extends VcfType<String> {

	private static VcfCharacter instance;

	private VcfCharacter() {
		super(Function.identity());
	}

	public static VcfCharacter getInstance() {
		if (instance == null) instance = new VcfCharacter();
		return instance;
	}

}
