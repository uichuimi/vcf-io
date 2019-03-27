package org.uichuimi.variant.io;

import org.uichuimi.variant.io.vcf.variant.GenotypeIndex;
import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * High performance variant splitter. Given a variant, it returns a list of variants, each one representing a different
 * alternative allele. It takes care of special INFO and format fields, translating coordinates from origin variant to
 * target variants.
 */
public class VariantSplitter {

	private final VcfHeader header;

	/**
	 * Creates a new VariantSplitter for variants belonging to this header.
	 *
	 * @param header header associated to the variants it is going to split.
	 */
	public VariantSplitter(VcfHeader header) {
		this.header = header;
	}

	/**
	 * Get the list of variants represented by this variant. There will be one variant per alternative allele.
	 */
	public Collection<Variant> split(Variant variant) {
		final String[] alts = variant.getAltArray();
		if (alts.length == 1)
			return Collections.singletonList(variant);
		final List<Variant> rtn = new ArrayList<>(alts.length);
		for (String alt : alts) {
			final Variant var = new Variant(variant.getChrom(), variant.getPosition(), variant.getRef(), alt, variant.getVcfHeader());
			rtn.add(var);
			var.setId(variant.getId());
			var.setQual(variant.getQual());
			var.setFilter(variant.getFilter());
		}
		// To go faster, we will split by field
		variant.getInfo().forEach((key, value) -> splitField(key, value, rtn));

		for (String sample : header.getSamples())
			variant.getSampleInfo().forEach(sample, (key, value) -> splitFormat(rtn, sample, key, value));
		return rtn;
	}

	private void splitField(String key, Object value, List<Variant> variants) {
		final ComplexHeaderLine info = header.getComplexHeader("INFO", key);
		final Object[] values = extract(variants.size(), value, info);
		for (int i = 0; i < variants.size(); i++)
			variants.get(i).getInfo().set(key, values[i]);
	}

	private void splitFormat(List<Variant> rtn, String sample, String key, Object value) {
		final ComplexHeaderLine format = header.getComplexHeader("FORMAT", key);
		final Object[] values = extract(rtn.size(), value, format);
		for (int i = 0; i < rtn.size(); i++)
			rtn.get(i).getSampleInfo().setFormat(sample, key, values[i]);
	}

	private static Object[] extract(int size, Object field, ComplexHeaderLine header) {
		// GT has special treatment
		if (header.getValue("ID").equals("GT")) {
			final Object[] rtn = new Object[size];
			final String gt = (String) field;
			final String[] gts;
			final String sep;
			if (gt.contains("/")) {
				sep = "/";
				gts = gt.split("/");
			} else {
				sep = "|";  // This symbol has meaning in a regex and must be escaped
				gts = gt.split("\\|");
			}
			assert gts.length == 2; // assuming ploidy is 2
			for (int i = 0; i < size; i++) {
				final String a = transformGt(gts[0], i);
				final String b = transformGt(gts[1], i);
				rtn[i] = String.format("%s%s%s", a, sep, b);
			}
			return rtn;
		}
		final String number = header.getValue("Number");
		switch (number) {
			case "A":
				// A: 1 per alternative allele
				return (Object[]) field;
			case "R": {
				// R: 1 per allele (reference + alternative)
				// R,A,B,C to [[R,A],[R,B],[R,C]]
				final Object[] rtn = new Object[size];
				final Object[] values = (Object[]) field;
				for (int i = 0; i < size; i++)
					rtn[i] = String.format("%s,%s", values[0], values[i + 1]);
				return rtn;
			}
			case "G": {
				// G: 1 per genotype
				// when split, each variant contains 3 genotypes: 0/0, 0/1, 1/1
				final Object[] rtn = new Object[size];
				final Object[] values = (Object[]) field;
				for (int i = 0; i < size; i++)
					rtn[i] = String.format("%s,%s,%s", getValue(values, 0, 0), getValue(values, 0, i + 1), getValue(values, i + 1, i + 1));
				return rtn;
			}
			default:
				// No special treatment, the same object for everybody
				final Object[] rtn = new Object[size];
				for (int i = 0; i < size; i++) rtn[i] = field;
				return rtn;
		}
	}

	/**
	 *
	 * @param gt a single gt value
	 * @param index alternative index
	 * @return "0" if gt is "0", "1" if gt = index + 1, "." otherwise
	 */
	private static String transformGt(String gt, int index) {
		if (gt.equals(".")) return ".";
		final int j = Integer.parseInt(gt);
		if (j == 0) return "0";
		// Alternatives start in 1
		if (j == index + 1) return "1";
		return ".";
	}

	private static Object getValue(Object[] values, int j, int k) {
		return values[GenotypeIndex.get(j, k)];
		// https://samtools.github.io/hts-specs/VCFv4.2.pdf
		// See GL field spec (around page 6)
		// if a variant has 1 alt, values will contain 3 values: RR, RA, AA
		// if a variant has 1 alts, values will contain 6 values: RR, RA, AA, RB, AB, BB
		// where R is the reference and A and B alternative alleles. The index of each genotype is given by the indices
		// of its alleles (j and k)
//		final int pos = (int) ((k * (k + 1) / 2.) + j);
//		return values[pos];
	}
}
