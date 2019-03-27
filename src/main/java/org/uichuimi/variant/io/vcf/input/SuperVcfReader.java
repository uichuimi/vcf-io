package org.uichuimi.variant.io.vcf.input;

import org.uichuimi.variant.io.vcf.Coordinate;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;
import org.uichuimi.variant.io.vcf.io.VariantSetReader;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SuperVcfReader implements AutoCloseable, Iterator<SuperVariant> {


	protected final VcfHeader header;
	protected final BufferedReader reader;
	private SuperVariant nextVariant;
	private SuperVariantFactory variantFactory;

	public SuperVcfReader(InputStream is) {
		reader = new BufferedReader(new InputStreamReader(is));
		header = VariantSetFactory.readHeader(reader);
		variantFactory = new SuperVariantFactory(header);
	}

	public SuperVcfReader(File file) throws FileNotFoundException {
		header = VariantSetFactory.readHeader(file);
		reader = new BufferedReader(new FileReader(file));
		variantFactory = new SuperVariantFactory(header);
	}

	public final Stream<SuperVariant> variants() {
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
	protected SuperVariant createVariant(String line) {
		return variantFactory.parse(line);
	}

	@Override
	public final SuperVariant next() {
		if (hasNext()) {
			final SuperVariant variant = nextVariant;
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
	public SuperVariant peek() {
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
	public SuperVariant next(Coordinate coordinate) {
		while (hasNext()) {
			final int compare = nextVariant.getCoordinate().compareTo(coordinate);
			if (compare == 0) {
				final SuperVariant variant = nextVariant;
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
