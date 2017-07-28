package vcf;

import org.junit.jupiter.api.Test;
import vcf.io.VariantSetReader;
import vcf.io.VariantSetWriter;

import java.io.File;
import java.io.IOException;

/**
 * Created by uichuimi on 4/10/16.
 */
public class VariantSetWriterTest {

    @Test
    public void test() {
        final File output = new File("test/writeSample.vcf");
        final File input = new File("test/files/ExpectedSample1.vcf");
        try (VariantSetWriter writer = new VariantSetWriter(output);
             VariantSetReader reader = new VariantSetReader(input)) {
            writer.setHeader(reader.header());
            reader.variants().forEach((variant) -> {
                try {
                    writer.write(variant);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}