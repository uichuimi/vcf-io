package org.uichuimi.variant.io.vcf;

public class MultiLevelInfo {

	private final Info info = new Info();
	private final Info[] alleleInfo;
	private final Info[] genotypeInfo;
	private final int numberOfAlleles;
	private final int numberOfGenotypes;

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

	public Info getInfo() {
		return info;
	}

	public Info getAlleleInfo(int index) {
		if (alleleInfo[index] == null) alleleInfo[index] = new Info();
		return alleleInfo[index];
	}

	public Info getGenotypeInfo(int index) {
		if (genotypeInfo[index] == null) genotypeInfo[index] = new Info();
		return genotypeInfo[index];
	}

	public int getNumberOfAlleles() {
		return numberOfAlleles;
	}

	public int getNumberOfGenotypes() {
		return numberOfGenotypes;
	}
}
