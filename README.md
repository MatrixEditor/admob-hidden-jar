# AdMob-Hidden-Jar (Android)

When using Google's AdMob for android library, there is sometimes a hidden JAR-File added to the app. the basic loading
process is the following:

    1. Decrypt JAR (stored as an AES-encrypted String)
    2. Save JAR in /cache or dex /directory in the private app directory
    3. Extract JAR and load the 'classes.dex' file that is stored inside
    4. Create wrapper classes that delegate the Execution of every method

This repository provides this encrypted JAR file [here](shadow.txt) and the decrypted version in the cache directory
[here](cache/1489418796403.jar). The JAR-file name is hardcoded in the source code that was inspected:

> Name: 1489418796403.jar

You can validate if your classes.dex file stores this hidden jar file by executing the following command:

```sh
./validateDEX.sh <DEX-filename>
# or on Windows
./validateDEX.bat <DEX-filename>
```

This small project can be used in your app if you don't want that any other library should load the hidden JAR-File. 
Usage:
```java
import io.github.matrixeditor.shjar.ShadowJar;
 
Shadowjar.ensureNoHiddenJar(context, () -> Toast.makeText(context, "JAR is loaded"));
```

## Usage of `ShadowJar`

In order to decrypt (and encrypt) the jar file stored in the source code, the `ShadowJar` class provides some utilities
around that. Basic usage:

```java
// Read, decrypt and save a file.
try(ShadowJar jar = ShadowJar.open(key, new File("encrypted.txt"))) { 
    jar.writeSelf("decrypted.jar");
}

// Add shared classes
ShadowJar jar=ShadowJar.getInstance();
SharedClass cls=jar.addSharedClass("...","...");
```

Further usage is provided in the
test-classes [here](src/main/test/io/github/matrixeditor/shjartest/SharedClassTest.java)
ans [here](src/main/test/io/github/matrixeditor/shjartest/ShadowJarTest.java).

## License

    MIT License
    
    Copyright (c) 2022 Matrixeditor
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
