package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.variant.Variant;
import org.uichuimi.vcf.variant.VcfConstants;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to create the string representation of a Variant for
 * <a href=https://samtools.github.io/hts-specs/VCFv4.3.pdf>Variant Call Format</a>.
 */
class VariantFormatter {

	private static final NumberFormat DECIMAL = new DecimalFormat("#.###");

	/**
	 * Creates the VCF representation of the variant.
	 */
	static String toVcf(Variant variant) {
		final StringBuilder builder = new StringBuilder();
		builder.append(variant.getCoordinate().getChrom())
				.append(VcfConstants.DELIMITER).append(variant.getCoordinate().getPosition())
				.append(VcfConstants.DELIMITER).append(join(variant.getIdentifiers()))
				.append(VcfConstants.DELIMITER).append(join(variant.getReferences()))
				.append(VcfConstants.DELIMITER).append(join(variant.getAlternatives()))
				.append(VcfConstants.DELIMITER).append(toString(variant.getQuality()))
				.append(VcfConstants.DELIMITER).append(join(variant.getFilters()))
				.append(VcfConstants.DELIMITER).append(getInfoString(variant));
		addSampleData(variant, builder);
		return builder.toString();
	}

	private static String join(List<String> values) {
		if (values.isEmpty()) return VcfConstants.EMPTY_VALUE;
		return String.join(VcfConstants.ARRAY_DELIMITER, values);
	}

	private static String getInfoString(Variant variant) {
		final StringJoiner infoBuilder = new StringJoiner(VcfConstants.INFO_DELIMITER);
		variant.getInfo().forEach((key, value) -> {
			final InfoHeaderLine line = variant.getHeader().getInfoHeader(key);
			if (line.getNumber().equals("0")) infoBuilder.add(key);
			else if (line.getNumber().equals("1")) {
				infoBuilder.add(key + VcfConstants.KEY_VALUE_DELIMITER + toString(value));
			} else {
				final String v = ((List<?>) value).stream()
						.map(VariantFormatter::toString)
						.collect(Collectors.joining(VcfConstants.ARRAY_DELIMITER));
				infoBuilder.add(key + VcfConstants.KEY_VALUE_DELIMITER + v);
			}
		});
		final String info = infoBuilder.toString();
		return info.isEmpty() ? VcfConstants.EMPTY_VALUE : info;
	}

	private static String toString(Object value) {
		if (value == null) return VcfConstants.EMPTY_VALUE;
		if (value instanceof Double || value instanceof Float) return DECIMAL.format(value);
		return String.valueOf(value);
	}

	private static void addSampleData(Variant variant, StringBuilder builder) {
		// We are going to collect the sample data in a matrix, ignoring those FORMAT tags that do not contain
		// information for any sample. After that, we are going to generate the string by sample.
		final List<String[]> sampleData = new ArrayList<>();
		final List<String> keys = new ArrayList<>();
		// keys sampleData
		// GT   [0/1, 0/1, ./.]
		// DP   [15, 14, 24]
		// H2   [51,51, ., .]
		// Collect FORMAT by key, adding only those where at least one sample has a value
		final List<FormatHeaderLine> formatLines = new ArrayList<>(variant.getHeader().getFormatLines().values());
		final FormatHeaderLine gt = variant.getHeader().getFormatHeader("GT");
		formatLines.remove(gt);
		formatLines.add(0, gt);
//		formatLines.sort(Comparator.comparingInt(fl -> FORMAT_ORDER.indexOf(fl.getId())));
		for (final FormatHeaderLine headerLine : formatLines) {
			final String[] values = new String[variant.getHeader().getSamples().size()];
			if (headerLine.getNumber().equals("1")) {
				for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
					final String value = toString(variant.getSampleInfo().get(s).get(headerLine.getId()));
					if (value != null) values[s] = value;
				}
			} else {
				for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
					final List<?> content = variant.getSampleInfo(s).get(headerLine.getId());
					final String value = content == null ? null : content.stream()
							.map(VariantFormatter::toString)
							.collect(Collectors.joining(","));
					if (value != null) values[s] = value;
				}

			}
			if (Arrays.stream(values).allMatch(Objects::isNull)) continue;
			keys.add(headerLine.getId());
			sampleData.add(values);
		}

		builder.append(VcfConstants.DELIMITER).append(String.join(VcfConstants.FORMAT_DELIMITER, keys));

		// Now we add FORMAT by sample
		for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
			// Collect all values
			final List<String> sample = new ArrayList<>(keys.size());
			for (String[] ft : sampleData) sample.add(ft[s]);

			// Remove trailing nulls, leaving at least one value
			// Can this feature annoy some parsers?
			while (sample.size() > 1 && (sample.get(sample.size() - 1) == null || sample.get(sample.size() - 1).equals(VcfConstants.EMPTY_VALUE)))
				sample.remove(sample.size() - 1);

			// map nulls to . and join
			if (sample.stream().allMatch(Objects::isNull)) {
				builder.append(VcfConstants.DELIMITER).append(VcfConstants.EMPTY_VALUE);
			} else {
				final String sformat = sample.stream()
						.map(v -> v == null ? VcfConstants.EMPTY_VALUE : v)
						.collect(Collectors.joining(VcfConstants.FORMAT_DELIMITER));
				builder.append(VcfConstants.DELIMITER).append(sformat);
			}
		}
	}
}
