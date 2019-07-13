package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.header.VcfHeader;

public class SampleVariantInfo extends VariantInfo {

	public static final String FORMAT_SEPARATOR = ":";
	private String[] fields;
	private String raw;

	public SampleVariantInfo(VcfHeader header, String[] fields, String raw) {
		super(header, null);
		this.fields = fields;
		this.raw = raw;
	}

	@Override
	public <T> T get(String key) {
		if (fields != null) {
			parseRaw();
			fields = null;
			raw = null;
		}
		return super.get(key);
	}

	private void parseRaw() {
		final String[] values = raw.split(FORMAT_SEPARATOR);
		for (int i = 0; i < fields.length; i++) set(fields[i], values[i]);
	}

	@Override
	public synchronized <T> void set(String key, T value) {
		super.set(key, value);
	}

}
