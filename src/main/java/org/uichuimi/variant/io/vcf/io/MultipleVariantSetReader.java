/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with VariantCallFormat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.uichuimi.variant.io.vcf.io;

import org.uichuimi.variant.io.vcf.variant.Coordinate;
import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.combine.VariantMerger;
import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.SimpleHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Reads 1 or more VCF files at the same time. By repeatedly calling
 * <code>next()</code> you can walk along genome coordinates, by having all
 * variants found in each position. Coordinates with no variants present are not
 * reported.
 * <p>
 * Sample code:
 * </p>
 * <code>
 * try (VariantSetReaderList readerList = new VariantSetReaderList(new LinkedList&lt;&gt;(files))) {
 * while (readerList.hasNext()) {
 * final List&lt;Variant&gt; variants = readerList.next();
 * for (Variant variant : variants) {
 * // do something with each variant
 * }
 * }
 * }
 * </code>
 * <p>
 * If you are not using each variant individually, maybe you want to use the
 * merged version:
 * </p>
 * <code>
 * try (VariantSetReaderList readerList = new VariantSetReaderList(new LinkedList&lt;&gt;(files))) {
 * while (readerList.hasNext()) {
 * final Variant variant = readerList.nextMerged();
 * // do something with the variant
 * }
 * }
 * </code>
 * When using the merged version, a merged header is created by joining all the
 * vcf headers, avoiding redundant lines. To merge variants, the first variant
 * is used as base. Then, for each other, the ID is updated and the INFO and
 * FORMAT fields are filled with the missing values. NOTE that QUAL and filter
 * values are not updated, and INFO values as DP or AC are not recalculated.
 * This is a work in progress. This version is useful when using only genotype
 * information and INFO values such as frequencies or consequences.
 * @deprecated Use {@link MultipleVcfReader} instead
 */
@Deprecated
public class MultipleVariantSetReader implements AutoCloseable {

	private final List<VariantBuffer> buffers = new LinkedList<>();
	private VcfHeader header;

	/**
	 * Creates a list of opened VariantSetReaders
	 *
	 * @param files list of samples with criteria
	 * @throws FileNotFoundException if any of the files is not found
	 */
	public MultipleVariantSetReader(Collection<File> files) throws FileNotFoundException {
		for (File sample : files) buffers.add(new VariantBuffer(sample));
		mergeHeaders();
	}

	@Override
	public void close() throws Exception {
		for (VariantBuffer buffer : buffers) buffer.getReader().close();
	}

	/**
	 * @return true if there are remaining variants in any of the input files
	 */
	public boolean hasNext() {
		return buffers.stream()
				.map(VariantBuffer::getNext)
				.anyMatch(Objects::nonNull);
	}

	/**
	 * Get a list of variants in the next coordinate where there is at least one
	 * variant and null when all variants have been consumed.
	 *
	 * @return a list of at least one variant in the next coordinate
	 */
	public List<Variant> next() {
		final Coordinate nextCoordinate = nextCoordinate();
		final List<Variant> next = new LinkedList<>();
		for (VariantBuffer buffer : buffers) {
			if (buffer.getNext() != null) {
				if (buffer.getNext().getCoordinate().equals(nextCoordinate)) {
					next.add(buffer.getNext());
					if (buffer.getReader().hasNext())
						buffer.setNext(buffer.getReader().next());
					else buffer.setNext(null);
				}
			}
		}
		return next;
	}

	private Coordinate nextCoordinate() {
		return buffers.stream()
				.map(VariantBuffer::getNext)
				.filter(Objects::nonNull)
				.map(Variant::getCoordinate)
				.min(Coordinate::compareTo)
				.orElse(null);
	}

	public Variant nextMerged() {
		return VariantMerger.merge(next(), header);
	}

	private void mergeHeaders() {
		header = new VcfHeader();
		// Samples
		buffers.stream().map(VariantBuffer::getReader)
				.map(VariantSetReader::header)
				.flatMap(vcfHeader -> vcfHeader.getSamples().stream())
				.distinct()
				.forEach(header.getSamples()::add);
		// header lines
		buffers.stream().map(VariantBuffer::getReader)
				.map(VariantSetReader::header).forEach(vcfHeader ->
				vcfHeader.getHeaderLines().forEach(sourceHeader -> {
					if (sourceHeader.getClass() == SimpleHeaderLine.class)
						addSimpleHeader((SimpleHeaderLine) sourceHeader);
					if (sourceHeader.getClass() == ComplexHeaderLine.class)
						addComplexHeader((ComplexHeaderLine) sourceHeader);
				}));
	}

	private void addSimpleHeader(SimpleHeaderLine sourceHeader) {
		if (headerContainsSimpleHeaderLine(sourceHeader)) return;
		header.getHeaderLines().add(sourceHeader);
	}

	private boolean headerContainsSimpleHeaderLine(SimpleHeaderLine sourceHeader) {
		for (SimpleHeaderLine headerLine : header.getSimpleHeaders())
			if (headerLine.getKey().equals(sourceHeader.getKey())
					&& headerLine.getValue().equals(sourceHeader.getValue()))
				return true;
		return false;
	}

	private void addComplexHeader(ComplexHeaderLine sourceHeader) {
		if (headerContainsComplexHeaderLine(sourceHeader)) return;
		header.getHeaderLines().add(sourceHeader);

	}

	private boolean headerContainsComplexHeaderLine(ComplexHeaderLine sourceHeader) {
		for (ComplexHeaderLine headerLine : header.getComplexHeaders()) {
			if (headerLine.getKey().equals(sourceHeader.getKey())
					&& headerLine.getValue("ID").equals(sourceHeader.getValue("ID")))
				return true;
		}
		return false;
	}

	/**
	 * Gets the header in case you want to use use the merged version
	 *
	 * @return the merged header
	 */
	public VcfHeader getMergedHeader() {
		return header;
	}

	private class VariantBuffer {
		private final VariantSetReader reader;
		private Variant next;

		VariantBuffer(File file) throws FileNotFoundException {
			reader = new VariantSetReader(file);
			setNext(getReader().next());

		}

		public Variant getNext() {
			return next;
		}

		public void setNext(Variant next) {
			this.next = next;
		}

		public VariantSetReader getReader() {
			return reader;
		}
	}
}
