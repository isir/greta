/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.application.modular.tools.classloader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSigner;
import java.util.Arrays;
import java.util.jar.Manifest;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class Resource {

        public abstract String getName();

        public abstract URL getURL();

        public abstract URL getCodeSourceURL();

        public abstract InputStream getInputStream() throws IOException;

        public abstract int getContentLength() throws IOException;

        private InputStream cis;

        private synchronized InputStream cachedInputStream() throws IOException {
            if (cis == null) {
                cis = getInputStream();
            }
            return cis;
        }

        public byte[] getBytes() throws IOException {
            byte[] b;
            InputStream in = cachedInputStream();
            boolean isInterrupted = Thread.interrupted();
            int len;
            for (;;) {
                try {
                    len = getContentLength();
                    break;
                } catch (InterruptedIOException iioe) {
                    Thread.interrupted();
                    isInterrupted = true;
                }
            }

            try {
                b = new byte[0];
                if (len == -1) len = Integer.MAX_VALUE;
                int pos = 0;
                while (pos < len) {
                    int bytesToRead;
                    if (pos >= b.length) { // Only expand when there's no room
                        bytesToRead = Math.min(len - pos, b.length + 1024);
                        if (b.length < pos + bytesToRead) {
                            b = Arrays.copyOf(b, pos + bytesToRead);
                        }
                    } else {
                        bytesToRead = b.length - pos;
                    }
                    int cc = 0;
                    try {
                        cc = in.read(b, pos, bytesToRead);
                    } catch (InterruptedIOException iioe) {
                        Thread.interrupted();
                        isInterrupted = true;
                    }
                    if (cc < 0) {
                        if (len != Integer.MAX_VALUE) {
                            throw new EOFException("Detect premature EOF");
                        } else {
                            if (b.length != pos) {
                                b = Arrays.copyOf(b, pos);
                            }
                            break;
                        }
                    }
                    pos += cc;
                }
            } finally {
                try {
                    in.close();
                } catch (InterruptedIOException iioe) {
                    isInterrupted = true;
                } catch (IOException ignore) {}

                if (isInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            return b;
        }

        public ByteBuffer getByteBuffer() throws IOException {
            InputStream in = cachedInputStream();

            if (Factory.ByteBuffered.isInstance(in)) {
                try {
                    return (ByteBuffer) Factory.ByteBuffered_getByteBuffer.invoke(in);
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
            return null;
        }

        public Manifest getManifest() throws IOException {
            return null;
        }

        public java.security.cert.Certificate[] getCertificates() {
            return null;
        }

        public CodeSigner[] getCodeSigners() {
            return null;
        }
    }
