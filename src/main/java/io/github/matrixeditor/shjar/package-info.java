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

/**
 * <h2>ShJar - ShadowJar Module</h2>
 *
 * This package defines basic services that can be used when working
 * with the {@link io.github.matrixeditor.shjar.ShadowJar} class.
 * <p>
 * For a detailed description of how the <code>ShadowJar</code> class
 * can be/ should be used, please refer to the documentation given on
 * that class. {@link io.github.matrixeditor.shjar.SharedClass} objects
 * can be retrieved by executing <code>addSharedClass</code> on a
 * <code>ShadowJar</code> object.
 * <p>
 * To check if your project contains the encrypted JAR file, you can use
 * the validation scripts provided in the <a
 * href="https://github.com/MatrixEditor/admob-hidden-jar">repository</a>
 * on GitHub.
 * <p>
 * If you want to include this project in your android app and want to
 * validate that no hidden JAR file is loaded, just call
 * <code>ShadowJar.ensureNoHiddenJar()</code> that will throw and exception
 * on failure.
 *
 * @author MatrixEditor
 */
package io.github.matrixeditor.shjar;