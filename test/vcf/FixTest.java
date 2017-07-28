package vcf;

import org.junit.jupiter.api.Disabled;
import vcf.io.VariantSetFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by uichuimi on 29/09/16.
 */
public class FixTest {

    @Disabled
    public void testFix() {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.vcf");
        final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(file)) fixFile(file.toFile());
                return FileVisitResult.CONTINUE;
            }
        };
        final Path root = new File("/media").toPath();
        try {
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fixFile(File file) {
        VariantSet variantSet = VariantSetFactory.createFromFile(file);
    }
}
