package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.variant.Coordinate;

/**
 * Stores <em>chromosome</em> and <em>position</em> of a coordinate as strings. When {@link
 * LazyProperty#getValue()} is called, they are converted into a Coordinate and nullified.
 */
class CoordinateProperty extends LazyProperty<Coordinate> {

	private String position;

	CoordinateProperty(String chromosome, String position) {
		super(chromosome);
		this.position = position;
	}

	CoordinateProperty(Coordinate coordinate) {
		super(coordinate);
	}

	@Override
	protected Coordinate parse(String chromosome) {
		final Coordinate coordinate = new Coordinate(chromosome, Integer.valueOf(position));
		position = null;
		return coordinate;
	}
}
