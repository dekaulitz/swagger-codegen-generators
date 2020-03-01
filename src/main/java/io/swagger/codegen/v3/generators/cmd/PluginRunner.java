package io.swagger.codegen.v3.generators.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.generators.CLIHelper;
import io.swagger.codegen.v3.generators.modules.PluginConfigurator;
import io.swagger.codegen.v3.generators.modules.PluginsGenerator;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.util.RemoteUrl;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.config.CodegenConfiguratorUtils.*;
import static io.swagger.codegen.v3.generators.CLIHelper.isValidJson;
import static io.swagger.codegen.v3.generators.CLIHelper.isValidYaml;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class PluginRunner extends Generate {
    private String url;
    private List<CodegenArgument> codegenArguments;
    //add new extension runner
    protected String extensionPlugin;

    public String getExtensionPlugin() {
        return extensionPlugin;
    }

    public void setExtensionPlugin(String extensionPlugin) {
        this.extensionPlugin = extensionPlugin;
    }

    @Override
    public void run() {
        loadArguments();

        // attempt to read from config file
        PluginConfigurator configurator = PluginConfigurator.fromFile(configFile);




        // if a config file wasn't specified or we were unable to read it
        if (configurator == null) {
            // create a fresh configurator
            configurator = new PluginConfigurator();
        }
        //add new extension plugins
        if (isNotEmpty(extensionPlugin)) {
            configurator.setExtensionPlugin(this.extensionPlugin.split(","));
        }

        // now override with any specified parameters
        if (verbose != null) {
            configurator.setVerbose(verbose);
        }

        if (skipOverwrite != null) {
            configurator.setSkipOverwrite(skipOverwrite);
        }

        if (isNotEmpty(spec)) {
            configurator.setInputSpecURL(spec);
        }

        if (isNotEmpty(lang)) {
            configurator.setLang(lang);
        }

        if (isNotEmpty(output)) {
            configurator.setOutputDir(output);
        }

        if (isNotEmpty(auth)) {
            configurator.setAuth(auth);
        }

        if (isNotEmpty(templateDir)) {
            configurator.setTemplateDir(templateDir);
        }

        if (isNotEmpty(templateVersion)) {
            configurator.setTemplateVersion(templateVersion);
        }

        if (isNotEmpty(apiPackage)) {
            configurator.setApiPackage(apiPackage);
        }

        if (isNotEmpty(modelPackage)) {
            configurator.setModelPackage(modelPackage);
        }

        if (isNotEmpty(modelNamePrefix)) {
            configurator.setModelNamePrefix(modelNamePrefix);
        }

        if (isNotEmpty(modelNameSuffix)) {
            configurator.setModelNameSuffix(modelNameSuffix);
        }

        if (isNotEmpty(invokerPackage)) {
            configurator.setInvokerPackage(invokerPackage);
        }

        if (isNotEmpty(groupId)) {
            configurator.setGroupId(groupId);
        }

        if (isNotEmpty(artifactId)) {
            configurator.setArtifactId(artifactId);
        }

        if (isNotEmpty(artifactVersion)) {
            configurator.setArtifactVersion(artifactVersion);
        }

        if (isNotEmpty(library)) {
            configurator.setLibrary(library);
        }

        if (isNotEmpty(gitUserId)) {
            configurator.setGitUserId(gitUserId);
        }

        if (isNotEmpty(gitRepoId)) {
            configurator.setGitRepoId(gitRepoId);
        }

        if (isNotEmpty(releaseNote)) {
            configurator.setReleaseNote(releaseNote);
        }

        if (isNotEmpty(httpUserAgent)) {
            configurator.setHttpUserAgent(httpUserAgent);
        }

        if (isNotEmpty(ignoreFileOverride)) {
            configurator.setIgnoreFileOverride(ignoreFileOverride);
        }

        if (removeOperationIdPrefix != null) {
            configurator.setRemoveOperationIdPrefix(removeOperationIdPrefix);
        }

        if (codegenArguments != null && !codegenArguments.isEmpty()) {
            configurator.setCodegenArguments(codegenArguments);
        }

        if (disableExamples != null && disableExamples) {
            additionalProperties.add(String.format("%s=%s", CodegenConstants.DISABLE_EXAMPLES_OPTION, disableExamples.toString()));
        }

        if (CodegenConstants.MUSTACHE_TEMPLATE_ENGINE.equalsIgnoreCase(templateEngine)) {
            additionalProperties.add(String.format("%s=%s", CodegenConstants.TEMPLATE_ENGINE, CodegenConstants.MUSTACHE_TEMPLATE_ENGINE));
        } else {
            additionalProperties.add(String.format("%s=%s", CodegenConstants.TEMPLATE_ENGINE, CodegenConstants.HANDLEBARS_TEMPLATE_ENGINE));
        }

        applySystemPropertiesKvpList(systemProperties, configurator);
        applyInstantiationTypesKvpList(instantiationTypes, configurator);
        applyImportMappingsKvpList(importMappings, configurator);
        applyTypeMappingsKvpList(typeMappings, configurator);
        applyAdditionalPropertiesKvpList(additionalProperties, configurator);
        applyLanguageSpecificPrimitivesCsvList(languageSpecificPrimitives, configurator);
        applyReservedWordsMappingsKvpList(reservedWordsMappings, configurator);
        //collection all command
        final ClientOptInput clientOptInput = configurator.toClientOptInput();

        new PluginsGenerator().opts(clientOptInput).generate();
    }

    protected void loadArguments() {
        if (StringUtils.isBlank(this.url)) {
            return;
        }
        final String content;
        File file = new File(this.url);
        if (file.exists() && file.isFile()) {
            try {
                content = FileUtils.readFileToString(file);
            } catch (IOException e) {
                LOG.error("Unable to read file: " + this.url, e);
                return;
            }
        } else if (CLIHelper.isValidURL(this.url)) {
            try {
                content = RemoteUrl.urlToString(this.url, null);
            } catch (Exception e) {
                LOG.error("Unable to read url: " + this.url, e);
                return;
            }
        } else {
            return;
        }

        if (StringUtils.isBlank(content)) {
            return;
        }

        JsonNode node = null;

        if (isValidJson(content)) {
            try {
                node = Json.mapper().readTree(content.getBytes());
            } catch (IOException e) {
                LOG.error("Unable to deserialize json from: " + this.url, e);
                node = null;
            }
        } else if (isValidYaml(content)) {
            try {
                node = Yaml.mapper().readTree(content.getBytes());
            } catch (IOException e) {
                LOG.error("Unable to deserialize yaml from: " + this.url, e);
                node = null;
            }
        }

        if (node == null) {
            return;
        }

        final Map<String, Object> optionValueMap = CLIHelper.createOptionValueMap(node);
        try {
            BeanUtils.populate(this, optionValueMap);
        } catch (Exception e) {
            LOG.error("Error setting values to object.", e);
        }
    }
}
