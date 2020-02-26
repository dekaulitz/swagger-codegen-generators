package io.swagger.codegen.v3.generators.cmd;

import ch.lambdaj.collection.LambdaIterable;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.plugins.Plugins;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static java.util.ServiceLoader.load;

public class Plugin implements Runnable {
    @Override
    public void run() {
        LambdaIterable<String> langs =
            with(load(Plugins.class))
                .extract(on(Plugins.class).getName());
        System.out.printf("Available languages: %s%n", langs);
    }
}
