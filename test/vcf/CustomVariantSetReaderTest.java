package vcf;

import org.junit.jupiter.api.Disabled;
import vcf.io.CustomVariantSetReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by uichuimi on 3/10/16.
 */
public class CustomVariantSetReaderTest {

	@Disabled
	public void test() {
		final File file = new File("test/files/SP030.vcf");
		try (CustomVariantSetReader reader = new CustomVariantSetReader(file)) {
			reader.addInfo("DP");
			reader.addSample("SP030");
			reader.addFormat("GT");
			reader.addFormat("DP");
			reader.setloadId(true);
			final VcfHeader header = reader.header();
			System.out.println(header);
			reader.variants().forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}