package maoge.cas;/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.cryptacular.bean.BufferedBlockCipherBean;
import org.cryptacular.bean.CipherBean;
import org.cryptacular.bean.KeyStoreFactoryBean;
import org.cryptacular.generator.sp80038a.RBGNonce;
import org.cryptacular.io.URLResource;
import org.cryptacular.spec.BufferedBlockCipherSpec;
import org.jasig.spring.webflow.plugin.Transcoder;

import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encodes an object by encrypting its serialized byte stream. Details of encryption are handled by an instance of
 * {@link CipherBean}. Default ciphering mode is set to 128-bit AES in CBC mode with compression.
 * <p>
 * Optional gzip compression of the serialized byte stream before encryption is supported and enabled by default.
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 */
public class EncryptedTranscoder implements Transcoder {

    /** Handles encryption/decryption details. */
    private CipherBean cipherBean;

    /** Flag to indicate whether to Gzip compression before encryption. */
    private boolean compression = true;

    public EncryptedTranscoder() throws IOException {
        final BufferedBlockCipherBean bufferedBlockCipherBean = new BufferedBlockCipherBean();
        bufferedBlockCipherBean.setBlockCipherSpec(new BufferedBlockCipherSpec("AES", "CBC", "PKCS7"));
        bufferedBlockCipherBean.setKeyStore(createAndPrepareKeyStore());
        bufferedBlockCipherBean.setKeyAlias("aes128");
        bufferedBlockCipherBean.setKeyPassword("changeit");
        bufferedBlockCipherBean.setNonce(new RBGNonce());

        setCipherBean(bufferedBlockCipherBean);
    }

    public EncryptedTranscoder(final CipherBean cipherBean) throws IOException {
        setCipherBean(cipherBean);
    }

    public void setCompression(final boolean compression) {
        this.compression = compression;
    }

    protected void setCipherBean(final CipherBean cipherBean) {
        this.cipherBean = cipherBean;
    }

    public byte[] encode(final Object o) throws IOException {
        if (o == null) {
            return new byte[0];
        }
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            if (this.compression) {
                out = new ObjectOutputStream(new GZIPOutputStream(outBuffer));
            } else {
                out = new ObjectOutputStream(outBuffer);
            }
            out.writeObject(o);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        try {
            return cipherBean.encrypt(outBuffer.toByteArray());
        } catch (Exception e) {
            throw new IOException("Encryption error", e);
        }
    }

    public Object decode(final byte[] encoded) throws IOException {
        final byte[] data;
        try {
            data = cipherBean.decrypt(encoded);
        } catch (Exception e) {
            throw new IOException("Decryption error", e);
        }
        final ByteArrayInputStream inBuffer = new ByteArrayInputStream(data);
        ObjectInputStream in = null;
        try {
            if (this.compression) {
                in = new ObjectInputStream(new GZIPInputStream(inBuffer));
            } else {
                in = new ObjectInputStream(inBuffer);
            }

            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization error", e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    protected KeyStore createAndPrepareKeyStore() {
        final KeyStoreFactoryBean ksFactory = new KeyStoreFactoryBean();
        final URL u = this.getClass().getResource("/etc/keystore.jceks");

        ksFactory.setResource(new URLResource(u));
        ksFactory.setType("JCEKS");
        ksFactory.setPassword("changeit");

        return ksFactory.newInstance();
    }
}
