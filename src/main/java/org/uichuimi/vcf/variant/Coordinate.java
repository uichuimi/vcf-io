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

package org.uichuimi.vcf.variant;

import java.util.Objects;

/**
 * A genomic position.
 */
public class Coordinate implements Comparable<Coordinate> {

	private final Chromosome chromosome;
	private final long position;
	private final Chromosome.Namespace namespace;

	public Coordinate(String chrom, long position) {
		this(Chromosome.Namespace.getDefault(), chrom, position);
	}

	public Coordinate(Chromosome.Namespace namespace, String chromosome, long position) {
		this(namespace, Chromosome.get(chromosome, namespace), position);
	}

	public Coordinate(Chromosome.Namespace namespace, Chromosome chromosome, long position) {
		this.namespace = namespace;
		this.chromosome = chromosome;
		this.position = position;
	}

	@Override
	public int compareTo(Coordinate that) {
		final int compare = this.chromosome.compareTo(that.chromosome);
		return compare != 0 ? compare : Long.compare(this.position, that.position);
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == Coordinate.class && (obj == this || compareTo((Coordinate) obj) == 0);
	}

	@Override
	public int hashCode() {
		return Objects.hash(namespace, chromosome, position);
	}

	public String getChrom() {
		return namespace.getName(chromosome);
	}

	public long getPosition() {
		return position;
	}

	public Chromosome getChromosome() {
		return chromosome;
	}

	@Override
	public String toString() {
		return getChrom() + ":" + position;
	}

}
