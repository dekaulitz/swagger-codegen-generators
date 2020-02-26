package io.swagger.codegen.v3.generators.cmd;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenConfigLoader;
import io.swagger.codegen.v3.generators.modules.PluginConfigLoader;

import io.swagger.codegen.v3.plugins.Plugins;

public class PluginConfigHelp implements Runnable {
    private String lang;

    /**
     * <p>Setter for the field <code>lang</code>.</p>
     *
     * @param lang a {@link java.lang.String} object.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        System.out.println();
        Plugins config =  PluginConfigLoader.forName(lang);
        System.out.println("CONFIG OPTIONS");
        for (CliOption langCliOption : config.cliOptions()) {
            System.out.println("\t" + langCliOption.getOpt());
            System.out.println("\t    "
                + langCliOption.getOptionHelp().replaceAll("\n", "\n\t    "));
            System.out.println();
        }
    }
}
