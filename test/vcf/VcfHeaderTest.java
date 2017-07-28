package vcf;

import org.junit.jupiter.api.Test;
import vcf.io.VariantSetFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class VcfHeaderTest {

    @Test
    void testConstructor() {
        final VcfHeader header = new VcfHeader("VCFv4.2");
        assertEquals("VCFv4.2", header.getSimpleHeader("fileformat").getValue());
    }

    @Test
    void testFromFile() {
        final File file = new File("test/files/Sample2.vcf");
        final VcfHeader header = VariantSetFactory.readHeader(file);
//        assertEquals(5, header.getSimpleHeaders().size());
        assertEquals(18, header.getHeaderLines().size());
        assertEquals(5, header.getSimpleHeaders().size());

    }

}