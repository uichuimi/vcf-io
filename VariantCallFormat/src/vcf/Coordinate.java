package vcf;

import utils.OS;

/**
 * Created by uichuimi on 4/10/16.
 */
public class Coordinate implements Comparable<Coordinate> {


    private final int chromIndex;
    private final String chrom;
    private final int position;

    public Coordinate(String chrom, int position) {
        this.chrom = chrom;
        this.position = position;
        this.chromIndex = getChromIndex(chrom);
    }

    private int getChromIndex(String chrom) {
        return OS.getStandardChromosomes().indexOf(chrom);
    }

    @Override
    public int compareTo(Coordinate other) {
        // Variants with no standard chromosome goes to the end
        if (chromIndex != -1 && other.chromIndex == -1) return -1;
        if (chromIndex == -1 && other.chromIndex != -1) return 1;
        // Non-standard chromosomes are ordered alphabetically
        int compare = (chromIndex == -1)
                ? chrom.compareTo(other.chrom)
                : Integer.compare(chromIndex, other.chromIndex);
        if (compare != 0) return compare;
        return Integer.compare(position, other.position);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == Coordinate.class && (obj == this || compareTo((Coordinate) obj) == 0);
    }

    public String getChrom() {
        return chrom;
    }

    public int getPosition() {
        return position;
    }
}
