package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.io.VariantSetFactory;
import org.uichuimi.vcf.variant.Coordinate;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class VariantReader implements AutoCloseable, Iterator<Variant>, Iterable<Variant> {


	private final VcfHeader header;
	private final BufferedReader reader;
	private Variant nextVariant;

	public VariantReader(InputStream is) {
		reader = new BufferedReader(new InputStreamReader(is));
		header = VariantSetFactory.readHeader(reader);
	}

	public VariantReader(File file) throws FileNotFoundException {
		header = VariantSetFactory.readHeader(file);
		reader = new BufferedReader(new FileReader(file));
	}

	/**
	 * get the header. Kept for retro compatibility.
	 *
	 * @return file header
	 */
	public final VcfHeader header() {
		return header;
	}

	/**
	 * Get the header
	 *
	 * @return file header
	 */
	public VcfHeader getHeader() {
		return header;
	}

	// --------------------------  Iterable  ---------------------------- //
	@Override
	public Iterator<Variant> iterator() {
		return this;
	}

	// --------------------------   Stream   ---------------------------- //
	public final Stream<Variant> variants() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
				Spliterator.NONNULL | Spliterator.ORDERED), true);
	}

	// -----------------------  Autocloseable  -------------------------- //
	@Override
	public final void close() throws IOException {
		reader.close();
	}

	// --------------------------  Iterator  ---------------------------- //
	@Override
	public final boolean hasNext() {
		if (nextVariant != null) return true;
		else {
			try {
				String line = reader.readLine();
				while (line != null && line.startsWith("#"))
					line = reader.readLine();
				if (line == null) return false;
				nextVariant = new Variant(header, line);
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
	 * {@link VariantSetReader#next()} will have coordinate greater than <em>coordinate</em>.
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
