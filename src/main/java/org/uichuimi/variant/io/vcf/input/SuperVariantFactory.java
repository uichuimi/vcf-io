package org.uichuimi.variant.io.vcf.input;

import org.uichuimi.variant.io.vcf.Coordinate;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.header.FormatHeaderLine;
import org.uichuimi.variant.io.vcf.header.InfoHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SuperVariantFactory {

	private static final String EMPTY_VALUE = ".";
	private static final String SECONDARY_SEPARATOR = ",";
	private static final String SEPARATOR = "\t";
	private static final String INFO_SEPARATOR = ";";
	private static final String KEY_VALUE_SEPARATOR = "=";


	private final VcfHeader header;
	private final Map<String, InfoHeaderLine> infoHeaders;
	private final Map<String, FormatHeaderLine> formatHeaders;

	public SuperVariantFactory(VcfHeader header) {
		this.header = header;
		infoHeaders = header.getHeaderLines().stream()
				.filter(InfoHeaderLine.class::isInstance)
				.map(InfoHeaderLine.class::cast)
				.collect(Collectors.toMap(InfoHeaderLine::getId, Function.identity()));
		formatHeaders = header.getHeaderLines().stream()
				.filter(FormatHeaderLine.class::isInstance)
				.map(FormatHeaderLine.class::cast)
				.collect(Collectors.toMap(FormatHeaderLine::getId, Function.identity()));

	}

	public final VariantContext parse(String line) {
		final String[] row = line.split(SEPARATOR);

		final String chrom = row[0];
		final int position = Integer.parseInt(row[1]);
		final Coordinate coordinate = new Coordinate(chrom, position);
		final List<String> refs = Arrays.asList(row[3].split(SECONDARY_SEPARATOR));
		final List<String> alts = Arrays.asList(row[4].split(SECONDARY_SEPARATOR));
		final VariantContext variant = new VariantContext(header, coordinate, refs, alts);

		final List<String> ids = row[2].equals(EMPTY_VALUE)
				? Collections.emptyList()
				: Arrays.asList(row[2].split(SECONDARY_SEPARATOR));

		final double quality = Double.valueOf(row[5]);
		final List<String> filter = row[6].equals(EMPTY_VALUE)
				? Collections.emptyList()
				: Arrays.asList(row[6].split(SECONDARY_SEPARATOR));
		variant.getIds().addAll(ids);
		variant.setQuality(quality);
		variant.getFilters().addAll(filter);

		if (row.length > 7) parseInfo(variant, row[7]);
		if (row.length > 8) parseFormat(variant, row);
		return variant;
	}

	private void parseInfo(VariantContext variant, String info) {
		final String[] infos = info.split(INFO_SEPARATOR);
		for (String s : infos) {
			final String[] keyValue = s.split(KEY_VALUE_SEPARATOR);
			final String key = keyValue[0];
			final String value = keyValue.length > 1 ? keyValue[1] : null;
			final InfoHeaderLine infoHeaderLine = getOrCreateInfo(key);
			infoHeaderLine.apply(variant, variant, value);
		}
	}

	private InfoHeaderLine getOrCreateInfo(String key) {
		InfoHeaderLine infoHeaderLine = infoHeaders.get(key);
		if (infoHeaderLine != null) return infoHeaderLine;
		Logger.getLogger("vcf-io").warning(key + " INFO header is not present, assuming String");
		final Map<String, String> map = new HashMap<>();
		map.put("ID", key);
		map.put("Number", "1");
		map.put("Type", "String");
		map.put("Description", key);
		infoHeaderLine = new InfoHeaderLine(map);
		infoHeaders.put(key, infoHeaderLine);
		header.getHeaderLines().add(infoHeaderLine);
		return infoHeaderLine;
	}

	private void parseFormat(VariantContext variant, String[] row) {
		final String[] keys = row[8].split(":");
		for (int i = 0; i < header.getSamples().size(); i++) {
			final String[] format = row[9 + i].split(":");
			for (int f = 0; f < format.length; f++) {
				final String key = keys[f];
				formatHeaders.get(key).apply(variant, variant.getSampleInfo(i), format[f]);
			}
		}
	}
}
