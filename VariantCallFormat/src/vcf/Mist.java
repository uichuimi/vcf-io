/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package vcf;

import java.util.BitSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by uichuimi on 8/06/16.
 */
public class Mist {

    private TreeMap<String, BitSet> regions = new TreeMap<>();

    public void addRegion(String chrom, int start, int end) {
        regions.putIfAbsent(chrom, new BitSet());
        final BitSet positions = regions.get(chrom);
        positions.set(start, end);
    }

    public boolean isInMistRegion(String chrom, int position) {
        return regions.containsKey(chrom) && regions.get(chrom).get(position);
    }
}
