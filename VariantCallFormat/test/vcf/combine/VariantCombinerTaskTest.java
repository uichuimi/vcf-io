package vcf.combine;

import de.saxsys.mvvmfx.testingutils.jfxrunner.JfxRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import vcf.VariantSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by uichuimi on 12/07/16.
 */
@RunWith(JfxRunner.class)
public class VariantCombinerTaskTest {

    private final static File SP030_VCF = new File("test/files/SP030.vcf");
    private final static File SP072_VCF = new File("test/files/SP072.vcf");
    private final static File SP077_VCF = new File("test/files/SP077.vcf");
    private final static File SP030_MIST = new File("test/files/SP030.mist");
    private final static File SP072_MIST = new File("test/files/SP072.mist");
    private final static File SP077_MIST = new File("test/files/SP077.mist");

    @Test
    public void testWithJfxPlatform() {
        final List<Sample> samples = new ArrayList<>();
        final Sample sp030 = new Sample(SP030_VCF, "SP030");
        sp030.setMistFile(SP030_MIST);
        samples.add(sp030);
        final Sample sp072 = new Sample(SP072_VCF, "SP072");
        sp030.setMistFile(SP072_MIST);
        samples.add(sp072);
        final Sample sp077 = new Sample(SP077_VCF, "SP077");
        sp030.setMistFile(SP077_MIST);
        samples.add(sp077);
        final VariantCombinerTask task = new VariantCombinerTask(samples, false);
        try {
            task.run();
            final VariantSet variantSet = task.get();
            Assert.assertEquals(94, variantSet.getVariants().size());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
