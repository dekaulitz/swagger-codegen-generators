package io.swagger.codegen.v3.generators.nodejs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class ExpressJsCodegen extends NodeJSServerCodegen {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressJsCodegen.class);
    //for generating service file that related with api controller
    protected Boolean generateServicePackage = true;
    protected String servicePath = "src/models";
    protected String apiPath = "src/controllers";
    protected String configPath = "configurations";
    protected String servicePackage = "models";
    protected String repositoryPath = "src/repositories";
    protected String repositoryPackage = "repositories";
    public ExpressJsCodegen() {
        super();
        typeMapping.put("string", "String");
        typeMapping.put("integer", "Number");

        // set the output folder here
        outputFolder = "generated-code/gbo";
        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
            "controller.mustache",   // the template to use
            ".js");       // the extension for each file to write
        serviceApiTemplate.put("model-controller.mustache", ".js");
        repositoryFileTemplates.put("repository-api.mustache", ".js");

    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (StringUtils.isBlank(templateDir)) {
            embeddedTemplateDir = templateDir = getTemplateDir();
        }
        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("serverPort", serverPort);
        additionalProperties.put("apiPath", apiPath);
        additionalProperties.put("servicePath", servicePath);
        supportingFiles.add(new SupportingFile("package.mustache", "", "package.json"));
        supportingFiles.add(new SupportingFile("index.mustache", "", "app.js"));
        supportingFiles.add(new SupportingFile("swagger.mustache", "docs", "swagger.yml"));
        supportingFiles.add(new SupportingFile(".env.dev.mustache", "", ".env.dev"));
        supportingFiles.add(new SupportingFile("eslint.mustache", "", ".eslintrc.json"));
        supportingFiles.add(new SupportingFile("routers.mustache", apiPath, "routers.js"));
        supportingFiles.add(new SupportingFile("configuration.mustache", configPath, "index.js"));
        additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        additionalProperties.put(CodegenConstants.REPOSITORY_PACKAGE, repositoryPackage);
        additionalProperties.put(CodegenConstants.API_PACKAGE, apiPath);
        additionalProperties.put(CodegenConstants.SERVICE_PACKAGE, servicePackage);
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        writeOptional(outputFolder, new SupportingFile(".Dockerfile.mustache", "", ".Dockerfile"));
        writeOptional(outputFolder, new SupportingFile("docker-compose.mustache", "", "docker-compose.yml"));
    }

    @Override
    public Boolean generateService() {
        return true;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "express-js";
    }

    @Override
    public String getDefaultTemplateDir() {
        return "nodejs-express";
    }

    @Override
    public String apiPackage() {
        return apiPath;
    }

    @Override
    public String toServiceFileName(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // e.g. PetApi.go => pet_api.go
        return "model." + underscore(name);
    }

    @Override
    public String toApiFilename(String name) {
        return "controller." + underscore(name);
    }

    @Override
    public String toRepositoryFileName(String name) {
        return "repository." + underscore(name);
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    public String serviceFileFolder() {
        return outputFolder + File.separator + servicePath.replace('.', File.separatorChar);
    }
    @Override
    public String repositoryFileFolder() {
        return outputFolder + File.separator + repositoryPath.replace('.', File.separatorChar);
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        OpenAPI openAPI = (OpenAPI) objs.get("openAPI");
        if (openAPI != null) {
            try {
                objs.put("swagger-yaml", Yaml.mapper().writeValueAsString(openAPI));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }

    @Override
    public List<Map<String, String>> importServices(List<CodegenOperation> ops) {
        List<Map<String, String>> importServices = new ArrayList<>();
        Map<String, String> im = new LinkedHashMap<>();
        im.put("importService", servicePackage+"/"+toServiceFileName(underscore(ops.get(0).baseName)));
        im.put("operationId", ops.get(0).operationId);
        importServices.add(0,im);
        return importServices;
    }
}
