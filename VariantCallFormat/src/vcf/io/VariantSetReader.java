package vcf.io;

import vcf.*;

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


    private final VcfHeader header;
    private final BufferedReader reader;
    private Variant nextVariant;

    public VariantSetReader(File file) throws FileNotFoundException {
        header = VariantSetFactory.readHeader(file);
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
        return header;
    }

    @Override
    public boolean hasNext() {
        if (nextVariant != null) return true;
        else {
            try {
                String line = reader.readLine();
                while (line != null && line.startsWith("#")) line = reader.readLine();
                if (line == null) return false;
                nextVariant = VariantFactory.createVariant(line, header);
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
