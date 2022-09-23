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


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * A <code>SharedClass</code> object stores the encrypted and decrypted referenced
 * class and method names that would be called in apps that use the AdMob module.
 * <p>
 * Instances of this class can be retrieved by calling
 * {@link ShadowJar#addSharedClass(String, String)} with the encrypted class and
 * method names.
 *
 * @author MatrixEditor
 * @since 1.0
 */
public final class SharedClass implements Runnable {

    /**
     * The {@link ShadowJar} reference.
     */
    private final ShadowJar jar;

    /**
     * The encrypted class name.
     *
     * @see #sharedClass
     */
    private final String encryptedClassName;

    /**
     * The encrypted method name.
     *
     * @see #sharedMethod
     */
    private final String encryptedMethodName;

    /**
     * The shared class name.
     * <p>
     * Usually, this field expands to <code>com.google.android.ads.zxxz</code> with
     * the dedicated class name at the end.
     */
    private String sharedClass;

    /**
     * The shared method name.
     * <p>
     * Usually, this field expands to <code>a</code> as the name of the method.
     */
    private String sharedMethod;

    SharedClass(ShadowJar jar, String encryptedClassName, String encryptedMethodName) {
        this.jar = jar;
        this.encryptedClassName = encryptedClassName;
        this.encryptedMethodName = encryptedMethodName;
    }

    private String decryptName(SecretKey secretKey, String name) throws GeneralSecurityException {
        Cipher cipher = getJar().getCipherInstance();
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] result = cipher.doFinal(name.getBytes());
        return new String(result, StandardCharsets.UTF_8);
    }

    @Override
    public void run() {
        try {
            sharedClass = decryptName(getJar().getDecodedSecret(), encryptedClassName);
            sharedMethod = decryptName(getJar().getDecodedSecret(), encryptedMethodName);
        } catch (GeneralSecurityException e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public String toString() {
        return String.format("sClass{cls='%s' > '%s', mth='%s' > '%s'}",
                encryptedClassName, sharedClass, encryptedMethodName, sharedMethod);
    }

    public String getSharedClass() {
        return sharedClass;
    }

    public String getSharedMethod() {
        return sharedMethod;
    }

    public String getEncryptedClassName() {
        return encryptedClassName;
    }

    public String getEncryptedMethodName() {
        return encryptedMethodName;
    }

    public ShadowJar getJar() {
        return jar;
    }
}
