package org.uichuimi.variant.io.vcf;

/**
 * Contains 3 levels of info storage:
 * <ol>
 * <li><b>global:</b> data with Number=.,0,1,2,3,4... (DP,QD)</li>
 * <li><b>per allele:</b> data with Number=R,A (AF,AD)</li>
 * <li><b>per genotype:</b> data with Number=G (PL)</li>
 * </ol>
 * All data is stored in {@link Info} objects, as key=value.
 * <p>
 * <b>global</b> information is the information that does not belong to any allele. It is indicated in the header lines
 * with Number=<em>n</em>, where <em>n</em> is an integer number (0,1,2,3,...) or the dot (.) character, indicating
 * how many values are expected. The dot character '.' means an undetermined number of values. For a variant they are
 * usually general stats such as DP or QD or more platform dependent such as BaseQRankSum. In a sample, GT, GQ or DP
 * have Number=1.
 * </p>
 * <p>
 * <b>per allele</b> information expresses values attached to a specific allele, either a reference or alternative
 * allele. This corresponds to tags with Number=[R|A], where R means all of the alleles and A only the alternatives.
 * The {@link Info} objects have the same position as their alleles.
 * </p>
 * <p>
 * <b>per genotype</b> is used for Number=G and has a value per genotype. Each genotype is a combination of 2 alleles
 * (we only support diploidy). The {@link Info} objects have the same order as their genotypes (see {@link
 * GenotypeIndex}). Variants do not usually use this level of storage, but samples have several tags, such as GL or PL.
 * </p>
 */
public class MultiLevelInfo {

	private final Info info = new Info();
	private final Info[] alleleInfo;
	private final Info[] genotypeInfo;
	private final int numberOfAlleles;
	private final int numberOfGenotypes;

	/**
	 * Creates a new {@link MultiLevelInfo} object. The number of reference and alternative alleles are needed to create
	 * the {@link Info} containers.
	 *
	 * @param references   number of reference alleles
	 * @param alternatives number of alternative alleles
	 */
	public MultiLevelInfo(int references, int alternatives) {
		this.numberOfAlleles = references + alternatives;
		this.numberOfGenotypes = computeGenotypes(numberOfAlleles);
		this.alleleInfo = new Info[numberOfAlleles];
		this.genotypeInfo = new Info[numberOfGenotypes];
	}

	private int computeGenotypes(int numberOfAlleles) {
		int numberOfGenotypes = 0;
		for (int i = 1; i <= numberOfAlleles; i++) numberOfGenotypes += i;
		return numberOfGenotypes;
	}

	/**
	 * Get the global information of this object.
	 */
	public Info getInfo() {
		return info;
	}

	/**
	 * Get the information of the allele in index.
	 */
	public Info getAlleleInfo(int index) {
		if (alleleInfo[index] == null) alleleInfo[index] = new Info();
		return alleleInfo[index];
	}

	/**
	 * Get the information of the genotype in index.
	 */
	public Info getGenotypeInfo(int index) {
		if (genotypeInfo[index] == null) genotypeInfo[index] = new Info();
		return genotypeInfo[index];
	}

	/**
	 * Get total number of alleles, references and alternatives.
	 */
	public int getNumberOfAlleles() {
		return numberOfAlleles;
	}

	/**
	 * Get total number of genotypes.
	 */
	public int getNumberOfGenotypes() {
		return numberOfGenotypes;
	}
}
