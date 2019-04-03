package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.variant.VariantContext;
import org.uichuimi.vcf.variant.VariantSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to create the string representation of a VariantContext for
 * <a href=https://samtools.github.io/hts-specs/VCFv4.3.pdf>Variant Call Format</a>.
 */
public class VcfFormatter {

	private static final String SEPARATOR = "\t";
	private static final String SECONDARY_SEPARATOR = ",";
	private static final String INFO_SEPARATOR = ";";
	private static final String FORMAT_SEPARATOR = ":";

	private static final NumberFormat DECIMAL = new DecimalFormat("###,###.###");

	/**
	 * Creates the VCF representation of the variant.
	 */
	public static String toVcf(VariantContext variant) {
		final StringBuilder builder = new StringBuilder();
		builder.append(variant.getCoordinate().getChrom())
				.append(SEPARATOR).append(variant.getCoordinate().getPosition())
				.append(SEPARATOR).append(join(variant.getIds()))
				.append(SEPARATOR).append(join(variant.getReferences()))
				.append(SEPARATOR).append(join(variant.getAlternatives()))
				.append(SEPARATOR).append(DECIMAL.format(variant.getQuality()))
				.append(SEPARATOR).append(join(variant.getFilters()))
				.append(SEPARATOR).append(getInfoString(variant));
		addSampleData(variant, builder);
		return builder.toString();
	}

	private static String join(List<String> values) {
		if (values.isEmpty()) return VariantSet.EMPTY_VALUE;
		return String.join(SECONDARY_SEPARATOR, values);
	}

	private static String getInfoString(VariantContext variant) {
		final StringJoiner infoBuilder = new StringJoiner(INFO_SEPARATOR);
		for (InfoHeaderLine headerLine : variant.getHeader().getInfoLines()) {
			if (headerLine.getNumber().equals("0")) {
				// Flags
				if (variant.getInfo().getGlobal().hasInfo(headerLine.getId()))
					infoBuilder.add(headerLine.getId());
			} else {
				// Others
				final String value = headerLine.extract(variant, variant.getInfo());
				if (value != null) infoBuilder.add(headerLine.getId() + "=" + value);
			}
		}
		return infoBuilder.toString();
	}

	private static void addSampleData(VariantContext variant, StringBuilder builder) {
		// We are going to collect the sample data in a matrix, ignoring those FORMAT tags that do not contain
		// information for any sample. After that, we are going to generate the string by sample.
		final List<String[]> sampleData = new ArrayList<>();
		final List<String> keys = new ArrayList<>();
		// keys sampleData
		// GT   [0/1, 0/1, ./.]
		// DP   [15, 14, 24]
		// H2   [51,51, ., .]
		// Collect FORMAT by key, adding only those where at least one sample has a value
		for (final FormatHeaderLine headerLine : variant.getHeader().getFormatLines()) {
			final String[] values = new String[variant.getHeader().getSamples().size()];
			for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
				final String value = headerLine.extract(variant, variant.getSampleInfo(s));
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
			final String sformat = sample.stream()
					.map(v -> v == null ? VariantSet.EMPTY_VALUE : v)
					.collect(Collectors.joining(FORMAT_SEPARATOR));
			builder.append(SEPARATOR).append(sformat);
		}
	}
}
