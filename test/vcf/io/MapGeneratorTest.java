package vcf.io;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapGeneratorTest {

	@Test
	void testParse() {

		// ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
		final LinkedHashMap<String, String> gt = new LinkedHashMap<>();
		gt.put("ID", "GT");
		gt.put("Number", "1");
		gt.put("Type", "String");
		gt.put("Description", "Genotype");
		final LinkedHashMap<String, String> parsed = MapGenerator.parse("ID=GT,Number=1,Type=String,Description=\"Genotype\"");
		assertEqualLinkedHashMap(gt, parsed);

		// ##contig=<ID=1,length=249250621,assembly=b37>
		final LinkedHashMap<String, String> contig = new LinkedHashMap<>();
		contig.put("ID", "1");
		contig.put("length", "249250621");
		contig.put("assembly", "b37");
		final LinkedHashMap<String, String> parsed2 = MapGenerator.parse("ID=1,length=249250621,assembly=b37");
		assertEqualLinkedHashMap(contig, parsed2);
	}

	private void assertEqualLinkedHashMap(LinkedHashMap<String, String> expected,
	                                      LinkedHashMap<String, String> map) {
		// Assert size of maps
		assertEquals(expected.size(), map.size(), "Different map sizes");

		// Assert order of keys
		final List<String> expectedKeys = new LinkedList<>(expected.keySet());
		final List<String> mapKeys = new LinkedList<>(map.keySet());
		for (int i = 0; i < expected.size(); i++)
			assertEquals(expectedKeys.get(i), mapKeys.get(i));

		// Assert equal values
		expected.forEach((key, value) -> assertEquals(value, map.get(key)));
	}

}