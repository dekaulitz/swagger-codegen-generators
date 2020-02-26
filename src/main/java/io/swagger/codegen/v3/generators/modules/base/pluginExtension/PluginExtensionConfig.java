package io.swagger.codegen.v3.generators.modules.base.pluginExtension;

import io.swagger.codegen.v3.plugins.Plugins;
import io.swagger.codegen.v3.plugins.repository.PluginsCodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

public interface PluginExtensionConfig {

    void setPluginConfig(Plugins pluginConfig);

    void setOpenApi(OpenAPI openAPI);

    String getName();

    void execute();
}
