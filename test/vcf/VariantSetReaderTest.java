package vcf;

import org.junit.Test;
import vcf.io.VariantSetReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by uichuimi on 3/10/16.
 */
public class VariantSetReaderTest {

    @Test
    public void test() {
        final File file = new File("test/files/MultiSample.vcf");
        try (VariantSetReader reader = new VariantSetReader(file)) {
            final VcfHeader header = reader.header();
            System.out.println(header);
            reader.variants().forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}