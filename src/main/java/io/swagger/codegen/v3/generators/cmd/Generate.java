package io.swagger.codegen.v3.generators.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.CLIHelper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.util.RemoteUrl;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.config.CodegenConfiguratorUtils.*;
import static io.swagger.codegen.v3.generators.CLIHelper.isValidJson;
import static io.swagger.codegen.v3.generators.CLIHelper.isValidYaml;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * User: lanwen Date: 24.03.15 Time: 20:22
 *
 * @author ovo
 * @version $Id: $Id
 */
public class Generate implements Runnable {

    /** Constant <code>LOG</code> */
    public static final Logger LOG = LoggerFactory.getLogger(Generate.class);

    protected Boolean verbose;
    protected String lang;
    protected String output = "";
    protected String spec;
    protected String templateDir;
    protected String templateVersion;
    protected String templateEngine;
    protected String auth;
    protected List<String> systemProperties = new ArrayList<>();
    protected String configFile;
    protected Boolean skipOverwrite;
    protected String apiPackage;
    protected String modelPackage;
    protected String modelNamePrefix;
    protected String modelNameSuffix;
    protected List<String> instantiationTypes = new ArrayList<>();
    protected List<String> typeMappings = new ArrayList<>();
    protected List<String> additionalProperties = new ArrayList<>();
    protected List<String> languageSpecificPrimitives = new ArrayList<>();
    protected List<String> importMappings = new ArrayList<>();
    protected String invokerPackage;
    protected String groupId;
    protected String artifactId;
    protected String artifactVersion;
    protected String library;
    protected String gitUserId;
    protected String gitRepoId;
    protected String releaseNote;
    protected String httpUserAgent;
    protected List<String> reservedWordsMappings = new ArrayList<>();
    protected String ignoreFileOverride;
    protected Boolean removeOperationIdPrefix;
    protected Boolean disableExamples;
    private String url;
    private List<CodegenArgument> codegenArguments;

    /**
     * <p>Setter for the field <code>verbose</code>.</p>
     *
     * @param verbose a {@link java.lang.Boolean} object.
     */
    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * <p>Setter for the field <code>lang</code>.</p>
     *
     * @param lang a {@link java.lang.String} object.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * <p>Setter for the field <code>output</code>.</p>
     *
     * @param output a {@link java.lang.String} object.
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * <p>Setter for the field <code>spec</code>.</p>
     *
     * @param spec a {@link java.lang.String} object.
     */
    public void setSpec(String spec) {
        this.spec = spec;
    }

    /**
     * <p>Setter for the field <code>templateDir</code>.</p>
     *
     * @param templateDir a {@link java.lang.String} object.
     */
    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    /**
     * <p>Setter for the field <code>templateVersion</code>.</p>
     *
     * @param templateVersion a {@link java.lang.String} object.
     */
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    /**
     * <p>Setter for the field <code>templateEngine</code>.</p>
     *
     * @param templateEngine a {@link java.lang.String} object.
     */
    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * <p>Setter for the field <code>auth</code>.</p>
     *
     * @param auth a {@link java.lang.String} object.
     */
    public void setAuth(String auth) {
        this.auth = auth;
    }

    /**
     * <p>Setter for the field <code>systemProperties</code>.</p>
     *
     * @param systemProperties a {@link java.util.List} object.
     */
    public void setSystemProperties(List<String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    /**
     * <p>Setter for the field <code>configFile</code>.</p>
     *
     * @param configFile a {@link java.lang.String} object.
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * <p>Setter for the field <code>skipOverwrite</code>.</p>
     *
     * @param skipOverwrite a {@link java.lang.Boolean} object.
     */
    public void setSkipOverwrite(Boolean skipOverwrite) {
        this.skipOverwrite = skipOverwrite;
    }

    /**
     * <p>Setter for the field <code>apiPackage</code>.</p>
     *
     * @param apiPackage a {@link java.lang.String} object.
     */
    public void setApiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
    }

    /**
     * <p>Setter for the field <code>modelPackage</code>.</p>
     *
     * @param modelPackage a {@link java.lang.String} object.
     */
    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    /**
     * <p>Setter for the field <code>modelNamePrefix</code>.</p>
     *
     * @param modelNamePrefix a {@link java.lang.String} object.
     */
    public void setModelNamePrefix(String modelNamePrefix) {
        this.modelNamePrefix = modelNamePrefix;
    }

    /**
     * <p>Setter for the field <code>modelNameSuffix</code>.</p>
     *
     * @param modelNameSuffix a {@link java.lang.String} object.
     */
    public void setModelNameSuffix(String modelNameSuffix) {
        this.modelNameSuffix = modelNameSuffix;
    }

    /**
     * <p>Setter for the field <code>instantiationTypes</code>.</p>
     *
     * @param instantiationTypes a {@link java.util.List} object.
     */
    public void setInstantiationTypes(List<String> instantiationTypes) {
        this.instantiationTypes = instantiationTypes;
    }

    /**
     * <p>Setter for the field <code>typeMappings</code>.</p>
     *
     * @param typeMappings a {@link java.util.List} object.
     */
    public void setTypeMappings(List<String> typeMappings) {
        this.typeMappings = typeMappings;
    }

    /**
     * <p>Setter for the field <code>additionalProperties</code>.</p>
     *
     * @param additionalProperties a {@link java.util.List} object.
     */
    public void setAdditionalProperties(List<String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * <p>Setter for the field <code>languageSpecificPrimitives</code>.</p>
     *
     * @param languageSpecificPrimitives a {@link java.util.List} object.
     */
    public void setLanguageSpecificPrimitives(List<String> languageSpecificPrimitives) {
        this.languageSpecificPrimitives = languageSpecificPrimitives;
    }

    /**
     * <p>Setter for the field <code>importMappings</code>.</p>
     *
     * @param importMappings a {@link java.util.List} object.
     */
    public void setImportMappings(List<String> importMappings) {
        this.importMappings = importMappings;
    }

    /**
     * <p>Setter for the field <code>invokerPackage</code>.</p>
     *
     * @param invokerPackage a {@link java.lang.String} object.
     */
    public void setInvokerPackage(String invokerPackage) {
        this.invokerPackage = invokerPackage;
    }

    /**
     * <p>Setter for the field <code>groupId</code>.</p>
     *
     * @param groupId a {@link java.lang.String} object.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * <p>Setter for the field <code>artifactId</code>.</p>
     *
     * @param artifactId a {@link java.lang.String} object.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * <p>Setter for the field <code>artifactVersion</code>.</p>
     *
     * @param artifactVersion a {@link java.lang.String} object.
     */
    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    /**
     * <p>Setter for the field <code>library</code>.</p>
     *
     * @param library a {@link java.lang.String} object.
     */
    public void setLibrary(String library) {
        this.library = library;
    }

    /**
     * <p>Setter for the field <code>gitUserId</code>.</p>
     *
     * @param gitUserId a {@link java.lang.String} object.
     */
    public void setGitUserId(String gitUserId) {
        this.gitUserId = gitUserId;
    }

    /**
     * <p>Setter for the field <code>gitRepoId</code>.</p>
     *
     * @param gitRepoId a {@link java.lang.String} object.
     */
    public void setGitRepoId(String gitRepoId) {
        this.gitRepoId = gitRepoId;
    }

    /**
     * <p>Setter for the field <code>releaseNote</code>.</p>
     *
     * @param releaseNote a {@link java.lang.String} object.
     */
    public void setReleaseNote(String releaseNote) {
        this.releaseNote = releaseNote;
    }

    /**
     * <p>Setter for the field <code>httpUserAgent</code>.</p>
     *
     * @param httpUserAgent a {@link java.lang.String} object.
     */
    public void setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent = httpUserAgent;
    }

    /**
     * <p>Setter for the field <code>reservedWordsMappings</code>.</p>
     *
     * @param reservedWordsMappings a {@link java.util.List} object.
     */
    public void setReservedWordsMappings(List<String> reservedWordsMappings) {
        this.reservedWordsMappings = reservedWordsMappings;
    }

    /**
     * <p>Setter for the field <code>ignoreFileOverride</code>.</p>
     *
     * @param ignoreFileOverride a {@link java.lang.String} object.
     */
    public void setIgnoreFileOverride(String ignoreFileOverride) {
        this.ignoreFileOverride = ignoreFileOverride;
    }

    /**
     * <p>Setter for the field <code>removeOperationIdPrefix</code>.</p>
     *
     * @param removeOperationIdPrefix a {@link java.lang.Boolean} object.
     */
    public void setRemoveOperationIdPrefix(Boolean removeOperationIdPrefix) {
        this.removeOperationIdPrefix = removeOperationIdPrefix;
    }

    /**
     * <p>Setter for the field <code>url</code>.</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * <p>Setter for the field <code>codegenArguments</code>.</p>
     *
     * @param codegenArguments a {@link java.util.List} object.
     */
    public void setCodegenArguments(List<CodegenArgument> codegenArguments) {
        this.codegenArguments = codegenArguments;
    }

    /**
     * <p>Setter for the field <code>disableExamples</code>.</p>
     *
     * @param disableExamples a {@link java.lang.Boolean} object.
     */
    public void setDisableExamples(Boolean disableExamples) {
        this.disableExamples = disableExamples;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {

        loadArguments();

        // attempt to read from config file
        CodegenConfigurator configurator = CodegenConfigurator.fromFile(configFile);

        // if a config file wasn't specified or we were unable to read it
        if (configurator == null) {
            // create a fresh configurator
            configurator = new CodegenConfigurator();
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
        final ClientOptInput clientOptInput = configurator.toClientOptInput();

        new CustomGenerator().opts(clientOptInput).generate();
    }

    private void loadArguments() {
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
