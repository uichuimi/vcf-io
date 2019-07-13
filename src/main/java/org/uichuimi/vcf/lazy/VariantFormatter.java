package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.variant.VariantSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to create the string representation of a Variant for
 * <a href=https://samtools.github.io/hts-specs/VCFv4.3.pdf>Variant Call Format</a>.
 */
public class VariantFormatter {

	private static final String SEPARATOR = "\t";
	private static final String SECONDARY_SEPARATOR = ",";
	private static final String INFO_SEPARATOR = ";";
	private static final String FORMAT_SEPARATOR = ":";

	private static final NumberFormat DECIMAL = new DecimalFormat("#.###");
	public static final List<String> FORMAT_ORDER = Arrays.asList("GT", "AD", "DP", "GQ", "PL");

	/**
	 * Creates the VCF representation of the variant.
	 */
	public static String toVcf(Variant variant) {
		final StringBuilder builder = new StringBuilder();
		builder.append(variant.getCoordinate().getChrom())
				.append(SEPARATOR).append(variant.getCoordinate().getPosition())
				.append(SEPARATOR).append(join(variant.getIdentifiers()))
				.append(SEPARATOR).append(join(variant.getReferences()))
				.append(SEPARATOR).append(join(variant.getAlternatives()))
				.append(SEPARATOR).append(toString(variant.getQuality()))
				.append(SEPARATOR).append(join(variant.getFilters()))
				.append(SEPARATOR).append(getInfoString(variant));
		addSampleData(variant, builder);
		return builder.toString();
	}

	private static String join(List<String> values) {
		if (values.isEmpty()) return VariantSet.EMPTY_VALUE;
		return String.join(SECONDARY_SEPARATOR, values);
	}

	private static String getInfoString(Variant variant) {
		final StringJoiner infoBuilder = new StringJoiner(INFO_SEPARATOR);
		variant.getInfo().forEach((key, value) -> {
			final InfoHeaderLine line = variant.getHeader().getInfoHeader(key);
			if (line.getNumber().equals("0")) infoBuilder.add(key);
			else if (line.getNumber().equals("1")) {
				infoBuilder.add(String.format("%s=%s", key, toString(value)));
			} else {
				final String v = ((List<?>) value).stream()
						.map(VariantFormatter::toString)
						.collect(Collectors.joining(","));
				infoBuilder.add(String.format("%s=%s", key, v));
			}
		});
		return infoBuilder.toString();
	}

	private static String toString(Object value) {
		if (value == null) return VariantSet.EMPTY_VALUE;
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
		final List<FormatHeaderLine> formatLines = new ArrayList<>(variant.getHeader().getFormatLines());
		formatLines.sort(Comparator.comparingInt(fl -> FORMAT_ORDER.indexOf(fl.getId())));
		for (final FormatHeaderLine headerLine : formatLines) {
			final String[] values = new String[variant.getHeader().getSamples().size()];
			for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
				final String value = toString(variant.getSampleInfo().get(s).get(headerLine.getId()));
				if (value != null) values[s] = value;
			}
			if (Arrays.stream(values).allMatch(Objects::isNull)) continue;
			keys.add(headerLine.getId());
			sampleData.add(values);
		}

		builder.append("\t").append(String.join(FORMAT_SEPARATOR, keys));

		// Now we add FORMAT by sample
		for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
			// Collect all values
			final List<String> sample = new ArrayList<>(keys.size());
			for (String[] ft : sampleData) sample.add(ft[s]);

			// Remove trailing nulls, leaving at least one value
			// Can this feature annoy some parsers?
			while (sample.size() > 1 && sample.get(sample.size() - 1) == null)
				sample.remove(sample.size() - 1);

			// map nulls to . and join
			if (sample.stream().allMatch(Objects::isNull)) {
				builder.append(SEPARATOR).append(VariantSet.EMPTY_VALUE);
			} else {
				final String sformat = sample.stream()
						.map(v -> v == null ? VariantSet.EMPTY_VALUE : v)
						.collect(Collectors.joining(FORMAT_SEPARATOR));
				builder.append(SEPARATOR).append(sformat);
			}
		}
	}
}
