package io.swagger.codegen.v3.generators.modules;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.plugins.Plugins;

import java.util.ServiceLoader;

import static java.util.ServiceLoader.load;

public class PluginConfigLoader {
    /**
     * Tries to load config class with SPI first, then with class name directly from classpath
     *
     * @param name name of config, or full qualified class name in classpath
     * @return config class
     */
    public static Plugins forName(String name) {
        ServiceLoader<Plugins> loader = load(Plugins.class);

        StringBuilder availableConfigs = new StringBuilder();

        for (Plugins config : loader) {
            if (config.getName().equals(name)) {
                return config;
            }

            availableConfigs.append(config.getName()).append("\n");
        }

        // else try to load directly
        try {
            return (Plugins) Class.forName(name).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't load config class with name ".concat(name) + " Available: " + availableConfigs.toString(), e);
        }
    }
}
