package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.List;

public class SampleInfoProperty extends LazyProperty<List<VariantInfo>> {

	private static final String FORMAT_SEPARATOR = ":";
	private String[] data;
	private VcfHeader header;

	SampleInfoProperty(VcfHeader header, String[] data) {
		super("");
		this.header = header;
		this.data = data;
	}

	@Override
	protected List<VariantInfo> parse(String raw) {
		if (data.length < 9) return new ArrayList<>();
		final String [] fields = data[8].split(FORMAT_SEPARATOR);
		final int samples = data.length - 9;
		final List<VariantInfo> infos = new ArrayList<>(samples);
		for (int s = 0; s < samples; s++) infos.add(new SampleVariantInfo(header, fields, data[9 + s]));
		return infos;
	}
}
