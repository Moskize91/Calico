package com.taozeyu.calico.resource;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by taozeyu on 16/8/2.
 */
class ReaderWithOneChar extends Reader {

    private final Reader reader;
    private final char firstChar;
    private boolean hasReadFirstChar;

    ReaderWithOneChar(Reader reader, char firstChar) {
        this.reader = reader;
        this.firstChar = firstChar;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int result = 0;
        if (hasReadFirstChar) {
            result = reader.read(cbuf, off, len);
        } else if (len > 0) {
            cbuf[off] = firstChar;
            if (len > 1) {
                result = reader.read(cbuf, off + 1, len - 1);
                if (result == -1) {
                    result = 1;
                } else {
                    result += 1;
                }
            } else {
                result = 1;
            }
            hasReadFirstChar = true;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
