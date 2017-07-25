package vcf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by uichuimi on 4/10/16.
 */
public class Coordinate implements Comparable<Coordinate> {

    private final static List<String> CHROMOSOMES = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6",
            "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"));

    private int chromIndex;
    private final int position;

    public Coordinate(String chrom, int position) {
        this.position = position;
        this.chromIndex = getChromIndex(chrom);
    }

    private int getChromIndex(String chrom) {
        if (!CHROMOSOMES.contains(chrom)) CHROMOSOMES.add(chrom);
        return CHROMOSOMES.indexOf(chrom);
    }

    @Override
    public int compareTo(Coordinate other) {
        final int compare = Integer.compare(chromIndex, other.chromIndex);
        return compare == 0 ? Integer.compare(position, other.position) : compare;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == Coordinate.class && (obj == this || compareTo((Coordinate) obj) == 0);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(chromIndex) + Integer.hashCode(position);
    }

    public String getChrom() {
        return CHROMOSOMES.get(chromIndex);
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return getChrom() + ":" + position;
    }

    public void setContig(String contig) {
        this.chromIndex = getChromIndex(contig);
    }
}
