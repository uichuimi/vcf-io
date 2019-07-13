package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.variant.Coordinate;

public class CoordinateProperty extends LazyProperty<Coordinate> {

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
		return new Coordinate(chromosome, Integer.valueOf(position));
	}
}
