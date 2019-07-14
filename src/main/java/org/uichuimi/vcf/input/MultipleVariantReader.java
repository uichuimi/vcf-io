package org.uichuimi.vcf.input;

import org.uichuimi.vcf.combine.VariantMerger;
import org.uichuimi.vcf.header.ComplexHeaderLine;
import org.uichuimi.vcf.header.SimpleHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.lazy.Variant;
import org.uichuimi.vcf.utils.FileUtils;
import org.uichuimi.vcf.variant.Coordinate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads multiple VCF files or InputStreams in parallel. Calls to {@link
 * MultipleVariantReader#next()} return a list with all of the variants in all readers with the
 * lowest coordinate read. This class is ideal to merge VCF files.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MultipleVariantReader implements AutoCloseable, Iterator<Collection<Variant>>, Iterable<Collection<Variant>> {

	private final Collection<VariantReader> readers;
	private final VcfHeader header = new VcfHeader();

	/**
	 * Prepares a reader with the different inputs. Headers are
	 *
	 * @param inputs
	 * 		list of input streams
	 * @throws IOException
	 * 		if any of the inputs is unreadable
	 */
	public MultipleVariantReader(Collection<InputStream> inputs) throws IOException {
		this.readers = new ArrayList<>(inputs.size());
		for (InputStream inputStream : inputs) readers.add(new VariantReader(inputStream));
		mergeHeaders();
	}

	/**
	 * Secondary constructor. As Java does not allow override constructors with generics, we provide
	 * the files constructors with a getInstance pattern.
	 */
	public static MultipleVariantReader getInstance(Collection<File> files) throws IOException {
		final List<InputStream> is = new ArrayList<>(files.size());
		for (File file : files) is.add(FileUtils.getInputStream(file));
		return new MultipleVariantReader(is);
	}

	public VcfHeader getHeader() {
		return header;
	}

	private void mergeHeaders() {
		// Samples
		readers.stream()
				.map(VariantReader::getHeader)
				.flatMap(vcfHeader -> vcfHeader.getSamples().stream())
				.distinct()
				.forEach(header.getSamples()::add);
		// header lines
		readers.stream()
				.map(VariantReader::getHeader).forEach(vcfHeader ->
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

	// -----------------------  Autocloseable  -------------------------- //
	@Override
	public void close() throws Exception {
		for (VariantReader reader : readers) reader.close();
	}

	// --------------------------  Iterator  ---------------------------- //
	@Override
	public Collection<Variant> next() {
		final Coordinate coordinate = nextCoordinate();
		return readers.stream()
				.map(reader -> reader.next(coordinate))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/**
	 * Reads the next list of variants and merges them in a single variant.
	 *
	 * @return the next variants merged into one
	 */
	public Variant nextMerged() {
		return VariantMerger.merge(next(), header);
	}

	@Override
	public boolean hasNext() {
		return readers.stream().anyMatch(VariantReader::hasNext);
	}

	// --------------------------  Iterable  ---------------------------- //
	@Override
	public Iterator<Collection<Variant>> iterator() {
		return this;
	}

	public Iterator<Variant> mergedIterator() {
		return new Iterator<Variant>() {
			@Override
			public boolean hasNext() {
				return MultipleVariantReader.this.hasNext();
			}

			@Override
			public Variant next() {
				return nextMerged();
			}
		};
	}

	// --------------------------   Stream   ---------------------------- //
	@SuppressWarnings("unused")
	public final Stream<Collection<Variant>> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
				Spliterator.NONNULL | Spliterator.ORDERED), true);
	}

	@SuppressWarnings("unused")
	public final Stream<Variant> mergedStream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(mergedIterator(),
				Spliterator.NONNULL | Spliterator.ORDERED), true);
	}


	private Coordinate nextCoordinate() {
		return readers.stream()
				.map(VariantReader::peek)
				.filter(Objects::nonNull)
				.map(Variant::getCoordinate)
				.min(Coordinate::compareTo)
				.orElse(null);
	}

}
