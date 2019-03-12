package org.uichuimi.variant.io.vcf.io;

import org.uichuimi.variant.io.vcf.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class MultipleVcfReader implements AutoCloseable, Iterator<Collection<Variant>> {

	private final Collection<VariantSetReader> readers;
	private final VcfHeader header = new VcfHeader();

	public MultipleVcfReader(Collection<InputStream> inputStreams) {
		this.readers = new ArrayList<>(inputStreams.size());
		for (InputStream inputStream : inputStreams) readers.add(new VariantSetReader(inputStream));
		mergeHeaders();
	}

	/**
	 * Secondary constructor. As Java does not allow override constructors with generics, we provide
	 * the files constructors with a getInstance pattern.
	 */
	public static MultipleVcfReader getInstance(Collection<File> files) throws FileNotFoundException {
		final List<InputStream> is = new ArrayList<>(files.size());
		for (File file : files) is.add(new FileInputStream(file));
		return new MultipleVcfReader(is);
	}

	public VcfHeader getHeader() {
		return header;
	}

	@Override
	public void close() throws Exception {
		for (VariantSetReader reader : readers) reader.close();
	}

	@Override
	public boolean hasNext() {
		return readers.stream().anyMatch(VariantSetReader::hasNext);
	}

	@Override
	public Collection<Variant> next() {
		final Coordinate coordinate = nextCoordinate();
		return readers.stream()
				.map(reader -> reader.next(coordinate))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public Variant nextMerged() {
		return VariantMerger.merge(next(), header);
	}

	private Coordinate nextCoordinate() {
		return readers.stream()
				.map(VariantSetReader::peek)
				.filter(Objects::nonNull)
				.map(Variant::getCoordinate)
				.min(Coordinate::compareTo)
				.orElse(null);
	}

	private void mergeHeaders() {
		// Samples
		readers.stream()
				.map(VariantSetReader::header)
				.flatMap(vcfHeader -> vcfHeader.getSamples().stream())
				.distinct()
				.forEach(header.getSamples()::add);
		// header lines
		readers.stream()
				.map(VariantSetReader::header).forEach(vcfHeader ->
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
		for (ComplexHeaderLine headerLine : header.getComplexHeaders()) {
			if (headerLine.getKey().equals(sourceHeader.getKey())
					&& headerLine.getValue("ID").equals(sourceHeader.getValue("ID")))
				return true;
		}
		return false;
	}

}
