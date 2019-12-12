package io.swagger.codegen.v3.generators.go;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.openapi.OpenAPIYamlGenerator;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import jdk.nashorn.internal.ir.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * <p>GoGinServerCodegen class.</p>
 *
 * @author ovo
 * @version $Id: $Id
 */
public class GoGinServerCodegen extends AbstractGoCodegen {
    protected String packageName = "tokita";
    protected String apiVersion = "1.0.0";
    protected int serverPort = 8080;
    protected String projectName = "swagger-server";
    protected String apiPath = "apps/controllers";
    protected String vmodels = "apps/vmodels";
    protected String utils = "utils";
    protected String modelPackage = "vmodels";
    protected String apiPackage = "controllers";

    /**
     * <p>Constructor for GoGinServerCodegen.</p>
     */
    public GoGinServerCodegen() {
        super();

        // set the output folder here
        outputFolder = "generated-code/gbo";

        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put(
            "vmodel.mustache",
            ".go");

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
            "controller-api.mustache",   // the template to use
            ".go");       // the extension for each file to write

        /*
         * Reserved words.  Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
            Arrays.asList(
                // data type
                "string", "bool", "uint", "uint8", "uint16", "uint32", "uint64",
                "int", "int8", "int16", "int32", "int64", "float32", "float64",
                "complex64", "complex128", "rune", "byte", "uintptr",

                "break", "default", "func", "interface", "select",
                "case", "defer", "go", "map", "struct",
                "chan", "else", "goto", "package", "switch",
                "const", "fallthrough", "if", "range", "type",
                "continue", "for", "import", "return", "var", "error", "nil")
            // Added "error" as it's used so frequently that it may as well be a keyword
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultTemplateDir() {
        return "go-gin";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processOpts() {
        super.processOpts();

        if (StringUtils.isBlank(templateDir)) {
            embeddedTemplateDir = templateDir = getTemplateDir();
        }

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        } else {
            setPackageName("swagger");
        }

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("serverPort", serverPort);
        additionalProperties.put("apiPath", apiPath);
        additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);


        supportingFiles.add(new SupportingFile("gopkg.mustache", "", "Gopkg.toml"));
        supportingFiles.add(new SupportingFile("main.mustache", "", "main.go"));
        supportingFiles.add(new SupportingFile("routers.mustache", apiPath, "routers.go"));
        supportingFiles.add(new SupportingFile("logger.mustache", utils, "logger.go"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String apiPackage() {
        return apiPath;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Configures the type of generator.
     *
     * @see io.swagger.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     */
    @Override
    public String getName() {
        return "go-gin";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     */
    @Override
    public String getHelp() {
        return "Generates a Go server library using the swagger-tools project.  By default, " +
            "it will also generate service classes--which you can disable with the `-Dnoservice` environment variable.";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String toApiFilename(String name) {

        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // e.g. PetApi.go => pet_api.go
        return "controller_" + underscore(name);
    }

    @Override
    public String toModelFilename(String name) {
        return toModel("vmodel_" + name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + vmodels.replace('.', File.separatorChar);
    }

    /**
     * @param objs
     * @return
     * @desc generating swagger docs automaticaly
     */
    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        OpenAPI openAPI = (OpenAPI) objs.get("openAPI");
        if (openAPI != null) {
            OpenAPIYamlGenerator test = new OpenAPIYamlGenerator();
            test.setOutputDir(outputFolder + File.separator + "docs");
            test.preprocessOpenAPI(openAPI);
        }

        return super.postProcessSupportingFileData(objs);
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {

        return super.fromOperation(path, httpMethod, operation, schemas, openAPI);
    }
}
