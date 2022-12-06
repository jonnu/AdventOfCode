package com.github.jonnu.advent.inject;

import com.github.jonnu.advent.common.BufferedResourceReader;
import com.github.jonnu.advent.common.ResourceReader;
import com.google.inject.Binder;
import com.google.inject.Module;

public class AdventModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(ResourceReader.class).to(BufferedResourceReader.class);
    }
}
