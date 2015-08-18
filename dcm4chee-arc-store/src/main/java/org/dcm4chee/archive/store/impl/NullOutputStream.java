package org.dcm4chee.archive.store.impl;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by umberto on 8/13/15.
 */
public class NullOutputStream extends OutputStream {

    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    @Override
    public void write(byte[] b, int off, int len) {
    }

    @Override
    public void write(int b) {
    }

    @Override
    public void write(byte[] b) throws IOException {
    }
}
