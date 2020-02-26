package io.swagger.codegen.v3.generators.modules;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.auth.AuthParser;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.plugins.Plugins;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.ClasspathHelper;
import io.swagger.v3.parser.util.RemoteUrl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class PluginConfigurator extends CodegenConfigurator {
    public static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurator.class);



    protected String[] extensionPlugin;
    protected String lang;
    protected String inputSpec;
    protected String inputSpecURL;
    protected String outputDir;
    protected boolean verbose;
    protected boolean skipOverwrite;
    protected boolean removeOperationIdPrefix;
    protected String templateDir;
    protected String templateVersion;
    protected String auth;
    protected AuthorizationValue authorizationValue;
    protected String apiPackage;
    protected String modelPackage;
    protected String invokerPackage;
    protected String modelNamePrefix;
    protected String modelNameSuffix;
    protected String groupId;
    protected String artifactId;
    protected String artifactVersion;
    protected String library;
    protected String ignoreFileOverride;
    protected List<CodegenArgument> codegenArguments = new ArrayList<>();
    protected Map<String, String> systemProperties = new HashMap<String, String>();
    protected Map<String, String> instantiationTypes = new HashMap<String, String>();
    protected Map<String, String> typeMappings = new HashMap<String, String>();
    protected Map<String, Object> additionalProperties = new HashMap<String, Object>();
    protected Map<String, String> importMappings = new HashMap<String, String>();
    protected Set<String> languageSpecificPrimitives = new HashSet<String>();
    protected Map<String, String> reservedWordMappings = new HashMap<String, String>();

    protected String gitUserId = "GIT_USER_ID";
    protected String gitRepoId = "GIT_REPO_ID";
    protected String releaseNote = "Minor update";
    protected String httpUserAgent;

    protected final Map<String, Object> dynamicProperties = new HashMap<String, Object>(); //the map that holds the JsonAnySetter/JsonAnyGetter values

    public PluginConfigurator() {
        this.setOutputDir(".");
    }

    public PluginConfigurator setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public PluginConfigurator setInputSpec(String inputSpec) {
        this.inputSpec = inputSpec;
        return this;
    }

    public String getInputSpec() {
        return inputSpec;
    }

    public String getInputSpecURL() {
        return inputSpecURL;
    }

    public PluginConfigurator setInputSpecURL(String inputSpecURL) {
        this.inputSpecURL = inputSpecURL;
        return this;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public PluginConfigurator setOutputDir(String outputDir) {
        this.outputDir = toAbsolutePathStr(outputDir);
        return this;
    }

    public String getModelPackage() {
        return modelPackage;
    }

    public PluginConfigurator setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
        return this;
    }

    public String getModelNamePrefix() {
        return modelNamePrefix;
    }

    public PluginConfigurator setModelNamePrefix(String prefix) {
        this.modelNamePrefix = prefix;
        return this;
    }

    public boolean getRemoveOperationIdPrefix() {
        return removeOperationIdPrefix;
    }

    public PluginConfigurator setRemoveOperationIdPrefix(boolean removeOperationIdPrefix) {
        this.removeOperationIdPrefix = removeOperationIdPrefix;
        return this;
    }

    public String getModelNameSuffix() {
        return modelNameSuffix;
    }

    public PluginConfigurator setModelNameSuffix(String suffix) {
        this.modelNameSuffix = suffix;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public PluginConfigurator setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isSkipOverwrite() {
        return skipOverwrite;
    }

    public PluginConfigurator setSkipOverwrite(boolean skipOverwrite) {
        this.skipOverwrite = skipOverwrite;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    public PluginConfigurator setTemplateDir(String templateDir) {
        File f = new File(templateDir);

        // check to see if the folder exists
        if (!(f.exists() && f.isDirectory())) {
            throw new IllegalArgumentException("Template directory " + templateDir + " does not exist.");
        }

        this.templateDir = f.getAbsolutePath();
        return this;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public PluginConfigurator setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
        return this;
    }

    public String getAuth() {
        return auth;
    }

    public PluginConfigurator setAuth(String auth) {
        this.auth = auth;
        return this;
    }

    public AuthorizationValue getAuthorizationValue() {
        return authorizationValue;
    }

    public void setAuthorizationValue(AuthorizationValue authorizationValue) {
        this.authorizationValue = authorizationValue;
    }

    public String getApiPackage() {
        return apiPackage;
    }

    public PluginConfigurator setApiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
        return this;
    }

    public String getInvokerPackage() {
        return invokerPackage;
    }

    public PluginConfigurator setInvokerPackage(String invokerPackage) {
        this.invokerPackage = invokerPackage;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public PluginConfigurator setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public PluginConfigurator setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public PluginConfigurator setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
        return this;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public PluginConfigurator setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
        return this;
    }

    public PluginConfigurator addSystemProperty(String key, String value) {
        this.systemProperties.put(key, value);
        return this;
    }

    public Map<String, String> getInstantiationTypes() {
        return instantiationTypes;
    }

    public PluginConfigurator setInstantiationTypes(Map<String, String> instantiationTypes) {
        this.instantiationTypes = instantiationTypes;
        return this;
    }

    public PluginConfigurator addInstantiationType(String key, String value) {
        this.instantiationTypes.put(key, value);
        return this;
    }

    public Map<String, String> getTypeMappings() {
        return typeMappings;
    }

    public PluginConfigurator setTypeMappings(Map<String, String> typeMappings) {
        this.typeMappings = typeMappings;
        return this;
    }

    public PluginConfigurator addTypeMapping(String key, String value) {
        this.typeMappings.put(key, value);
        return this;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public PluginConfigurator setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    public PluginConfigurator addAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
        return this;
    }

    public Map<String, String> getImportMappings() {
        return importMappings;
    }

    public PluginConfigurator setImportMappings(Map<String, String> importMappings) {
        this.importMappings = importMappings;
        return this;
    }

    public PluginConfigurator addImportMapping(String key, String value) {
        this.importMappings.put(key, value);
        return this;
    }

    public Set<String> getLanguageSpecificPrimitives() {
        return languageSpecificPrimitives;
    }

    public PluginConfigurator setLanguageSpecificPrimitives(Set<String> languageSpecificPrimitives) {
        this.languageSpecificPrimitives = languageSpecificPrimitives;
        return this;
    }

    public PluginConfigurator addLanguageSpecificPrimitive(String value) {
        this.languageSpecificPrimitives.add(value);
        return this;
    }

    public String getLibrary() {
        return library;
    }

    public PluginConfigurator setLibrary(String library) {
        this.library = library;
        return this;
    }

    public String getGitUserId() {
        return gitUserId;
    }

    public PluginConfigurator setGitUserId(String gitUserId) {
        this.gitUserId = gitUserId;
        return this;
    }

    public String getGitRepoId() {
        return gitRepoId;
    }

    public PluginConfigurator setGitRepoId(String gitRepoId) {
        this.gitRepoId = gitRepoId;
        return this;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public PluginConfigurator setReleaseNote(String releaseNote) {
        this.releaseNote = releaseNote;
        return this;
    }

    public String getHttpUserAgent() {
        return httpUserAgent;
    }

    public PluginConfigurator setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent = httpUserAgent;
        return this;
    }

    public Map<String, String> getReservedWordsMappings() {
        return reservedWordMappings;
    }

    public PluginConfigurator setReservedWordsMappings(Map<String, String> reservedWordsMappings) {
        this.reservedWordMappings = reservedWordsMappings;
        return this;
    }

    public PluginConfigurator addAdditionalReservedWordMapping(String key, String value) {
        this.reservedWordMappings.put(key, value);
        return this;
    }

    public String getIgnoreFileOverride() {
        return ignoreFileOverride;
    }

    public PluginConfigurator setIgnoreFileOverride(final String ignoreFileOverride) {
        this.ignoreFileOverride = ignoreFileOverride;
        return this;
    }
    public String[] getExtensionPlugin() {
        return extensionPlugin;
    }

    public void setExtensionPlugin(String[] extensionPlugin) {
        this.extensionPlugin = extensionPlugin;
    }

    public String loadSpecContent(String location, List<AuthorizationValue> auths) throws Exception {
        location = location.replaceAll("\\\\", "/");
        String data = "";
        if (location.toLowerCase().startsWith("http")) {
            data = RemoteUrl.urlToString(location, auths);
        } else {
            final String fileScheme = "file:";
            Path path;
            if (location.toLowerCase().startsWith(fileScheme)) {
                path = Paths.get(URI.create(location));
            } else {
                path = Paths.get(location);
            }
            if (Files.exists(path)) {
                data = FileUtils.readFileToString(path.toFile(), "UTF-8");
            } else {
                data = ClasspathHelper.loadFileFromClasspath(location);
            }
        }
        LOGGER.trace("Loaded raw data: {}", data);
        return data;
    }

    public ClientOptInput toClientOptInput() {

        Validate.notEmpty(lang, "language must be specified");

        if (StringUtils.isBlank(inputSpec) && StringUtils.isBlank(inputSpecURL)) {
            throw new IllegalArgumentException("input spec or URL must be specified");
        }

        setVerboseFlags();
        setSystemProperties();

        Plugins config = PluginConfigLoader.forName(lang);
        ClientOptInput input = new ClientOptInput();
        final List<AuthorizationValue> authorizationValues = AuthParser.parse(auth);
        if (authorizationValue != null) {
            authorizationValues.add(authorizationValue);
        }
        if(this.extensionPlugin.length!=0){
            input.setExtensionPlugin(this.extensionPlugin);
        }
        if (!StringUtils.isBlank(inputSpec)) {
            config.setInputSpec(inputSpec);
            ParseOptions options = new ParseOptions();
            options.setResolve(true);
            options.setFlatten(true);
            SwaggerParseResult result = new OpenAPIParser().readContents(inputSpec, authorizationValues, options);
            OpenAPI openAPI = result.getOpenAPI();

            if (config.needsUnflattenedSpec()) {
                ParseOptions optionsUnflattened = new ParseOptions();
                optionsUnflattened.setResolve(true);
                SwaggerParseResult resultUnflattened = new OpenAPIParser().readContents(inputSpec, authorizationValues, optionsUnflattened);
                OpenAPI openAPIUnflattened = resultUnflattened.getOpenAPI();
                config.setUnflattenedOpenAPI(openAPIUnflattened);
            }

            input.opts(new ClientOpts())
                .openAPI(openAPI);

            LOGGER.debug("getClientOptInput - parsed inputSpec");
        } else {
            String specContent = null;
            try {
                specContent = loadSpecContent(inputSpecURL, authorizationValues);
            } catch (Exception e) {
                String msg = "Unable to read URL: " + inputSpecURL;
                LOGGER.error(msg, e);
                throw new IllegalArgumentException(msg);
            }

            if (StringUtils.isBlank(specContent)) {
                String msg = "Empty content found in URL: " + inputSpecURL;
                LOGGER.error(msg);
                throw new IllegalArgumentException(msg);
            }
            config.setInputSpec(specContent);
            config.setInputURL(inputSpecURL);
            ParseOptions options = new ParseOptions();
            options.setResolve(true);
            options.setFlatten(true);
            SwaggerParseResult result = new OpenAPIParser().readLocation(inputSpecURL, authorizationValues, options);
            OpenAPI openAPI = result.getOpenAPI();
            LOGGER.debug("getClientOptInput - parsed inputSpecURL " + inputSpecURL);
            input.opts(new ClientOpts())
                .openAPI(openAPI);

            if (config.needsUnflattenedSpec()) {
                ParseOptions optionsUnflattened = new ParseOptions();
                optionsUnflattened.setResolve(true);
                SwaggerParseResult resultUnflattened = new OpenAPIParser().readLocation(inputSpecURL, authorizationValues, optionsUnflattened);
                OpenAPI openAPIUnflattened = resultUnflattened.getOpenAPI();
                config.setUnflattenedOpenAPI(openAPIUnflattened);
            }

        }

        config.setOutputDir(outputDir);
        config.setSkipOverwrite(skipOverwrite);
        config.setIgnoreFilePathOverride(ignoreFileOverride);
        config.setRemoveOperationIdPrefix(removeOperationIdPrefix);

        config.instantiationTypes().putAll(instantiationTypes);
        config.typeMapping().putAll(typeMappings);
        config.importMapping().putAll(importMappings);
        config.languageSpecificPrimitives().addAll(languageSpecificPrimitives);
        config.reservedWordsMappings().putAll(reservedWordMappings);

        config.setLanguageArguments(codegenArguments);

        checkAndSetAdditionalProperty(apiPackage, CodegenConstants.API_PACKAGE);
        checkAndSetAdditionalProperty(modelPackage, CodegenConstants.MODEL_PACKAGE);
        checkAndSetAdditionalProperty(invokerPackage, CodegenConstants.INVOKER_PACKAGE);
        checkAndSetAdditionalProperty(groupId, CodegenConstants.GROUP_ID);
        checkAndSetAdditionalProperty(artifactId, CodegenConstants.ARTIFACT_ID);
        checkAndSetAdditionalProperty(artifactVersion, CodegenConstants.ARTIFACT_VERSION);
        checkAndSetAdditionalProperty(templateDir, toAbsolutePathStr(templateDir), CodegenConstants.TEMPLATE_DIR);
        checkAndSetAdditionalProperty(templateVersion, CodegenConstants.TEMPLATE_VERSION);
        checkAndSetAdditionalProperty(modelNamePrefix, CodegenConstants.MODEL_NAME_PREFIX);
        checkAndSetAdditionalProperty(modelNameSuffix, CodegenConstants.MODEL_NAME_SUFFIX);
        checkAndSetAdditionalProperty(gitUserId, CodegenConstants.GIT_USER_ID);
        checkAndSetAdditionalProperty(gitRepoId, CodegenConstants.GIT_REPO_ID);
        checkAndSetAdditionalProperty(releaseNote, CodegenConstants.RELEASE_NOTE);
        checkAndSetAdditionalProperty(httpUserAgent, CodegenConstants.HTTP_USER_AGENT);

        handleDynamicProperties(config);

        if (isNotEmpty(library)) {
            config.setLibrary(library);
        }

        config.additionalProperties().putAll(additionalProperties);

        input.config(config);
        input.setPlugins(config);
        return input;
    }

    @JsonAnySetter
    public PluginConfigurator addDynamicProperty(String name, Object value) {
        dynamicProperties.put(name, value);
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getDynamicProperties() {
        return dynamicProperties;
    }

    protected void handleDynamicProperties(CodegenConfig codegenConfig) {
        for (CliOption langCliOption : codegenConfig.cliOptions()) {
            String opt = langCliOption.getOpt();
            if (dynamicProperties.containsKey(opt)) {
                codegenConfig.additionalProperties().put(opt, dynamicProperties.get(opt));
            } else if (systemProperties.containsKey(opt)) {
                codegenConfig.additionalProperties().put(opt, systemProperties.get(opt));
            }
        }
    }

    protected void setVerboseFlags() {
        if (!verbose) {
            return;
        }
        LOGGER.info("\nVERBOSE MODE: ON. Additional debug options are injected" +
            "\n - [debugSwagger] prints the swagger specification as interpreted by the codegen" +
            "\n - [debugModels] prints models passed to the template engine" +
            "\n - [debugOperations] prints operations passed to the template engine" +
            "\n - [debugSupportingFiles] prints additional data passed to the template engine");

        System.setProperty("debugSwagger", "");
        System.setProperty("debugModels", "");
        System.setProperty("debugOperations", "");
        System.setProperty("debugSupportingFiles", "");
    }

    public void setCodegenArguments(List<CodegenArgument> codegenArguments) {
        this.codegenArguments = codegenArguments;
    }

    public List<CodegenArgument> getCodegenArguments() {
        return this.codegenArguments;
    }

    protected void setSystemProperties() {
        for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }

    protected static String toAbsolutePathStr(String path) {
        if (isNotEmpty(path)) {
            return Paths.get(path).toAbsolutePath().toString();
        }

        return path;

    }

    protected void checkAndSetAdditionalProperty(String property, String propertyKey) {
        checkAndSetAdditionalProperty(property, property, propertyKey);
    }

    protected void checkAndSetAdditionalProperty(String property, String valueToSet, String propertyKey) {
        if (isNotEmpty(property)) {
            additionalProperties.put(propertyKey, valueToSet);
        }
    }

    public static PluginConfigurator fromFile(String configFile) {

        if (isNotEmpty(configFile)) {
            try {
                return Json.mapper().readValue(new File(configFile), PluginConfigurator.class);
            } catch (IOException e) {
                LOGGER.error("Unable to deserialize config file: " + configFile, e);
            }
        }
        return null;
    }

}
