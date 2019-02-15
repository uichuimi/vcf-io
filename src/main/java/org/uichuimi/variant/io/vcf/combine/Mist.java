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

package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.Variant;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Keeps an in-memory representation of a Mist file.
 * Created on 8/06/16.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Mist {

	/**
	 * Key of treeMap is the contig. TreeRegions are sorted by [start,end]
	 */
	private TreeMap<String, TreeSet<MistRegion>> treeMap = new TreeMap<>();

	public void addRegion(String chrom, int start, int end) {
		treeMap.putIfAbsent(chrom, new TreeSet<>());
		treeMap.get(chrom).add(new MistRegion(start, end));
	}

	public boolean isInMistRegion(Variant variant) {
		return isInMistRegion(variant.getChrom(), variant.getPosition());
	}

	public boolean isInMistRegion(String chrom, int position) {
		if (!treeMap.containsKey(chrom)) return false;
		final TreeSet<MistRegion> mistRegions = treeMap.get(chrom);
		return mistRegions.stream().anyMatch(mistRegion -> mistRegion.contains(position));
	}

	private class MistRegion implements Comparable<MistRegion> {
		int start;
		int end;

		MistRegion(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public int compareTo(MistRegion other) {
			final int compare = Integer.compare(start, other.start);
			return compare != 0
					? compare
					: Integer.compare(end, other.end);
		}

		public boolean contains(int position) {
			return start <= position && position <= end;
		}
	}
}
