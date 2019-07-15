package org.uichuimi.vcf.variant;

import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;

/**
 * Although the info of a sample has the same structure as the variant info, its creation is
 * different, since the keys and the values are provided in different strings.
 */
public class SampleInfo extends Info {

	private VcfHeader header;
	private String[] fields;
	private String raw;

	public SampleInfo(VcfHeader header, String[] fields, String raw) {
		super(header, null);
		this.header = header;
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

	@Override
	public synchronized <T> void set(String key, T value) {
		super.set(key, value);
	}

	private void parseRaw() {
		final String[] values = raw.split(VcfConstants.FORMAT_DELIMITER);
		for (int i = 0; i < fields.length; i++) {
			final FormatHeaderLine header = this.header.getFormatHeader(fields[i]);
			final String raw = values[i];
			set(fields[i], header.getProperty(raw));
		}
	}

}
