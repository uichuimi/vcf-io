package vcf;

import java.io.*;

/**
 * writes VCF format to an output stream. As header must be the first thing to write, a valid header must be provided
 * via the <strong>setHeader()</strong> method. If a call is made to <strong>write(Variant variant)</strong>, but no
 * call has been made to writeHeader(), then it is implicit call.
 * Created by uichuimi on 4/10/16.
 */
public class VariantSetWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private VariantSet variantSet;
    private boolean headerWritten;


    /**
     * Creates a new VariantSetWriter that writes into a File. Once created, a FileWriter is hold to file, so try to
     * close this as soon as you finish using it.
     *
     * @param file file to write. It will be overwritten. If it does not exists, it will be created. See {@link FileWriter}
     * @throws IOException copied from {@link FileWriter}: if the file exists but is a directory rather than a regular
     *                     file, does not exist but cannot be created, or cannot be opened for any other reason
     */
    public VariantSetWriter(File file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    /**
     * Creates a new VariantSetWriter that writes into an output stream. An OutputStreamWriter is opened, so remember
     * to close it when you finish using it or use a try-with-resources block.
     *
     * @param outputStream where to write
     */
    public VariantSetWriter(OutputStream outputStream) {
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void setHeader(VcfHeader header) {
        variantSet = new VariantSet(header);
    }

    /**
     * Writes header into writer. Only first call to this method is effective.
     *
     * @throws IOException copied from {@link BufferedWriter} write method: if an I/O error occurs
     */
    public void writeHeader() throws IOException {
        if (!headerWritten) {
            writer.write(variantSet.getHeader().toString());
            writer.newLine();
            headerWritten = true;
        }
    }

    /**
     * Writes a variant. If header has not been yet written, then a call to writeHeader is made. This operation
     * changes the
     *
     * @param variant variant to write
     * @throws IOException copied from {@link BufferedWriter} write method: if an I/O error occurs
     */
    public void write(Variant variant) throws IOException {
        writeHeader();
        final VariantSet vs = variant.getVariantSet();
        // Temporarily change the VariantSet of the variant to correctly print INFO and FORMAT values
        variant.setVariantSet(this.variantSet);
        writer.write(variant.toString());
        writer.newLine();
        variant.setVariantSet(vs);
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
