package io.swagger.codegen.v3.generators.go;

import io.swagger.codegen.v3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

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
    protected String apiPath = "src/controllers";
    protected String vmodels = "src/vmodels";
    protected String utils = "utils";
    protected String configPath = "configurations";
    protected String modelPackage = "vmodels";
    protected String apiPackage = "controllers";
    protected String vModels = "vmodels";

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
        supportingFiles.add(new SupportingFile("config.mustache", configPath, "configuration.go"));
        supportingFiles.add(new SupportingFile("config-host.mustache", configPath, "host_configuration.go"));
        supportingFiles.add(new SupportingFile("config.mustache", configPath, "configuration.go"));

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


    @Override
    public String modelPackage() {
        return vModels;
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
            operation.setReturnType(this.toModelImport(operation.getReturnType()));
            operation.bodyParams.forEach(codegenParameter -> {
                if (codegenParameter.getItems() != null)
                    if (codegenParameter.getItems().containerType.equals("array")) {
                        codegenParameter.dataType = codegenParameter.dataType.replace("[]", "[]vmodels.");
                    } else
                        codegenParameter.dataType = this.toModelImport(codegenParameter.dataType);
                else
                    codegenParameter.dataType = this.toModelImport(codegenParameter.dataType);
            });
            operation.path = this.fittingPathWithGinFormat(operation);
            operation.headerParams.forEach(codegenParameter -> {

            });
        }

        // remove model imports to avoid error
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        if (imports == null)
            return objs;

        Iterator<Map<String, String>> iterator = imports.iterator();
        while (iterator.hasNext()) {
            String _import = iterator.next().get("import");
            if (_import.startsWith(apiPackage()))
                iterator.remove();
        }

        // this will only import "fmt" if there are items in pathParams
        for (CodegenOperation operation : operations) {
            if (operation.pathParams != null && operation.pathParams.size() > 0) {
                imports.add(createMapping("import", "fmt"));
                break; //just need to import once
            }
        }

        boolean addedOptionalImport = false;
        boolean addedTimeImport = false;
        boolean addedOSImport = false;
        for (CodegenOperation operation : operations) {
            for (CodegenParameter param : operation.allParams) {
                // import "os" if the operation uses files
                if (!addedOSImport && param.dataType == "*os.File") {
                    imports.add(createMapping("import", "os"));
                    addedOSImport = true;
                }

                // import "time" if the operation has a required time parameter.
                if (param.required) {
                    if (!addedTimeImport && param.dataType == "time.Time") {
                        imports.add(createMapping("import", "time"));
                        addedTimeImport = true;
                    }
                }

                // import "optionals" package if the parameter is primitive and optional
                if (!param.required && getBooleanValue(param, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME)) {
                    if (!addedOptionalImport) {
                        imports.add(createMapping("import", "github.com/antihax/optional"));
                        addedOptionalImport = true;
                    }
                    // We need to specially map Time type to the optionals package
                    if (param.dataType == "time.Time") {
                        param.vendorExtensions.put("x-optionalDataType", "Time");
                        continue;
                    }
                    // Map optional type to dataType
                    param.vendorExtensions.put("x-optionalDataType", param.dataType.substring(0, 1).toUpperCase() + param.dataType.substring(1));
                }
            }
        }

        // recursively add import for mapping one type to multiple imports
        List<Map<String, String>> recursiveImports = (List<Map<String, String>>) objs.get("imports");
        if (recursiveImports == null)
            return objs;

        ListIterator<Map<String, String>> listIterator = imports.listIterator();
        while (listIterator.hasNext()) {
            String _import = listIterator.next().get("import");
            // if the import package happens to be found in the importMapping (key)
            // add the corresponding import package to the list
            if (importMapping.containsKey(_import)) {
                listIterator.add(createMapping("import", importMapping.get(_import)));
            }
        }

        return objs;
    }

    //fitting with go gin new path
    private String fittingPathWithGinFormat(CodegenOperation operation) {
        String[] newReplacements = operation.path.split("/");
        int i = 0;
        boolean itsSame = false;
        for (String newReplacement : newReplacements) {
            if (!operation.pathParams.isEmpty()) {
                for (CodegenParameter codegenParameter : operation.pathParams) {
                    if (newReplacement.matches(".*" + codegenParameter.paramName + "*.")) {
                        newReplacements[i] = ":" + codegenParameter.paramName;
                    }
                }
            }
            i++;
        }
        return String.join("/", newReplacements);
    }
}
