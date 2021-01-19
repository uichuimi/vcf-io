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

	SampleInfo(VcfHeader header, String[] fields, String raw) {
		super(header, null);
		this.header = header;
		this.fields = fields;
		this.raw = raw;
	}

	@Override
	public <T> T get(String key) {
		parseValues();
		return super.get(key);
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		parseValues();
		return super.get(key, type);
	}

	@Override
	public synchronized <T> void set(String key, T value) {
		parseValues();
		super.set(key, value);
	}

	private void parseValues() {
		if (fields != null) {
			parseRaw();
			fields = null;
			raw = null;
		}
	}

	private void parseRaw() {
		if (raw.equals(VcfConstants.EMPTY_VALUE)) return;
		final String[] values = raw.split(VcfConstants.FORMAT_DELIMITER);
		for (int i = 0; i < fields.length; i++) {
			//  Trailing fields can be dropped
			if (values.length <= i) return;
			final FormatHeaderLine header = this.header.getFormatHeader(fields[i]);
			final String raw = values[i];
			if (!raw.equals(VcfConstants.EMPTY_VALUE))
				set(fields[i], header.getProperty(raw));
		}
	}

}
