package org.uichuimi.variant.io.vcf.variant;

import java.util.ArrayList;
import java.util.List;

/**
 * Genotypes and their data are stored in lists. The number of genotypes is determined by the number of alleles. This
 * class determines the position of a particular genotype given its alleles' positions. It also do the reversed
 * operation: given a genotype position, return the alleles' positions.
 * <pre>
 *            +---+---+---+
 *          a | 0 | 1 | 2 |
 *            +---+---+---+
 *      b
 *    +---+   +---+---+---+
 *    | 0 |   | 0 |   |   |
 *    +---+   +---+---+---+
 *    | 1 |   | 1 | 2 |   |
 *    +---+   +---+---+---+
 *    | 2 |   | 3 | 4 | 5 |
 *    +---+   +---+---+---+
 * </pre>
 * For example, the position 2 is for genotype 1/1. The genotype 1/2 is at position 4.
 *
 */
public class GenotypeIndex {

	private static int genotypes = 0;
	private static int g = 0;

	/**
	 * Precomputed values for j in genotype arrays
	 */
	private static final List<Integer> JS = new ArrayList<>();
	/**
	 * Precomputed values for k in genotype arrays
	 */
	private static final List<Integer> KS = new ArrayList<>();
	/**
	 * Precomputed indexes in genotype arrays
	 */
	private static final List<List<Integer>> GS = new ArrayList<>();

	public static int get(int j, int k) {
		if (GS.size() <= k) resize((int) ((k * (k + 1) / .2) + j));
		// swap j and k, to only use the values below the diagonal
		if (j > k) {
			int aux = j;
			j = k;
			k = aux;
		}
		return GS.get(k).get(j);
	}

	public static int getK(int i) {
		if (KS.size() <= i) resize(i);
		return KS.get(i);
	}

	public static int getJ(int i) {
		if (JS.size() <= i) resize(i);
		return JS.get(i);
	}

	private static void resize(int maxIndex) {
		while (JS.size() <= maxIndex) {
			final List<Integer> gs = new ArrayList<>();
			for (int i = 0; i <= genotypes; i++) {
				JS.add(i);
				KS.add(genotypes);
				gs.add(g++);
			}
			GS.add(gs);
			genotypes++;
		}
	}
}
