package org.uichuimi.variant.io.vcf.io;

import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.VariantSet;
import org.uichuimi.variant.io.vcf.header.FormatHeaderLine;
import org.uichuimi.variant.io.vcf.header.HeaderLine;
import org.uichuimi.variant.io.vcf.header.InfoHeaderLine;

import java.util.*;
import java.util.stream.Collectors;

public class SuperVariantWriter {

	private static final String SEPARATOR = "\t";
	private static final String SECONDARY_SEPARATOR = ",";
	private static final String INFO_SEPARATOR = ";";
	private static final String FORMAT_SEPARATOR = ":";

	public static String toString(SuperVariant variant) {
		final StringBuilder builder = new StringBuilder();
		builder.append(variant.getCoordinate().getChrom())
				.append(SEPARATOR).append(variant.getCoordinate().getPosition())
				.append(SEPARATOR).append(join(variant.getIds()))
				.append(SEPARATOR).append(join(variant.getReferences()))
				.append(SEPARATOR).append(join(variant.getAlternatives()))
				.append(SEPARATOR).append(variant.getQuality())
				.append(SEPARATOR).append(join(variant.getFilters()))
				.append(SEPARATOR).append(getInfoString(variant));
		addSampleData(variant, builder);
		return builder.toString();
	}

	private static String join(List<String> values) {
		if (values.isEmpty()) return VariantSet.EMPTY_VALUE;
		return String.join(SECONDARY_SEPARATOR, values);
	}

	private static String getInfoString(SuperVariant variant) {
		final StringJoiner infoBuilder = new StringJoiner(INFO_SEPARATOR);
		for (HeaderLine headerLine : variant.getHeader().getHeaderLines()) {
			if (headerLine instanceof InfoHeaderLine) {
				final InfoHeaderLine infoHeaderLine = (InfoHeaderLine) headerLine;
				// Flags
				if (infoHeaderLine.getNumber().equals("0")) {
					if (variant.getInfo().hasInfo(infoHeaderLine.getId()))
						infoBuilder.add(infoHeaderLine.getId());
				} else {
					final String value = infoHeaderLine.extract(variant, variant);
					if (value != null) infoBuilder.add(infoHeaderLine.getId() + "=" + value);
				}
			}
		}
		return infoBuilder.toString();
	}

	private static void addSampleData(SuperVariant variant, StringBuilder builder) {
		final List<FormatHeaderLine> formatLines = variant.getHeader().getFormatLines();
		final List<String[]> sampleData = new ArrayList<>();
		final List<String> keys = new ArrayList<>();
		// keys sampleData
		// GT   [0/1, 0/1, ./.]
		// DP   [15, 14, 24]
		// H2   [51,51, ., .]
		// Collect FORMAT by format key, adding only those where at least one sample has a value
		for (final FormatHeaderLine headerLine : formatLines) {
			final String[] vals = new String[variant.getHeader().getSamples().size()];
			for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
				final String value = headerLine.extract(variant, variant.getSampleInfo(s));
				if (value != null) vals[s] = value;
			}
			if (Arrays.stream(vals).allMatch(Objects::isNull)) continue;
			keys.add(headerLine.getId());
			sampleData.add(vals);
		}

		// keys only contain those keys present in at least 1 sample
		builder.append("\t").append(String.join(FORMAT_SEPARATOR, keys));

		// Now we add FORMAT by sample
		for (int s = 0; s < variant.getHeader().getSamples().size(); s++) {
			// Collect all values
			final List<String> sample = new ArrayList<>(keys.size());
			for (String[] ft : sampleData) sample.add(ft[s]);

			// Remove trailing nulls
			// Can this feature annoy some parsers?
			 while (sample.size() > 1 && sample.get(sample.size() - 1) == null) sample.remove(sample.size() - 1);

			// map nulls to . and join
			final String sformat = sample.stream()
					.map(v -> v == null ? VariantSet.EMPTY_VALUE : v)
					.collect(Collectors.joining(FORMAT_SEPARATOR));
			builder.append(SEPARATOR).append(sformat);
		}
	}
}
