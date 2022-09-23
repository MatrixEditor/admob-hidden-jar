/*
 * Copyright (c) 2022 MatrixEditor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.matrixeditor.shjar;

import android.content.Context;
import android.os.FileObserver;
import io.github.matrixeditor.shjar.crypto.ZxxZCipher;
import io.github.matrixeditor.shjar.crypto.ZxxZCryptoProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * A {@link ShadowJar} object provides different utilities when working with
 * Google's AdMob ZXXZ library.
 * <p>
 * This class can be created in two ways. The first one includes the content
 * reading and decrypting of the encrypted JAR file. And secondly, create
 * an empty {@link ShadowJar} object with a provided encoded {@link SecretKey}.
 * <p>
 * Note that this class implement the {@link Closeable} interface, so it can be
 * used as follows:
 * <pre>{@code
 * try (ShadowJar jar = ShadowJar.open(secretKey, "encryptedJarfile.txt") {
 *     jar.writeSelf("output.jar");
 * }
 * }</pre>
 * <p>
 * Future versions of this class may be packed into new classes that are
 * named like <code>ShadowJarV2</code> and so on. Future releases of this
 * module will implement a mechanism that searches for the encrypted JAR
 * string in the linked source code.
 *
 * @author MatrixEditor
 * @since 1.0
 */
public class ShadowJar implements Closeable {

    /**
     * The default AES secret key for decrypting the JAR file.
     * <p>
     * The key is Base64 encoded and XOR'ed with the number <code>68</code>.
     */
    public static final String DEFAULT_SECRET_KEY = "QZZCt2ftWILMiOv/bx0NwH1VFPjOT+QCiqkEm96fZOY=";

    /**
     * The source code specifies a temp file name which is by default
     * <code>1489418796403</code>. The file is stored either in a <code>cache</code>
     * or <code>dex</code> directory.
     */
    public static final String FILENAME = "1489418796403";
    /**
     * A simple wrapper for storing all created {@link SharedClass} instances.
     */
    private final Map<Entry<String, String>, SharedClass> sharedClassCache =
            new HashMap<>();
    /**
     * A variable indicating whether this object is closed;
     */
    private volatile transient boolean closed;

    /**
     * An instance of the {@link ZxxZCipher} which provides encryption
     * and decryption.
     */
    private volatile Cipher zxxz;

    /**
     * The {@link SecretKeySpec} for the <code>AES</code> algorithm storing
     * the decoded secret.
     */
    private volatile SecretKey secretKey;

    /**
     * A simple buffer storing the decrypted jar file.
     */
    private byte[] decryptedContent;

    // prevent instance creation from outside
    private ShadowJar() {
    }

    /**
     * Creates an empty {@link ShadowJar} object storing the default secret key
     * and a {@link ZxxZCipher} instance.
     *
     * @return a new empty {@link ShadowJar} instance
     * @throws GeneralSecurityException if the {@link ZxxZCipher} could not be initialized
     * @see #DEFAULT_SECRET_KEY
     */
    public static ShadowJar getInstance() throws GeneralSecurityException {
        return getInstance(DEFAULT_SECRET_KEY);
    }

    /**
     * Creates an empty {@link ShadowJar} object storing the provided secret key
     * and a {@link ZxxZCipher} instance.
     *
     * @param secretKey the encoded secret key which should be used.
     * @return a new empty {@link ShadowJar} instance
     * @throws GeneralSecurityException if the {@link ZxxZCipher} could not be initialized
     */
    public static ShadowJar getInstance(String secretKey) throws GeneralSecurityException {
        ShadowJar jar = new ShadowJar();
        jar.secretKey = new SecretKeySpec(ZxxZCipher.decodeXOR(secretKey), "AES");
        jar.zxxz = Cipher.getInstance("AES", new ZxxZCryptoProvider());
        return jar;
    }

    /**
     * Creates a new {@link ShadowJar} object and reads the content from the
     * given {@link File}.
     *
     * @param key the encoded secret key which should be used
     * @param src the file to read
     * @return a new {@link ShadowJar} object and reads the content from the
     * given {@link File}.
     * @throws GeneralSecurityException if the {@link ZxxZCipher} could not be initialized
     * @throws IOException              if an error while reading occurs
     */
    public static ShadowJar open(String key, File src) throws GeneralSecurityException, IOException {
        ShadowJar jar = getInstance(key);
        jar.read(src);
        return jar;
    }

    /**
     * Creates a new {@link ShadowJar} object and reads the content from the
     * given {@link String}.
     *
     * @param key     the encoded secret key which should be used
     * @param content the encrypted jar file as a {@link String}
     * @return a new {@link ShadowJar} object and reads the content from the
     * given {@link String}.
     * @throws GeneralSecurityException if the {@link ZxxZCipher} could not be initialized
     */
    public static ShadowJar open(String key, String content) throws GeneralSecurityException {
        ShadowJar jar = getInstance(key);
        jar.read(content);
        return jar;
    }

    /**
     * This method validates that no hidden JAR file is loaded at runtime.
     * <p>
     * Actually, this method registers an observer to the internal app cache
     * directory where the hidden JAR-file would be saved. The {@link Runnable}
     * is executed if a file with the dedicated {@link #FILENAME} was created.
     *
     * @param context the app's context
     * @param action the action to take if the file was created.
     * @return a new {@link FileObserver} object
     */
    public static FileObserver ensureNoHiddenJar(Context context, Runnable action) {
        FileObserver observer = new FileObserver(context.getCacheDir(), FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int i, String s) {
                if (i == FileObserver.CREATE) {
                    if (s.contains(FILENAME)) {
                        action.run();
                    }
                }
            }
        };
        observer.startWatching();
        return observer;
    }

    /**
     * Tries to read all bytes and to decrypt them from the given {@link String}.
     *
     * @param content the encrypted content
     * @throws GeneralSecurityException if an error during encryption occurs
     */
    public synchronized void read(String content) throws GeneralSecurityException {
        ensureOpen();
        zxxz.init(Cipher.DECRYPT_MODE, secretKey);
        decryptedContent = zxxz.doFinal(content.getBytes());
    }

    /**
     * Tries to read all bytes and to decrypt them from the given {@link File}.
     *
     * @param file a file storing the encrypted content
     * @throws GeneralSecurityException if an error during encryption occurs
     * @throws IOException              if an I/O error occurs
     */
    public synchronized void read(File file) throws GeneralSecurityException, IOException {
        ensureOpen();
        if (!file.exists()) {
            throw new NullPointerException("File does not exists");
        }
        zxxz.init(Cipher.DECRYPT_MODE, secretKey);
        try (FileInputStream fis = new FileInputStream(file)) {
            decryptedContent = zxxz.doFinal(fis.readAllBytes());
        }
    }

    public void writeSelf(String destination) throws IOException {
        ensureOpen();
        if (decryptedContent == null || decryptedContent.length == 0) {
            throw new IllegalStateException("No content imported yet");
        }

        File file = new File(destination);
        if (file.exists()) {
            throw new IOException("Destination file already exists");
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decryptedContent, 0, decryptedContent.length);
        }
    }

    /**
     * Closes this {@link ShadowJar} and releases any system resources
     * associated with it. If the object is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

        Arrays.fill(decryptedContent, (byte) 0);
        decryptedContent = null;
        closed = true;
    }

    /**
     * Returns the {@link SecretKey} for the <code>AES</code> algorithm.
     *
     * @return the decrypted secret key
     */
    public SecretKey getDecodedSecret() {
        return secretKey;
    }

    /**
     * Creates a new {@link SharedClass} instance is not present from the given
     * encrypted class and method names.
     *
     * @param clsName the encrypted class name
     * @param mthName the encrypted method name
     * @return the {@link SharedClass} that represents the provided class
     * @apiNote This method will not return <code>null</code> values.
     */
    public SharedClass addSharedClass(String clsName, String mthName) {
        Entry<String, String> e = new AbstractMap.SimpleEntry<>(clsName, mthName);
        if (sharedClassCache.containsKey(e)) {
            return sharedClassCache.get(e);
        }

        SharedClass sharedClass = new SharedClass(this, clsName, mthName);
        sharedClassCache.put(e, sharedClass);
        sharedClass.run();
        return sharedClass;
    }

    /**
     * Returns all added {@link SharedClass} objects as an array.
     *
     * @return all added {@link SharedClass} objects as an array.
     */
    public SharedClass[] getSharedClasses() {
        if (sharedClassCache.isEmpty()) {
            return new SharedClass[0];
        }
        return sharedClassCache.values().toArray(SharedClass[]::new);
    }

    /**
     * Retrieves a Stream with all the currently added {@link SharedClass}es.
     *
     * @return the stream of {@link SharedClass}es added by the user
     * @since 9
     */
    public Stream<SharedClass> sharedClasses() {
        return Arrays.stream(getSharedClasses());
    }

    /**
     * Returns the {@link ZxxZCipher} instance.
     *
     * @return the {@link ZxxZCipher} instance.
     */
    public Cipher getCipherInstance() {
        return zxxz;
    }

    private void ensureOpen() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException("ShadowJar has been closed already");
        }
    }
}
