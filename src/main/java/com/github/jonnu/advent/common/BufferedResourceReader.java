package com.github.jonnu.advent.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

public class BufferedResourceReader implements ResourceReader {

    @Override
    public BufferedReader read(final String resource) {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return Optional.ofNullable(classloader.getResourceAsStream(resource))
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .orElseThrow(() -> new RuntimeException("Unable to stream " + resource + "; Did you typo?"));
    }
}
