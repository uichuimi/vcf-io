package vcf.io;

import vcf.Variant;
import vcf.VariantException;
import vcf.VcfHeader;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * By default, variants will only load chromosome, reference and alternative
 * Created by uichuimi on 20/12/16.
 */
public class CustomVariantSetReader implements AutoCloseable, Iterator<Variant> {

    private final VcfHeader memoryHeader;
    private final VcfHeader fileHeader;
    private final BufferedReader reader;
    private Variant nextVariant;
    private boolean loadId;
    private boolean loadQual;
    private boolean loadFilter;

    /**
     * Creates a VariantSet reader that will only read chrom, pos, id, ref,
     * alt, qual and filter by default. No INFO, no FORMAT is loaded by default.
     *
     * @param file
     * @throws FileNotFoundException
     */
    public CustomVariantSetReader(File file) throws FileNotFoundException {
        fileHeader = VariantSetFactory.readHeader(file);
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

    /**
     * Generated VcfHeader with personalized load options
     *
     * @return
     */
    public VcfHeader header() {
        return memoryHeader;
    }

    /**
     * Returns the header read from file, which contains all possible INFO,
     * FORMAT and sample values
     *
     * @return the read file header
     */
    public VcfHeader fileHeader() {
        return fileHeader;
    }

    @Override
    public boolean hasNext() {
        if (nextVariant != null) return true;
        else {
            try {
                String line = reader.readLine();
                while (line != null && line.startsWith("#"))
                    line = reader.readLine();
                if (line == null) return false;
                nextVariant = VariantFactory.createVariant(line, memoryHeader, fileHeader, loadId, loadQual, loadFilter);
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

    /**
     * Sets whether to load ID field or not.
     *
     * @param loadId if true, ID will be read. If false ID will be always null.
     *               By default is true.
     */
    public void setloadId(boolean loadId) {
        this.loadId = loadId;
    }

    /**
     * Sets whether to load QUAL field or not.
     *
     * @param loadQual if true, QUAL will be read. If false QUAL will be always
     *                 null. By default is true.
     */
    public void setLoadQual(boolean loadQual) {
        this.loadQual = loadQual;
    }

    /**
     * Sets whether to load FILTER field or not.
     *
     * @param loadFilter if true, FILTER will be read. If false FILTER will be
     *                   always null. By default is true.
     */
    public void setLoadFilter(boolean loadFilter) {
        this.loadFilter = loadFilter;
    }

    /**
     * Add a sample to load its genotype
     *
     * @param sample a sample you want to load its genotype.
     */
    public void addSample(String sample) {
        if (fileHeader.getSamples().contains(sample) && !memoryHeader.getSamples().contains(sample))
            memoryHeader.getSamples().add(sample);
    }

    /**
     * Removes a sample so its genotype will not be loaded.
     *
     * @param sample the name of the sample to not be loaded
     */
    public void removeSample(String sample) {
        memoryHeader.getSamples().remove(sample);
    }

    /**
     * Adds a FORMAT (a genotype info) to be loaded.
     *
     * @param format the name of the format to be loaded
     */
    public void addFormat(String format) {
        addComplexHeader("FORMAT", format);
    }


    /**
     * Removes a FORMAT (a genotype info) so it will not be loaded.
     *
     * @param format the name of the format to not be loaded
     */
    public void removeFormat(String format) {
        removeComplexHeader("FORMAT", format);
    }

    /**
     * Adds a INFO to be loaded.
     *
     * @param info the name of the info to be loaded
     */
    public void addInfo(String info) {
        addComplexHeader("INFO", info);
    }

    /**
     * Removes a INFO so it will not be loaded.
     *
     * @param info the name of the info to not be loaded
     */
    public void removeInfo(String info) {
        removeComplexHeader("INFO", info);
    }

    /**
     * Copies a complex header from disk file to memory VcfHeader
     *
     * @param type
     * @param key
     */
    private void addComplexHeader(String type, String key) {
        if (fileHeader.hasComplexHeader(type, key))
            memoryHeader.addComplexHeader(type, fileHeader.getComplexHeader(type, key));
    }

    /**
     * Removes a complex header from memory VcfHeader
     *
     * @param type
     * @param key
     */
    private void removeComplexHeader(String type, String key) {
        memoryHeader.getComplexHeaders().get(type).remove(fileHeader.getComplexHeader(type, key));
    }
}
