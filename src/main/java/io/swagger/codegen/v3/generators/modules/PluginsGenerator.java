package io.swagger.codegen.v3.generators.modules;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.cmd.Version;
import io.swagger.codegen.v3.generators.modules.base.pluginExtension.PluginExtensionConfig;
import io.swagger.codegen.v3.generators.modules.base.pluginExtension.XtensionConfigLoader;
import io.swagger.codegen.v3.ignore.CodegenIgnoreProcessor;
import io.swagger.codegen.v3.plugins.EntitiesSchemas;
import io.swagger.codegen.v3.plugins.Plugins;
import io.swagger.codegen.v3.templates.TemplateEngine;
import io.swagger.codegen.v3.utils.ImplementationVersion;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PluginsGenerator extends AbstractGenerator implements Generator {
    protected final Logger LOGGER = LoggerFactory.getLogger(PluginsGenerator.class);

    protected CodegenConfig config;

    protected Plugins plugins;


    protected ClientOptInput opts;
    //OpenAPI OBject
    protected OpenAPI openAPI;
    protected CodegenIgnoreProcessor ignoreProcessor;
    protected TemplateEngine templateEngine;
    //checking can generate swagger meta data
    protected Boolean generateSwaggerMetadata = true;
    //checking can generate entities
    protected Boolean generateEntities = null;



    protected Boolean useOas2 = false;
    protected String basePath;
    protected String basePathWithoutHost;
    protected String contextPath;
    protected Map<String, String> generatorPropertyDefaults = new HashMap<>();
    //custom field for creating extension
    private String[] pluginExtensions;

    @Override
    //this is for asign object property from client ClientOptInput
    //this the entry point for assign new property from this object and getting the value from client input
    public Generator opts(ClientOptInput opts) {
        this.opts = opts;
        this.openAPI = opts.getOpenAPI();
        this.config = opts.getConfig();
        this.config.additionalProperties().putAll(opts.getOpts().getProperties());
        //custom plugins
        this.plugins = opts.getPlugins();
        this.plugins.additionalProperties().putAll(opts.getOpts().getProperties());
        //assing extension plugin from opts.getExtensionPlugin()
        this.pluginExtensions = opts.getExtensionPlugin();

        String ignoreFileLocation = this.config.getIgnoreFilePathOverride();
        if (ignoreFileLocation != null) {
            final File ignoreFile = new File(ignoreFileLocation);
            if (ignoreFile.exists() && ignoreFile.canRead()) {
                this.ignoreProcessor = new CodegenIgnoreProcessor(ignoreFile);
            } else {
                LOGGER.warn("Ignore file specified at {} is not valid. This will fall back to an existing ignore file if present in the output directory.", ignoreFileLocation);
            }
        }

        if (this.ignoreProcessor == null) {
            this.ignoreProcessor = new CodegenIgnoreProcessor(this.config.getOutputDir());
        }
        return this;
    }

    /**
     * Programmatically disable the output of .swagger-codegen/VERSION, .swagger-codegen-ignore,
     * or other metadata files used by Swagger Codegen.
     *
     * @param generateSwaggerMetadata true: enable outputs, false: disable outputs
     */
    @SuppressWarnings("WeakerAccess")
    public void setGenerateSwaggerMetadata(Boolean generateSwaggerMetadata) {
        this.generateSwaggerMetadata = generateSwaggerMetadata;
    }

    /**
     * Set generator properties otherwise pulled from system properties.
     * Useful for running tests in parallel without relying on System.properties.
     *
     * @param key   The system property key
     * @param value The system property value
     */
    @SuppressWarnings("WeakerAccess")
    public void setGeneratorPropertyDefault(final String key, final String value) {
        this.generatorPropertyDefaults.put(key, value);
    }

    protected Boolean getGeneratorPropertyDefaultSwitch(final String key, final Boolean defaultValue) {
        String result = null;
        if (this.generatorPropertyDefaults.containsKey(key)) {
            result = this.generatorPropertyDefaults.get(key);
        }
        if (result != null) {
            return Boolean.valueOf(result);
        }
        return defaultValue;
    }

    protected String getScheme() {
        String scheme = URLPathUtil.getScheme(this.openAPI, this.config);
        if (StringUtils.isBlank(scheme)) {
            scheme = "https";
        }
        scheme = config.escapeText(scheme);
        return scheme;
    }

    protected void configureGeneratorProperties() {
        plugins.processOpts();
        plugins.preprocessOpenAPI(this.openAPI);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        plugins.additionalProperties().put("generatorVersion", Version.readVersionFromResources());
        plugins.additionalProperties().put("generateDate", dtf.format(LocalDateTime.now()));
        plugins.additionalProperties().put("generatorClass", plugins.getClass().getName());
        plugins.additionalProperties().put("inputSpec", plugins.getInputSpec());
        if (this.openAPI.getExtensions() != null) {
            plugins.vendorExtensions().putAll(this.openAPI.getExtensions());
        }
        this.templateEngine = plugins.getTemplateEngine();
        URL url = URLPathUtil.getServerURL(openAPI, plugins);
        contextPath = plugins.escapeText(url == null ? StringUtils.EMPTY : url.getPath());
        basePath = plugins.escapeText(URLPathUtil.getHost(openAPI));
        basePathWithoutHost = plugins.escapeText(contextPath);
    }

    /**
     * @return
     * @apiNote this the main function for generate
     */
    @Override

    //@notes this is main gate for generating
    public List<File> generate() {

        if (openAPI == null) {
            throw new RuntimeException("missing OpenAPI input!");
        }
        if (config == null) {
            throw new RuntimeException("missing configuration input!");
        }
        configureGeneratorProperties();
        //check if some extension propety
        if (this.pluginExtensions != null && this.pluginExtensions.length != 0) {
            Map<String, PluginExtensionConfig> extmap = new HashMap<>();
            for (String ext : this.pluginExtensions) {
                extmap.put(ext, XtensionConfigLoader.forName(ext));
            }
            extmap.forEach((s, o) -> {
                LOGGER.info("generating plugin extension" + o.getClass().getName());
                o.setOpenApi(this.openAPI);
                o.setPluginConfig(this.plugins);
                o.execute();
            });
        }

        //listing all files
        List<File> files = new ArrayList<>();
        List<Object> allEntities = new ArrayList<>();
        generateEntitiesFile(files, allEntities);
        return null;
    }

    private void generateEntitiesFile(List<File> files, List<Object> allEntities) {
        Map<String, Schema> schemas = new HashMap<>();
        LinkedHashMap<String, LinkedHashMap<String, Object>> op = (LinkedHashMap<String, LinkedHashMap<String, Object>>) this.openAPI.getComponents().getExtensions().get("x-entities");
        if (op == null) {
            return;
        }
        for (Map.Entry entry : op.entrySet()) {
            schemas.put(entry.getKey().toString(), Json.mapper().convertValue(entry.getValue(), Schema.class));
        }

        //get argument from terminal get all models argument
        String modelNames = System.getProperty("entities");
        Set<String> modelsToGenerate = null;
        //if argument exist will only generate base on argument from terminal
        if (modelNames != null && !modelNames.isEmpty()) {
            modelsToGenerate = new HashSet<>(Arrays.asList(modelNames.split(",")));
        }

        Set<String> modelKeys = schemas.keySet();
        if (modelsToGenerate != null && !modelsToGenerate.isEmpty()) {
            Set<String> updatedKeys = new HashSet<>();
            for (String m : modelKeys) {
                if (modelsToGenerate.contains(m)) {
                    updatedKeys.add(m);
                }
            }
            modelKeys = updatedKeys;
        }

        // store all processed models
        Map<String, Object> postProcessAllEntities = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return ObjectUtils.compare(plugins.toEntitiesName(o1), plugins.toEntitiesName(o2));
            }
        });

        // process models only
        for (String name : modelKeys) {
            try {
                //don't generate models that have an import mapping
                if (plugins.importMapping().containsKey(name)) {
                    LOGGER.info("Model " + name + " not imported due to import mapping");
                    continue;
                }
                //map schema
                Schema schema = schemas.get(name);
                Map<String, Schema> schemaMap = new HashMap<>();
                schemaMap.put(name, schema);
                Map<String, Object> models = processEntities(plugins, schemaMap, schemas);
                models.put("classname", plugins.toEntitiesName(name));
                models.putAll(plugins.additionalProperties());
                //processing all modes
                postProcessAllEntities.put(name, models);

                final List<Object> modelList = (List<Object>) models.get("models");

                if (modelList == null || modelList.isEmpty()) {
                    continue;
                }

                for (Object object : modelList) {
                    Map<String, Object> modelMap = (Map<String, Object>) object;
                    CodegenModel codegenModel = null;
                    if (modelMap.containsKey("oneOf-model")) {
                        codegenModel = (CodegenModel) modelMap.get("oneOf-model");
                    }
                    if (modelMap.containsKey("anyOf-model")) {
                        codegenModel = (CodegenModel) modelMap.get("anyOf-model");
                    }
                    if (codegenModel != null) {
                        models = processEntitiy(codegenModel, plugins, schemas);
                        models.put("classname", plugins.toEntitiesName(codegenModel.name));
                        models.putAll(plugins.additionalProperties());
                        postProcessAllEntities.put(codegenModel.name, models);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not process model '" + name + "'" + ".Please make sure that your schema is correct!", e);
            }
        }

        // post process all processed models
        postProcessAllEntities = plugins.postProcessAllEntities(postProcessAllEntities);

        // generate files based on processed models
        for (String modelName : postProcessAllEntities.keySet()) {
            Map<String, Object> entities = (Map<String, Object>) postProcessAllEntities.get(modelName);
            try {
                //don't generate entities that have an import mapping
                if (plugins.importMapping().containsKey(modelName)) {
                    continue;
                }
                Map<String, Object> modelTemplate = (Map<String, Object>) ((List<Object>) entities.get("entities")).get(0);
                allEntities.add(modelTemplate);
                for (String templateName : plugins.entitiesTemplateFiles().keySet()) {
                    String suffix = plugins.entitiesTemplateFiles().get(templateName);
                    String filename = plugins.entitiesFileFolder() + File.separator + plugins.toEntitiesFilename(modelName) + suffix;
                    if (!plugins.shouldOverwrite(filename)) {
                        LOGGER.info("Skipped overwriting " + filename);
                        continue;
                    }
                    File written = processTemplateToFile(entities, templateName, filename);
                    if (written != null) {
                        files.add(written);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not generate model '" + modelName + "'", e);
            }
        }
        if (System.getProperty("debugentities") != null) {
            LOGGER.info("############ Model info ############");
            Json.prettyPrint(allEntities);
        }


    }



    protected File processTemplateToFile(Map<String, Object> templateData, String templateName, String outputFilename) throws IOException {
        String adjustedOutputFilename = outputFilename.replaceAll("//", "/").replace('/', File.separatorChar);
        if (ignoreProcessor.allowsFile(new File(adjustedOutputFilename))) {
            String templateFile = getFullTemplateFile(config, templateName);
            String rendered = templateEngine.getRendered(templateFile, templateData);
            writeToFile(adjustedOutputFilename, rendered);
            return new File(adjustedOutputFilename);
        }

        LOGGER.info("Skipped generation of " + adjustedOutputFilename + " due to rule in .swagger-codegen-ignore");
        return null;
    }



    /*
    produce general property like package import and check
     */
    protected Map<String, Object> processEntities(CodegenConfig config, Map<String, Schema> definitions, Map<String, Schema> allDefinitions) {
        Map<String, Object> objs = new HashMap<>();
        objs.put("package", plugins.entitiesPackage());
        List<Object> entities = new ArrayList<>();
        Set<String> allImports = new LinkedHashSet<>();
        for (String key : definitions.keySet()) {
            Schema schema = definitions.get(key);
            //inject model into codegen model
            CodegenModel cm = plugins.fromModel(key, schema, allDefinitions);
            cm.classFilename = plugins.toEntitiesName(key);
            Map<String, Object> mo = new HashMap<>();
            mo.put("model", cm);
            mo.put("importPath", plugins.toEntityImport(cm.classname));
//            mo.put("ventities",config.modelPackage()+ cm.classname);
            if (cm.vendorExtensions.containsKey("oneOf-model")) {
                CodegenModel oneOfModel = (CodegenModel) cm.vendorExtensions.get("oneOf-model");
                mo.put("oneOf-model", oneOfModel);
            }
            if (cm.vendorExtensions.containsKey("anyOf-model")) {
                CodegenModel anyOfModel = (CodegenModel) cm.vendorExtensions.get("anyOf-model");
                mo.put("anyOf-model", anyOfModel);
            }

            entities.add(mo);

            allImports.addAll(cm.imports);
        }
        objs.put("entities", entities);
        //setup all import
        Set<String> importSet = new TreeSet<>();
        for (String nextImport : allImports) {
            String mapping = plugins.importMapping().get(nextImport);
            if (mapping == null) {
                mapping = plugins.toEntityImport(nextImport);
            }
            if (mapping != null && !plugins.defaultIncludes().contains(mapping)) {
                importSet.add(mapping);
            }
            // add instantiation types
            mapping = config.instantiationTypes().get(nextImport);
            if (mapping != null && !plugins.defaultIncludes().contains(mapping)) {
                importSet.add(mapping);
            }
        }
        List<Map<String, String>> imports = new ArrayList<>();
        for (String s : importSet) {
            Map<String, String> item = new HashMap<>();
            item.put("import", s);
            imports.add(item);
        }
        objs.put("imports", imports);
        plugins.postProcessEntities(objs);
        return objs;
    }

    protected Map<String, Object> processEntitiy(CodegenModel codegenModel, CodegenConfig config, Map<String, Schema> allDefinitions) {
        Map<String, Object> objs = new HashMap<>();
        objs.put("package", plugins.entitiesPackage());
        List<Object> entities = new ArrayList<>();

        if (codegenModel.vendorExtensions.containsKey("x-is-composed-model")) {
            objs.put("x-is-composed-model", codegenModel.vendorExtensions.get("x-is-composed-model"));
        }

        Map<String, Object> modelObject = new HashMap<>();
        modelObject.put("model", codegenModel);
        modelObject.put("importPath", plugins.toEntityImport(codegenModel.classname));

        Set<String> allImports = new LinkedHashSet<>();
        allImports.addAll(codegenModel.imports);
        entities.add(modelObject);

        objs.put("entities", entities);
        Set<String> importSet = new TreeSet<>();
        for (String nextImport : allImports) {
            String mapping = plugins.importMapping().get(nextImport);
            if (mapping == null) {
                mapping = plugins.toEntityImport(nextImport);
            }
            if (mapping != null && !plugins.defaultIncludes().contains(mapping)) {
                importSet.add(mapping);
            }
            // add instantiation types
            mapping = plugins.instantiationTypes().get(nextImport);
            if (mapping != null && !plugins.defaultIncludes().contains(mapping)) {
                importSet.add(mapping);
            }
        }
        List<Map<String, String>> imports = new ArrayList<>();
        for (String s : importSet) {
            Map<String, String> item = new HashMap<>();
            item.put("import", s);
            imports.add(item);
        }
        objs.put("imports", imports);
        plugins.postProcessEntities(objs);
        return objs;
    }
}
