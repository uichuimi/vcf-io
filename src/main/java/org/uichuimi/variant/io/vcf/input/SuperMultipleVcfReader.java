package org.uichuimi.variant.io.vcf.input;

import org.uichuimi.variant.io.vcf.Coordinate;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.combine.SuperVariantMerger;
import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.SimpleHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class SuperMultipleVcfReader implements AutoCloseable, Iterator<Collection<VariantContext>> {

	private final Collection<SuperVcfReader> readers;
	private final VcfHeader header = new VcfHeader();

	public SuperMultipleVcfReader(Collection<InputStream> inputStreams) {
		this.readers = new ArrayList<>(inputStreams.size());
		for (InputStream inputStream : inputStreams) readers.add(new SuperVcfReader(inputStream));
		mergeHeaders();
	}

	/**
	 * Secondary constructor. As Java does not allow override constructors with generics, we provide
	 * the files constructors with a getInstance pattern.
	 */
	public static SuperMultipleVcfReader getInstance(Collection<File> files) throws IOException {
		final List<InputStream> is = new ArrayList<>(files.size());
		for (File file : files) is.add(FileUtils.getInputStream(file));
		return new SuperMultipleVcfReader(is);
	}

	public VcfHeader getHeader() {
		return header;
	}

	@Override
	public void close() throws Exception {
		for (SuperVcfReader reader : readers) reader.close();
	}

	@Override
	public boolean hasNext() {
		return readers.stream().anyMatch(SuperVcfReader::hasNext);
	}

	@Override
	public Collection<VariantContext> next() {
		final Coordinate coordinate = nextCoordinate();
		return readers.stream()
				.map(reader -> reader.next(coordinate))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public VariantContext nextMerged() {
		return SuperVariantMerger.merge(next(), header);
	}

	private Coordinate nextCoordinate() {
		return readers.stream()
				.map(SuperVcfReader::peek)
				.filter(Objects::nonNull)
				.map(VariantContext::getCoordinate)
				.min(Coordinate::compareTo)
				.orElse(null);
	}

	private void mergeHeaders() {
		// Samples
		readers.stream()
				.map(SuperVcfReader::header)
				.flatMap(vcfHeader -> vcfHeader.getSamples().stream())
				.distinct()
				.forEach(header.getSamples()::add);
		// header lines
		readers.stream()
				.map(SuperVcfReader::header).forEach(vcfHeader ->
				vcfHeader.getHeaderLines().forEach(sourceHeader -> {
					if (sourceHeader instanceof SimpleHeaderLine)
						addSimpleHeader((SimpleHeaderLine) sourceHeader);
					if (sourceHeader instanceof ComplexHeaderLine)
						addComplexHeader((ComplexHeaderLine) sourceHeader);
				}));
	}

	private void addSimpleHeader(SimpleHeaderLine sourceHeader) {
		if (headerContains(sourceHeader)) return;
		header.getHeaderLines().add(sourceHeader);
	}

	private boolean headerContains(SimpleHeaderLine sourceHeader) {
		for (SimpleHeaderLine headerLine : header.getSimpleHeaders())
			if (headerLine.getKey().equals(sourceHeader.getKey())
					&& headerLine.getValue().equals(sourceHeader.getValue()))
				return true;
		return false;
	}

	private void addComplexHeader(ComplexHeaderLine sourceHeader) {
		if (headerContains(sourceHeader)) return;
		header.getHeaderLines().add(sourceHeader);

	}

	private boolean headerContains(ComplexHeaderLine sourceHeader) {
		return header.hasComplexHeader(sourceHeader.getKey(), sourceHeader.getValue("ID"));
	}

}
