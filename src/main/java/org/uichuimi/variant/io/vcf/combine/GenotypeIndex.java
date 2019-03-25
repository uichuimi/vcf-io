package org.uichuimi.variant.io.vcf.combine;

import java.util.ArrayList;
import java.util.List;

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
		while (JS.size() < maxIndex) {
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
