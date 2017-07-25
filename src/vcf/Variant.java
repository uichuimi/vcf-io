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


import utils.StringStore;

/**
 * Stores a vcf. chrom, position, ref, alt, filter and format are Strings. position is an integer, qual a
 * double. Info is stored as a map of key==value. If value is null, key is treated as a flag.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Variant implements Comparable<Variant> {

    /*
     * A VCF line can hold more than one variant, for instance A/T,TAC
     * In this case, some fields have several values separated by comma (,)
     * Some of these fields are ALT, AC, AF, MLEAF and PL
     *
     * SQZ102
     * 1	11944422	.	TACACACAC	T,TAC	2337.73	.	AC=1,1;AF=0.500,0.500;AN=2;DP=61;ExcessHet=3.0103;FS=0.000;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=30.26;SOR=6.701	GT:AD:DP:GQ:PL	1/2:0,9,48:57:99:2375,1613,1523,302,0,133
     * 1	11944422	rs33981344	TAC	T	177.73	.	AC=1;AF=0.500;AN=2;BaseQRankSum=-2.379e+00;ClippingRankSum=0.00;DB;DP=26;ExcessHet=3.0103;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=60.00;MQRankSum=0.00;QD=9.87;ReadPosRankSum=1.87;SOR=0.540	GT:AD:DP:GQ:PL	0/1:12,6:22:99:215,0,316
     * 1	11944422	.	T	TACACACAC	680.73	.	AC=1;AF=0.500;AN=2;BaseQRankSum=-2.124e+00;ClippingRankSum=0.00;DP=62;ExcessHet=3.0103;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=59.79;MQRankSum=0.00;QD=17.02;ReadPosRankSum=-7.720e-01;SOR=0.540	GT:AD:DP:GQ:PL	0/1:26,14:48:99:718,0,1819
     * 1	11944422	rs112065997	TACACAC	T	1256.73	.	AC=2;AF=1.00;AN=2;DB;DP=38;ExcessHet=3.0103;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;QD=29.49;SOR=4.615	GT:AD:DP:GQ:PL	1/1:0,28:31:93:1293,93,0
     *
     */

    private final Coordinate coordinate;
    private final SampleInfo sampleInfo;
    private final Info info;
    private VcfHeader vcfHeader;
    private String ref;
    private Object alt;
    private String filter;
    private Double qual;
    private Object id;

    /**
     * Alt may be a String separated by ,
     *
     * @param chrom
     * @param position
     * @param ref
     * @param alt      a String
     */
    public Variant(String chrom, int position, String ref, String alt) {
        this.coordinate = new Coordinate(chrom, position);
        this.ref = StringStore.getInstance(ref);
        this.alt = ValueUtils.getValue(alt, "string");
        sampleInfo = new SampleInfo(this);
        info = new Info();
    }

    public Variant(String chrom, int position, String ref, String alt, VcfHeader header) {
        this.coordinate = new Coordinate(chrom, position);
        this.ref = StringStore.getInstance(ref);
        this.alt = ValueUtils.getValue(alt, "string");
        this.vcfHeader = header;
        sampleInfo = new SampleInfo(this);
        info = new Info();
    }

    /**
     * Gets the chromosome of the vcf.
     *
     * @return the chromosome of the vcf
     */
    public String getChrom() {
        return coordinate.getChrom();
    }

    /**
     * Gets the ID of the vcf.
     *
     * @return the ID of the vcf
     */
    public String getId() {
        return ValueUtils.getString(id);
    }

    public void setId(String id) {
        this.id = ValueUtils.getValue(id, "text");
    }

    public String[] getIdArray() {
        return ValueUtils.isArray(id) ? (String[]) id : new String[]{(String) id};
    }

    /**
     * Gets the REF value of the vcf.
     *
     * @return the ref value
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets the ALT value of the vcf.
     *
     * @return the alt value
     */
    public String getAlt() {
        return ValueUtils.getString(alt);
    }

    /**
     * Gets the alt field as an array of Strings.
     *
     * @return alt as <code>"A"</code> or <code>String[]{"A", "AC"}</code>
     */
    public Object[] getAltArray() {
        return ValueUtils.isArray(alt) ? (Object[]) alt : new String[]{(String) alt};
    }

    /**
     * Gets the position of the vcf.
     *
     * @return the position
     */
    public int getPosition() {
        return coordinate.getPosition();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * Gets the QUAL of the vcf.
     *
     * @return the quality
     */
    public Double getQual() {
        return qual;
    }

    public void setQual(Double qual) {
        this.qual = qual;
    }

    @Override
    public int compareTo(Variant variant) {
        return coordinate.compareTo(variant.coordinate);
    }

    /**
     * Changes the contig. Be sure you explicitly reorder the variants in your
     * dataset after changing the name of the contigs.
     * @param chrom
     */
    public void setChrom(String chrom) {
        coordinate.setContig(chrom);
    }
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = StringStore.getInstance(filter);
    }

    public VcfHeader getVcfHeader() {
        return vcfHeader;
    }

    public void setVcfHeader(VcfHeader vcfHeader) {
        this.vcfHeader = vcfHeader;
    }

    @Override
    public String toString() {
        return coordinate.getChrom() +
                "\t" + coordinate.getPosition() +
                "\t" + ValueUtils.getString(id) +
                "\t" + ref +
                "\t" + ValueUtils.getString(alt) +
                "\t" + ValueUtils.getString(qual) +
                "\t" + ValueUtils.getString(filter) +
                "\t" + info +
                sampleInfo;
    }

    public SampleInfo getSampleInfo() {
        return sampleInfo;
    }

    public Info getInfo() {
        return info;
    }

}
