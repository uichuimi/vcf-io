package org.uichuimi.vcf.input;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.io.VariantSetFactory;
import org.uichuimi.vcf.io.VariantSetReader;
import org.uichuimi.vcf.variant.Coordinate;
import org.uichuimi.vcf.variant.VariantContext;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class VariantContextReader implements AutoCloseable, Iterator<VariantContext> {


	protected final VcfHeader header;
	protected final BufferedReader reader;
	private VariantContext nextVariant;
	private VariantContextFactory variantFactory;

	public VariantContextReader(InputStream is) {
		reader = new BufferedReader(new InputStreamReader(is));
		header = VariantSetFactory.readHeader(reader);
		variantFactory = new VariantContextFactory(header);
	}

	public VariantContextReader(File file) throws FileNotFoundException {
		header = VariantSetFactory.readHeader(file);
		reader = new BufferedReader(new FileReader(file));
		variantFactory = new VariantContextFactory(header);
	}

	public final Stream<VariantContext> variants() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
				Spliterator.NONNULL | Spliterator.ORDERED), true);
	}

	@Override
	public final void close() throws IOException {
		reader.close();
	}

	public final VcfHeader header() {
		return header;
	}

	@Override
	public final boolean hasNext() {
		if (nextVariant != null) return true;
		else {
			try {
				String line = reader.readLine();
				while (line != null && line.startsWith("#"))
					line = reader.readLine();
				if (line == null) return false;
				nextVariant = createVariant(line);
				return true;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	/**
	 * This is the only intended method to be overwritten. VariantSetReader encapsulates the logic
	 * of opening, iterating and closing the file.
	 *
	 * @param line each variant line found in file
	 * @return a variant
	 */
	protected VariantContext createVariant(String line) {
		return variantFactory.parse(line);
	}

	@Override
	public final VariantContext next() {
		if (hasNext()) {
			final VariantContext variant = nextVariant;
			nextVariant = null;
			return variant;
		} else throw new NoSuchElementException();
	}

	/**
	 * Get, but do not remove, next variant in the buffer. Multiple call to this method should
	 * return the same value.
	 *
	 * @return the next variant in the buffer. If there are no more variants in the buffer, returns
	 * null.
	 */
	public VariantContext peek() {
		return hasNext() ? nextVariant : null;
	}

	/**
	 * Get the next variant which coordinate is equals to the coordinate passed as argument. If this
	 * reader does not contain a variant with this coordinate, null is returned and all variants
	 * with coordinate less than
	 * <em>coordinate</em> will be skipped. Next variant returned by {@link
	 * VariantSetReader#next()}
	 * will have coordinate greater than <em>coordinate</em>.
	 *
	 * @param coordinate coordinate of the next variant to return
	 * @return a variant matching coordinate or null
	 */
	public VariantContext next(Coordinate coordinate) {
		while (hasNext()) {
			final int compare = nextVariant.getCoordinate().compareTo(coordinate);
			if (compare == 0) {
				final VariantContext variant = nextVariant;
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

}
