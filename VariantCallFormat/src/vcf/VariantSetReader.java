package vcf;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by uichuimi on 3/10/16.
 */
public class VariantSetReader implements AutoCloseable, Iterator<Variant> {


    private final VariantSet container;
    private final BufferedReader reader;
    public Variant nextVariant;

    public VariantSetReader(File file) throws FileNotFoundException {
        container = new VariantSet(VariantSetFactory.readHeader(file));
        reader = new BufferedReader(new FileReader(file));
    }

    public Stream<Variant> variants() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
                Spliterator.NONNULL | Spliterator.ORDERED), true);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public VcfHeader header() {
        return container.getHeader();
    }

    @Override
    public boolean hasNext() {
        if (nextVariant != null) return true;
        else {
            try {
                String line = reader.readLine();
                while (line != null && line.startsWith("#")) line = reader.readLine();
                if (line == null) return false;
                nextVariant = VariantFactory.createVariant(line, container);
                return true;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (VariantException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public Variant next() {
        if (hasNext()) {
            final Variant variant = nextVariant;
            nextVariant = null;
            return variant;
        } else throw new NoSuchElementException();
    }
}
