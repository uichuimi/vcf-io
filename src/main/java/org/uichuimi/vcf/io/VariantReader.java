package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.utils.FileUtils;
import org.uichuimi.vcf.variant.Chromosome;
import org.uichuimi.vcf.variant.Coordinate;
import org.uichuimi.vcf.variant.Variant;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a VCF file or input stream. Variants can be iterated in several ways: as an iterator, as an
 * iterable an as a stream.
 * <p>
 * NOTE: to benefit from {@link Iterator} and {@link Iterable} interfaces, reading exceptions have
 * been encapsulated into {@link UncheckedIOException}, so take into account than even if compiler
 * does not enforce to capture this exceptions, iterating over a reader may produce {@link
 * IOException} as normal readers.
 */
public class VariantReader implements AutoCloseable, Iterator<Variant>, Iterable<Variant> {

	private VcfHeader header;
	private final BufferedReader reader;
	private Chromosome.Namespace namespace;
	private Variant nextVariant;

	/**
	 * Creates a reader from a file. File can be zipped or gzipped. If file is zipped, it is read to
	 * place the reader into the first byte of the first file.
	 *
	 * @param file
	 * 		input file
	 * @throws IOException
	 * 		if file is unreachable or unreadable
	 */
	public VariantReader(File file) throws IOException {
		this(FileUtils.getInputStream(file));
	}

	/**
	 * Creates a reader from an input stream. Header is read.
	 *
	 * @param input
	 * 		input stream
	 * @throws IOException
	 * 		if input is unreadable
	 */
	public VariantReader(InputStream input) throws IOException {
		this(input, Chromosome.Namespace.getDefault());
	}

	public VariantReader(InputStream input, Chromosome.Namespace namespace) throws IOException {
		this.namespace = namespace;
		reader = new BufferedReader(new InputStreamReader(input));
		header = HeaderReader.readHeader(reader);
	}

	/**
	 * Get the header
	 *
	 * @return file header
	 */
	public VcfHeader getHeader() {
		return header;
	}

	// -----------------------  Autocloseable  -------------------------- //
	@Override
	public final void close() throws IOException {
		reader.close();
	}

	// --------------------------  Iterable  ---------------------------- //
	@Override
	public Iterator<Variant> iterator() {
		return this;
	}

	// --------------------------   Stream   ---------------------------- //

	/**
	 * Generates a stream that provides variants in the order they are read as using this class as
	 * iterator or iterable.
	 *
	 * @return a stream of variants
	 */
	public final Stream<Variant> variants() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
				Spliterator.NONNULL | Spliterator.ORDERED), false);
	}

	// --------------------------  Iterator  ---------------------------- //

	/**
	 * Copied from {@link Iterator} interface:
	 * <p>
	 * Returns {@code true} if the iteration has more elements. (In other words, returns {@code
	 * true} if {@link #next} would return an element rather than throwing an exception.)
	 * <p>
	 * NOTE: this method may throw an {@link UncheckedIOException} as it is reading from an
	 * InputStream source. The exception has been encapsulated into an unchecked exception because
	 * this method is overriding the interface.
	 *
	 * @return {@code true} if the iteration has more elements
	 */
	@Override
	public boolean hasNext() {
		if (nextVariant != null) return true;
		else {
			try {
				String line = reader.readLine();
				while (line != null && line.startsWith("#"))
					line = reader.readLine();
				if (line == null) return false;
				nextVariant = new Variant(getHeader(), line, namespace);
				return true;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	@Override
	public final Variant next() {
		if (hasNext()) {
			final Variant variant = nextVariant;
			nextVariant = null;
			return variant;
		} else throw new NoSuchElementException();
	}

	// --------------------------  Specific  ---------------------------- //

	/**
	 * Get the next variant which coordinate is equals to the coordinate passed as argument. If this
	 * reader does not contain a variant with this coordinate, null is returned and all variants
	 * with coordinate less than <em>coordinate</em> will be skipped. Next variant returned by
	 * {@link VariantReader#next()} will have coordinate greater than <em>coordinate</em>.
	 *
	 * @param coordinate
	 * 		coordinate of the next variant to return
	 * @return a variant matching coordinate or null
	 */
	public Variant next(Coordinate coordinate) {
		while (hasNext()) {
			final int compare = nextVariant.getCoordinate().compareTo(coordinate);
			if (compare == 0) {
				final Variant variant = nextVariant;
				nextVariant = null;
				return variant;
			} else if (compare > 0) {
				// Do not nullify nextVariant
				return null;
			} else {
				// Force fetch of next variant
				nextVariant = null;
			}

		}
		return null;
	}

	public Collection<Variant> nextCollected(Coordinate coordinate) {
		final List<Variant> variants = new ArrayList<>();
		while (hasNext()) {
			final int compare = nextVariant.getCoordinate().compareTo(coordinate);
			if (compare > 0) break;
			if (compare == 0) variants.add(nextVariant);
			nextVariant = null;
		}
		return Collections.unmodifiableList(variants);
	}

	/**
	 * Get, but do not remove, next variant in the buffer. Multiple call to this method should
	 * return the same value.
	 *
	 * @return the next variant in the buffer. If there are no more variants in the buffer, returns
	 * null.
	 */
	public Variant peek() {
		return hasNext() ? nextVariant : null;
	}
}
