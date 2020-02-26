package io.swagger.codegen.v3.generators.modules.base.pluginExtension;

import io.swagger.codegen.v3.plugins.Plugins;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePluginExtension implements PluginExtensionConfig {
    protected final Logger LOGGER = LoggerFactory.getLogger(BasePluginExtension.class);
    protected OpenAPI openAPI;
    protected Plugins plugins;

    @Override
    public void setOpenApi(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }

    @Override
    public void setPluginConfig(Plugins plugins) {
        this.plugins = plugins;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public Plugins getPlugins() {
        return plugins;
    }


}
