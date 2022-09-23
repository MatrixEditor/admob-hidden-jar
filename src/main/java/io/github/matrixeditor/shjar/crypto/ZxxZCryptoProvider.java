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

package io.github.matrixeditor.shjar.crypto;

import javax.crypto.Cipher;
import java.security.Provider;
import java.util.ServiceLoader;

/**
 * A basic java security {@link Provider} that contains only one implemented
 * {@link Cipher}.
 * <p>
 * The <code>Cipher</code> is located in the same package as this provider
 * is stored: {@link ZxxZCipher}. This provider can be used in two different
 * ways.
 * <p>
 * <b>1:</b> Register the provider by creating a file name
 * <code>java.security.Provider</code> in the <code>META-INF/services</code>
 * folder. By doing that, the {@link ServiceLoader} automatically detects the
 * provider and adds them to the pre-defined ones. You can get a {@link Cipher}
 * instance by calling:
 * <pre>{@code
 * Cipher cipher = Cipher.getInstance("AES", "ZxxZ");
 * }</pre>
 * <p>
 * <b>2:</b> Create an instance of this {@link Provider} implementation and
 * transfer it to the {@link Cipher#getInstance(String, Provider)} method:
 * <pre>{@code
 * Provider provider = new ZxxZCryptoProvider();
 * Cipher cipher = Cipher.getInstance("AES", provider);
 * }</pre>
 *
 * @author MatrixEditor
 */
public class ZxxZCryptoProvider extends Provider {

    /**
     * Create a new {@link Provider} instance with adding the {@link ZxxZCipher}
     * to the implemented features.
     *
     * @since 1.0
     */
    public ZxxZCryptoProvider() {
        super("ZxxZ", "1.0.0", "ZxxZ hidden JAR Crypto Module");
        put("Cipher.AES", "io.github.matrixeditor.shjar.crypto.ZxxZCipher");
    }
}
