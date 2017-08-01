package vcf;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by uichuimi on 4/10/16.
 */
public class CoordinateTest {


	private final static List<Coordinate> SORTED = Arrays.asList(
			new Coordinate("1", 1456),
			new Coordinate("1", 1457),
			new Coordinate("2", 1111),
			new Coordinate("2", 2121),
			new Coordinate("10", 123456),
			new Coordinate("11", 432),
			new Coordinate("22", 321),
			new Coordinate("X", 11111),
			new Coordinate("X", 11112),
			new Coordinate("Y", 123456),
			new Coordinate("MT", 11),
			new Coordinate("GL1", 3455),
			new Coordinate("GL1", 3456),
			new Coordinate("HW", 123));

	@Test
	public void test() {
		for (int i = 0; i < SORTED.size(); i++) {
			for (int j = 0; j < SORTED.size(); j++) {
				final int compareTo = SORTED.get(i).compareTo(SORTED.get(j));
				if (i < j) assertTrue(compareTo < 0);
				if (i > j) assertTrue(compareTo > 0);
				if (i == j) assertTrue(compareTo == 0);
			}
		}
	}

}