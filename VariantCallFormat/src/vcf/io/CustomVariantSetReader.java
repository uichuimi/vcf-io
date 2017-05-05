package vcf.io;

import vcf.*;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * By defaults, variants will only load chromosome, reference and alternative
 * Created by uichuimi on 20/12/16.
 */
public class CustomVariantSetReader implements AutoCloseable, Iterator<Variant> {

    private final VcfHeader memoryHeader;
    private final VcfHeader readHeader;
    private final BufferedReader reader;
    private Variant nextVariant;
    private boolean loadId;
    private boolean loadQual;
    private boolean loadFilter;

    public CustomVariantSetReader(File file) throws FileNotFoundException {
        readHeader = (VariantSetFactory.readHeader(file));
        memoryHeader = VariantSetFactory.readHeader(file);
        memoryHeader.getSamples().clear();
        memoryHeader.getComplexHeaders().put("INFO", new LinkedList<>());
        memoryHeader.getComplexHeaders().put("FORMAT", new LinkedList<>());
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
        return memoryHeader;
    }

    @Override
    public boolean hasNext() {
        if (nextVariant != null) return true;
        else {
            try {
                String line = reader.readLine();
                while (line != null && line.startsWith("#")) line = reader.readLine();
                if (line == null) return false;
                nextVariant = VariantFactory.createVariant(line, memoryHeader, readHeader, loadId, loadQual, loadFilter);
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

    public void setloadId(boolean loadId) {
        this.loadId = loadId;
    }

    public void setLoadQual(boolean loadQual) {
        this.loadQual = loadQual;
    }

    public void setLoadFilter(boolean loadFilter) {
        this.loadFilter = loadFilter;
    }

    public void addSample(String sample) {
        if (readHeader.getSamples().contains(sample) && !memoryHeader.getSamples().contains(sample))
            memoryHeader.getSamples().add(sample);
    }

    public void removeSample(String sample) {
        memoryHeader.getSamples().remove(sample);
    }

    public void addFormat(String format) {
        addComplexHeader("FORMAT", format);
    }

    public void removeFormat(String format) {
        removeComplexHeader("FORMAT", format);
    }

    public void addInfo(String info) {
        addComplexHeader("INFO", info);
    }

    public void removeInfo(String info) {
        removeComplexHeader("INFO", info);
    }

    private void addComplexHeader(String type, String key) {
        if (readHeader.hasComplexHeader(type, key))
            memoryHeader.addComplexHeader(type, readHeader.getComplexHeader(type, key));
    }

    private void removeComplexHeader(String type, String key) {
        memoryHeader.getComplexHeaders().get(type).remove(readHeader.getComplexHeader(type, key));
    }
}
