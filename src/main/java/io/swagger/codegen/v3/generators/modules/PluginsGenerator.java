package io.swagger.codegen.v3.generators.modules;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.cmd.Version;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
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


        //listing all files
        List<File> files = new ArrayList<>();
        List<Object> allEntities = new ArrayList<>();
        generateEntitiesFile(files, allEntities);
        List<Object> allVmodels = new ArrayList<>();
        generateVmodel(files, allVmodels);

        // apis
        List<Object> allOperations = new ArrayList<>();
        generateApis(files, allOperations, allVmodels);
        Map<String, Object> bundle = buildSupportFileBundle(allOperations, allVmodels);
        generateSupportingFiles(files, bundle);

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
        String modelNames = System.getProperty("models");
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
            Map<String, Object> models = (Map<String, Object>) postProcessAllEntities.get(modelName);
            try {
                //don't generate models that have an import mapping
                if (plugins.importMapping().containsKey(modelName)) {
                    continue;
                }
                Map<String, Object> modelTemplate = (Map<String, Object>) ((List<Object>) models.get("models")).get(0);
                allEntities.add(modelTemplate);
                for (String templateName : plugins.entitiesTemplateFiles().keySet()) {
                    String suffix = plugins.entitiesTemplateFiles().get(templateName);
                    String filename = plugins.entitiesFileFolder() + File.separator + plugins.toEntitiesFilename(modelName) + suffix;
                    if (!plugins.shouldOverwrite(filename)) {
                        LOGGER.info("Skipped overwriting " + filename);
                        continue;
                    }
                    File written = processTemplateToFile(models, templateName, filename);
                    if (written != null) {
                        files.add(written);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not generate model '" + modelName + "'", e);
            }
        }
        if (System.getProperty("debugModels") != null) {
            LOGGER.info("############ Model info ############");
            Json.prettyPrint(allEntities);
        }


    }

    //@NOTE custom
    //custom generate all vmodels
    protected void generateVmodel(List<File> files, List<Object> allModels) {
        if(plugins.vModelFileFolder()==null){
           return;
        }
        final Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
        if (schemas == null) {
            return;
        }

        String modelNames = System.getProperty("models");
        Set<String> modelsToGenerate = null;
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
        Map<String, Object> allProcessedModels = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return ObjectUtils.compare(plugins.toVModelName(o1), plugins.toVModelName(o2));
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
                Schema schema = schemas.get(name);
                Map<String, Schema> schemaMap = new HashMap<>();
                schemaMap.put(name, schema);
                Map<String, Object> models = processVModels(plugins, schemaMap, schemas);
                models.put("classname", plugins.toVModelName(name));
                models.putAll(plugins.additionalProperties());
                allProcessedModels.put(name, models);

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
                        models = processVmodel(codegenModel, plugins, schemas);
                        models.put("classname", plugins.toVModelName(codegenModel.name));
                        models.putAll(plugins.additionalProperties());
                        allProcessedModels.put(codegenModel.name, models);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not process model '" + name + "'" + ".Please make sure that your schema is correct!", e);
            }
        }

        // post process all processed models
        allProcessedModels = plugins.postProcessAllVModels(allProcessedModels);

        // generate files based on processed models
        for (String modelName : allProcessedModels.keySet()) {
            Map<String, Object> models = (Map<String, Object>) allProcessedModels.get(modelName);
            try {
                //don't generate models that have an import mapping
                if (plugins.importMapping().containsKey(modelName)) {
                    continue;
                }
                Map<String, Object> vModels = (Map<String, Object>) ((List<Object>) models.get("models")).get(0);
                allModels.add(vModels);
                for (String templateName : plugins.vModelTemplateFiles().keySet()) {
                    String suffix = plugins.vModelTemplateFiles().get(templateName);
                    String filename = plugins.vModelFileFolder() + File.separator + plugins.toVModelName(modelName) + suffix;
                    if (!plugins.shouldOverwrite(filename)) {
                        LOGGER.info("Skipped overwriting " + filename);
                        continue;
                    }
                    File written = processTemplateToFile(models, templateName, filename);
                    if (written != null) {
                        files.add(written);
                    }
                }
                //its not necessary yet
//                if (generateModelTests) {
//                    generateModelTests(files, models, modelName);
//                }
//                if (generateModelDocumentation) {
//                    // to generate model documentation files
//                    generateModelDocumentation(files, models, modelName);
//                }

            } catch (Exception e) {
                throw new RuntimeException("Could not generate model '" + modelName + "'", e);
            }
        }
        if (System.getProperty("debugModels") != null) {
            LOGGER.info("############ Model info ############");
            Json.prettyPrint(allModels);
        }
    }


    /*
    produce general property like package import and check
     */
    protected Map<String, Object> processEntities(CodegenConfig config, Map<String, Schema> definitions, Map<String, Schema> allDefinitions) {
        Map<String, Object> objs = new HashMap<>();
        objs.put("package", plugins.entitiesPackage());
        List<Object> models = new ArrayList<>();
        Set<String> allImports = new LinkedHashSet<>();
        for (String key : definitions.keySet()) {
            Schema schema = definitions.get(key);
            //inject model into codegen model
            CodegenModel cm = plugins.fromModel(key, schema, allDefinitions);
            cm.classFilename = plugins.toEntitiesName(key);
            Map<String, Object> mo = new HashMap<>();
            mo.put("model", cm);
            mo.put("importPath", plugins.toEntityImport(cm.classname));
//            mo.put("vmodels",config.modelPackage()+ cm.classname);
            if (cm.vendorExtensions.containsKey("oneOf-model")) {
                CodegenModel oneOfModel = (CodegenModel) cm.vendorExtensions.get("oneOf-model");
                mo.put("oneOf-model", oneOfModel);
            }
            if (cm.vendorExtensions.containsKey("anyOf-model")) {
                CodegenModel anyOfModel = (CodegenModel) cm.vendorExtensions.get("anyOf-model");
                mo.put("anyOf-model", anyOfModel);
            }

            models.add(mo);

            allImports.addAll(cm.imports);
        }
        objs.put("models", models);
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
        plugins.postProcessModels(objs);
        return objs;
    }

    protected Map<String, Object> processEntitiy(CodegenModel codegenModel, CodegenConfig config, Map<String, Schema> allDefinitions) {
        Map<String, Object> objs = new HashMap<>();
        objs.put("package", plugins.entitiesPackage());
        List<Object> models = new ArrayList<>();

        if (codegenModel.vendorExtensions.containsKey("x-is-composed-model")) {
            objs.put("x-is-composed-model", codegenModel.vendorExtensions.get("x-is-composed-model"));
        }

        Map<String, Object> modelObject = new HashMap<>();
        modelObject.put("model", codegenModel);
        modelObject.put("importPath", plugins.toEntityImport(codegenModel.classname));

        Set<String> allImports = new LinkedHashSet<>();
        allImports.addAll(codegenModel.imports);
        models.add(modelObject);

        objs.put("models", models);
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


    protected Map<String, Object> processVModels(CodegenConfig config, Map<String, Schema> definitions, Map<String, Schema> allDefinitions) {
        Map<String, Object> objs = new HashMap<>();
        objs.put("package", plugins.entitiesPackage());
        List<Object> models = new ArrayList<>();
        Set<String> allImports = new LinkedHashSet<>();
        for (String key : definitions.keySet()) {
            Schema schema = definitions.get(key);
            //inject model into codegen model
            CodegenModel cm = plugins.fromModel(key, schema, allDefinitions);
            cm.classFilename = plugins.toVModelName(key);
            Map<String, Object> mo = new HashMap<>();
            mo.put("model", cm);
            mo.put("importPath", plugins.toVmodelImport(cm.classname));
//            mo.put("vmodels",config.modelPackage()+ cm.classname);
            if (cm.vendorExtensions.containsKey("oneOf-model")) {
                CodegenModel oneOfModel = (CodegenModel) cm.vendorExtensions.get("oneOf-model");
                mo.put("oneOf-model", oneOfModel);
            }
            if (cm.vendorExtensions.containsKey("anyOf-model")) {
                CodegenModel anyOfModel = (CodegenModel) cm.vendorExtensions.get("anyOf-model");
                mo.put("anyOf-model", anyOfModel);
            }

            models.add(mo);

            allImports.addAll(cm.imports);
        }
        objs.put("models", models);
        //setup all import
        Set<String> importSet = new TreeSet<>();
        for (String nextImport : allImports) {
            String mapping = plugins.importMapping().get(nextImport);
            if (mapping == null) {
                mapping = plugins.toVmodelImport(nextImport);
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
        plugins.postProcessModels(objs);
        return objs;
    }

    protected Map<String, Object> processVmodel(CodegenModel codegenModel, CodegenConfig config, Map<String, Schema> allDefinitions) {
        Map<String, Object> objs = new HashMap<>();
        objs.put("package", plugins.entitiesPackage());
        List<Object> models = new ArrayList<>();

        if (codegenModel.vendorExtensions.containsKey("x-is-composed-model")) {
            objs.put("x-is-composed-model", codegenModel.vendorExtensions.get("x-is-composed-model"));
        }

        Map<String, Object> modelObject = new HashMap<>();
        modelObject.put("model", codegenModel);
        modelObject.put("importPath", plugins.toVmodelImport(codegenModel.classname));

        Set<String> allImports = new LinkedHashSet<>();
        allImports.addAll(codegenModel.imports);
        models.add(modelObject);

        objs.put("models", models);
        Set<String> importSet = new TreeSet<>();
        for (String nextImport : allImports) {
            String mapping = plugins.importMapping().get(nextImport);
            if (mapping == null) {
                mapping = plugins.toVmodelImport(nextImport);
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
    protected Map<String, Object> buildSupportFileBundle(List<Object> allOperations, List<Object> allModels) {

        Map<String, Object> bundle = new HashMap<>();
        bundle.putAll(plugins.additionalProperties());
        bundle.put("apiPackage", plugins.apiPackage());

        Map<String, Object> apis = new HashMap<>();
        apis.put("apis", allOperations);

        URL url = URLPathUtil.getServerURL(openAPI, plugins);

        if (url != null) {
            bundle.put("host", url.getHost());
        }

        bundle.put("openAPI", openAPI);
        bundle.put("basePath", basePath);
        bundle.put("basePathWithoutHost", basePathWithoutHost);
        bundle.put("scheme", URLPathUtil.getScheme(openAPI, config));
        bundle.put("contextPath", contextPath);
        bundle.put("apiInfo", apis);
        bundle.put("models", allModels);
        boolean hasModel = true;
        if (allModels == null || allModels.isEmpty()) {
            hasModel = false;
        }
        bundle.put("hasModel", hasModel);
        bundle.put("apiFolder", plugins.apiPackage().replace('.', File.separatorChar));
        bundle.put("modelPackage", plugins.modelPackage());

//        processSecurityProperties(bundle);

        if (openAPI.getExternalDocs() != null) {
            bundle.put("externalDocs", openAPI.getExternalDocs());
        }
        for (int i = 0; i < allModels.size() - 1; i++) {
            HashMap<String, CodegenModel> cm = (HashMap<String, CodegenModel>) allModels.get(i);
            CodegenModel m = cm.get("model");
            m.getVendorExtensions().put(CodegenConstants.HAS_MORE_MODELS_EXT_NAME, Boolean.TRUE);
        }

        plugins.postProcessSupportingFileData(bundle);

        if (System.getProperty("debugSupportingFiles") != null) {
            LOGGER.info("############ Supporting file info ############");
            Json.prettyPrint(bundle);
        }
        return bundle;
    }

    protected void generateSupportingFiles(List<File> files, Map<String, Object> bundle) {

        Set<String> supportingFilesToGenerate = null;
        String supportingFiles = System.getProperty(CodegenConstants.SUPPORTING_FILES);
        boolean generateAll = false;
        if (supportingFiles != null && supportingFiles.equalsIgnoreCase("true")) {
            generateAll = true;
        } else if (supportingFiles != null && !supportingFiles.isEmpty()) {
            supportingFilesToGenerate = new HashSet<>(Arrays.asList(supportingFiles.split(",")));
        }

        for (SupportingFile support : plugins.supportingFiles()) {
            try {
                String outputFolder = plugins.outputFolder();
                if (StringUtils.isNotEmpty(support.folder)) {
                    outputFolder += File.separator + support.folder;
                }
                File of = new File(outputFolder);
                if (!of.isDirectory()) {
                    of.mkdirs();
                }
                String outputFilename = outputFolder + File.separator + support.destinationFilename.replace('/', File.separatorChar);
                if (!plugins.shouldOverwrite(outputFilename)) {
                    LOGGER.info("Skipped overwriting " + outputFilename);
                    continue;
                }
                String templateFile;
                if (support instanceof GlobalSupportingFile) {
                    templateFile = config.getCommonTemplateDir() + File.separator + support.templateFile;
                } else {
                    templateFile = getFullTemplateFile(plugins, support.templateFile);
                }

                boolean shouldGenerate = true;
                if (!generateAll && supportingFilesToGenerate != null && !supportingFilesToGenerate.isEmpty()) {
                    shouldGenerate = supportingFilesToGenerate.contains(support.destinationFilename);
                }
                if (!shouldGenerate) {
                    continue;
                }

                if (ignoreProcessor.allowsFile(new File(outputFilename))) {
                    if (templateFile.endsWith("mustache")) {
                        String rendered = templateEngine.getRendered(templateFile, bundle);
                        writeToFile(outputFilename, rendered);
                        files.add(new File(outputFilename));
                    } else {
                        InputStream in = null;

                        try {
                            in = new FileInputStream(templateFile);
                        } catch (Exception e) {
                            // continue
                        }
                        if (in == null) {
                            in = this.getClass().getClassLoader().getResourceAsStream(getCPResourcePath(templateFile));
                        }
                        File outputFile = new File(outputFilename);
                        OutputStream out = new FileOutputStream(outputFile, false);
                        if (in != null) {
                            LOGGER.info("writing file " + outputFile);
                            IOUtils.copy(in, out);
                            out.close();
                        } else {
                            LOGGER.warn("can't open " + templateFile + " for input");
                        }
                        files.add(outputFile);
                    }
                } else {
                    LOGGER.info("Skipped generation of " + outputFilename + " due to rule in .swagger-codegen-ignore");
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not generate supporting file '" + support + "'", e);
            }
        }

        // Consider .swagger-codegen-ignore a supporting file
        // Output .swagger-codegen-ignore if it doesn't exist and wasn't explicitly created by a generator
        final String swaggerCodegenIgnore = ".swagger-codegen-ignore";
        String ignoreFileNameTarget = plugins.outputFolder() + File.separator + swaggerCodegenIgnore;
        File ignoreFile = new File(ignoreFileNameTarget);
        if (generateSwaggerMetadata && !ignoreFile.exists()) {
            String ignoreFileNameSource = File.separator + plugins.getCommonTemplateDir() + File.separator + swaggerCodegenIgnore;
            String ignoreFileContents = readResourceContents(ignoreFileNameSource);
            try {
                writeToFile(ignoreFileNameTarget, ignoreFileContents);
            } catch (IOException e) {
                throw new RuntimeException("Could not generate supporting file '" + swaggerCodegenIgnore + "'", e);
            }
            files.add(ignoreFile);
        }

        if (generateSwaggerMetadata) {
            final String swaggerVersionMetadata = plugins.outputFolder() + File.separator + ".swagger-codegen" + File.separator + "VERSION";
            File swaggerVersionMetadataFile = new File(swaggerVersionMetadata);
            try {
                writeToFile(swaggerVersionMetadata, ImplementationVersion.read());
                files.add(swaggerVersionMetadataFile);
            } catch (IOException e) {
                throw new RuntimeException("Could not generate supporting file '" + swaggerVersionMetadata + "'", e);
            }
        }

        /*
         * The following code adds default LICENSE (Apache-2.0) for all generators
         * To use license other than Apache2.0, update the following file:
         *   modules/swagger-codegen/src/main/resources/_common/LICENSE
         *
        final String apache2License = "LICENSE";
        String licenseFileNameTarget = config.outputFolder() + File.separator + apache2License;
        File licenseFile = new File(licenseFileNameTarget);
        String licenseFileNameSource = File.separator + config.getCommonTemplateDir() + File.separator + apache2License;
        String licenseFileContents = readResourceContents(licenseFileNameSource);
        try {
            writeToFile(licenseFileNameTarget, licenseFileContents);
        } catch (IOException e) {
            throw new RuntimeException("Could not generate LICENSE file '" + apache2License + "'", e);
        }
        files.add(licenseFile);
         */

    }
    protected void generateApis(List<File> files, List<Object> allOperations, List<Object> allModels) {

        boolean hasModel = true;
        if (allModels == null || allModels.isEmpty()) {
            hasModel = false;
        }

        Map<String, List<CodegenOperation>> paths = processPaths(this.openAPI.getPaths());
        Set<String> apisToGenerate = null;
        String apiNames = System.getProperty("apis");
        if (apiNames != null && !apiNames.isEmpty()) {
            apisToGenerate = new HashSet<String>(Arrays.asList(apiNames.split(",")));
        }
        if (apisToGenerate != null && !apisToGenerate.isEmpty()) {
            Map<String, List<CodegenOperation>> updatedPaths = new TreeMap<>();
            for (String m : paths.keySet()) {
                if (apisToGenerate.contains(m)) {
                    updatedPaths.put(m, paths.get(m));
                }
            }
            paths = updatedPaths;
        }
        for (String tag : paths.keySet()) {
            try {
                List<CodegenOperation> ops = paths.get(tag);
                Collections.sort(ops, new Comparator<CodegenOperation>() {
                    @Override
                    public int compare(CodegenOperation one, CodegenOperation another) {
                        return ObjectUtils.compare(one.operationId, another.operationId);
                    }
                });
                Map<String, Object> operation = processOperations(plugins, tag, ops, allModels);


                operation.put("basePath", basePath);
                operation.put("basePathWithoutHost", basePathWithoutHost);
                operation.put("contextPath", contextPath);
                operation.put("baseName", tag);
                operation.put("modelPackage", plugins.modelPackage());
                operation.putAll(config.additionalProperties());
                operation.put("classname", plugins.toApiName(tag));
                operation.put("classVarName", plugins.toApiVarName(tag));
                operation.put("importPath", plugins.toApiImport(tag));
                operation.put("classFilename", plugins.toApiFilename(tag));

                if (!config.vendorExtensions().isEmpty()) {
                    operation.put("vendorExtensions", plugins.vendorExtensions());
                }

                // Pass sortParamsByRequiredFlag through to the Mustache template...
                boolean sortParamsByRequiredFlag = true;
                if (this.config.additionalProperties().containsKey(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG)) {
                    sortParamsByRequiredFlag = Boolean.valueOf(this.plugins.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG).toString());
                }
                operation.put("sortParamsByRequiredFlag", sortParamsByRequiredFlag);

                operation.put("hasModel", hasModel);


                allOperations.add(new HashMap<>(operation));
                for (int i = 0; i < allOperations.size(); i++) {
                    Map<String, Object> oo = (Map<String, Object>) allOperations.get(i);
                    if (i < (allOperations.size() - 1)) {
                        oo.put("hasMore", "true");
                    }
                }

                for (String templateName : plugins.apiTemplateFiles().keySet()) {
                    String filename = plugins.apiFilename(templateName, tag);
                    if (!config.shouldOverwrite(filename) && new File(filename).exists()) {
                        LOGGER.info("Skipped overwriting " + filename);
                        continue;
                    }

                    File written = processTemplateToFile(operation, templateName, filename);
                    if (written != null) {
                        files.add(written);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Could not generate api file for '" + tag + "'", e);
            }
        }
        if (System.getProperty("debugOperations") != null) {
            LOGGER.info("############ Operation info ############");
            Json.prettyPrint(allOperations);
        }

    }
    public Map<String, List<CodegenOperation>> processPaths(Paths paths) {
        Map<String, List<CodegenOperation>> ops = new TreeMap<>();
        for (String resourcePath : paths.keySet()) {
            PathItem path = paths.get(resourcePath);
            processOperation(resourcePath, "get", path.getGet(), ops, path);
            processOperation(resourcePath, "head", path.getHead(), ops, path);
            processOperation(resourcePath, "put", path.getPut(), ops, path);
            processOperation(resourcePath, "post", path.getPost(), ops, path);
            processOperation(resourcePath, "delete", path.getDelete(), ops, path);
            processOperation(resourcePath, "patch", path.getPatch(), ops, path);
            processOperation(resourcePath, "options", path.getOptions(), ops, path);
        }
        return ops;
    }

    protected Map<String, Object> processOperations(Plugins plugins, String tag, List<CodegenOperation> ops, List<Object> allModels) {
        Map<String, Object> operations = new HashMap<>();
        Map<String, Object> objs = new HashMap<>();
        objs.put("classname", plugins.toApiName(tag));
        objs.put("pathPrefix", plugins.toApiVarName(tag));

        // check for operationId uniqueness
        Set<String> opIds = new HashSet<>();
        int counter = 0;
        for (CodegenOperation op : ops) {
            String opId = op.nickname;
            if (opIds.contains(opId)) {
                counter++;
                op.nickname += "_" + counter;
            }
            opIds.add(opId);
        }
        objs.put("operation", ops);

        operations.put("operations", objs);
        operations.put("package", plugins.apiPackage());


        Set<String> allImports = new TreeSet<>();
        for (CodegenOperation op : ops) {
            allImports.addAll(op.imports);
        }

        List<Map<String, String>> imports = new ArrayList<>();
        for (String nextImport : allImports) {
            Map<String, String> im = new LinkedHashMap<>();
            String mapping = plugins.importMapping().get(nextImport);
            if (mapping == null) {
                mapping = plugins.toVmodelImport(nextImport);
            }
            if (mapping != null) {
                im.put("import", mapping);
                imports.add(im);
            }
        }

        operations.put("imports", imports);

        // add a flag to indicate whether there's any {{import}}
        if (imports.size() > 0) {
            operations.put("hasImport", true);
        }
        plugins.postProcessOperations(operations);
        plugins.postProcessOperationsWithModels(operations, allModels);
        if (objs.size() > 0) {
            List<CodegenOperation> os = (List<CodegenOperation>) objs.get("operation");

            if (os != null && os.size() > 0) {
                CodegenOperation op = os.get(os.size() - 1);
                op.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.FALSE);
            }
        }
        return operations;
    }
    protected void processOperation(String resourcePath, String httpMethod, Operation operation, Map<String, List<CodegenOperation>> operations, PathItem path) {
        if (operation == null) {
            return;
        }
        if (System.getProperty("debugOperations") != null) {
            LOGGER.info("processOperation: resourcePath= " + resourcePath + "\t;" + httpMethod + " " + operation + "\n");
        }
        List<Tag> tags = new ArrayList<>();

        List<String> tagNames = operation.getTags();
        List<Tag> swaggerTags = this.openAPI.getTags();
        if (tagNames != null) {
            if (swaggerTags == null) {
                for (String tagName : tagNames) {
                    tags.add(new Tag().name(tagName));
                }
            } else {
                for (String tagName : tagNames) {
                    boolean foundTag = false;
                    for (Tag tag : swaggerTags) {
                        if (tag.getName().equals(tagName)) {
                            tags.add(tag);
                            foundTag = true;
                            break;
                        }
                    }

                    if (!foundTag) {
                        tags.add(new Tag().name(tagName));
                    }
                }
            }
        }

        if (tags.isEmpty()) {
            tags.add(new Tag().name("default"));
        }

        /*
         build up a set of parameter "ids" defined at the operation level
         per the swagger 2.0 spec "A unique parameter is defined by a combination of a name and location"
          i'm assuming "location" == "in"
        */
        Set<String> operationParameters = new HashSet<>();
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                operationParameters.add(generateParameterId(parameter));
            }
        }

        //need to propagate path level down to the operation
        if (path.getParameters() != null) {
            for (Parameter parameter : path.getParameters()) {
                //skip propagation if a parameter with the same name is already defined at the operation level
                if (!operationParameters.contains(generateParameterId(parameter)) && operation.getParameters() != null) {
                    operation.getParameters().add(parameter);
                }
            }
        }

        final Map<String, Schema> schemas = openAPI.getComponents() != null ? openAPI.getComponents().getSchemas() : null;
        for (Tag tag : tags) {
            try {
                CodegenOperation codegenOperation = plugins.fromOperation(resourcePath, httpMethod, operation, schemas, openAPI);
                codegenOperation.tags = new ArrayList<>(tags);
                plugins.addOperationToGroup(plugins.sanitizeTag(tag.getName()), resourcePath, operation, codegenOperation, operations);
                List<SecurityRequirement> securities = operation.getSecurity();
                if (securities != null && securities.isEmpty()) {
                    continue;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = "Could not process operation:\n" //
                    + "  Tag: " + tag + "\n"//
                    + "  Operation: " + operation.getOperationId() + "\n" //
                    + "  Resource: " + httpMethod + " " + resourcePath + "\n"//
                    // + "  Definitions: " + swagger.getDefinitions() + "\n"  //
                    + "  Exception: " + ex.getMessage();
                throw new RuntimeException(msg, ex);
            }
        }

    }

    protected static String generateParameterId(Parameter parameter) {
        return parameter.getName() + ":" + parameter.getIn();
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

}
