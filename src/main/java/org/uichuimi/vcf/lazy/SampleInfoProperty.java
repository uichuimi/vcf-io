package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.VcfConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the raw representation of the FORMAT columns of a variant, and only creates the
 * VariantInfo objects under request.
 */
class SampleInfoProperty extends LazyProperty<List<Info>> {

	private String[] data;
	private VcfHeader header;

	SampleInfoProperty(VcfHeader header, String[] data) {
		super("");
		this.header = header;
		this.data = data;
	}

	@Override
	protected List<Info> parse(String raw) {
		if (data.length < 9) return new ArrayList<>();
		final String[] fields = data[8].split(VcfConstants.FORMAT_DELIMITER);
		final int samples = data.length - 9;
		final List<Info> infos = new ArrayList<>(samples);
		for (int s = 0; s < samples; s++)
			infos.add(new SampleInfo(header, fields, data[9 + s]));
		return infos;
	}
}
