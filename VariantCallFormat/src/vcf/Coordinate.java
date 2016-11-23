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
        int compare = (chromIndex == -1)
                ? chrom.compareTo(other.chrom) // Non-standard -> alphabetic order
                : Integer.compare(chromIndex, other.chromIndex); // Standard
        return compare == 0 ? Integer.compare(position, other.position) : compare;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == Coordinate.class && (obj == this || compareTo((Coordinate) obj) == 0);
    }

    @Override
    public int hashCode() {
        return chrom.hashCode() + Integer.hashCode(position);
    }

    public String getChrom() {
        return chrom;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return chrom + ":" + position;
    }
}
