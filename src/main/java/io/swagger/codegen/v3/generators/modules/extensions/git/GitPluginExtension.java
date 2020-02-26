package io.swagger.codegen.v3.generators.modules.extensions.git;

import io.swagger.codegen.v3.generators.modules.base.pluginExtension.BasePluginExtension;

public class GitPluginExtension extends BasePluginExtension {
    @Override
    public String getName() {
        return "git";
    }

    @Override
    public void execute() {
        System.out.println(this.getPlugins().outputFolder());
        System.out.println(this.openAPI.getInfo());
    }
}
