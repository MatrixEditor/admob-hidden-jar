package io.github.matrixeditor.shjartest;

import io.github.matrixeditor.shjar.ShadowJar;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ShadowJarTest {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        try (ShadowJar jar = ShadowJar.open(ShadowJar.DEFAULT_SECRET_KEY, new File("shadow.txt"))) {
            jar.writeSelf("output.jar");
        }
    }
}
