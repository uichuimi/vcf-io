package org.uichuimi.vcf.variant;

/**
 * Stores <em>chromosome</em> and <em>position</em> of a coordinate as strings. When {@link
 * LazyProperty#getValue()} is called, they are converted into a Coordinate and nullified.
 */
class CoordinateProperty extends LazyProperty<Coordinate> {

	private String position;
	private Chromosome.Namespace namespace;

	CoordinateProperty(String chromosome, String position, Chromosome.Namespace namespace) {
		super(chromosome);
		this.position = position;
		this.namespace = namespace;
	}

	CoordinateProperty(Coordinate coordinate) {
		super(coordinate);
	}

	@Override
	protected Coordinate parse(String chromosome) {
		final Coordinate coordinate = new Coordinate(namespace, chromosome, Integer.parseInt(position));
		position = null;
		namespace = null;
		return coordinate;
	}
}
