package io.swagger.codegen.v3.generators.modules.plugins.golang;

import io.swagger.codegen.v3.*;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class GolangGin extends BaseGolang {
    static Logger LOGGER = LoggerFactory.getLogger(GolangGin.class);

    public GolangGin() {
        super();
        //cleaning up cli before
        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Go package name (convention: lowercase).")
            .defaultValue("swagger"));
        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC)
            .defaultValue(Boolean.TRUE.toString()));

        //add vModel templates loader
        this.vModelTemplateFiles.put("vmodel.mustache", ".go");
        //add api-controller templates loader
        this.apiTemplateFiles.put("api-controller.mustache", ".go");
        //add entities templates loader
        this.entitiesTemplateFiles.put("entities.mustache", ".go");
        //add srevice templates loader
        this.serviceTemplateFiles.put("service.mustache", ".go");
        //add model templates loader
        this.modelTemplateFiles.put("model.mustache",".go");

        //add typing converter
        typeMapping.clear();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int64");
        typeMapping.put("number", "float32");
        typeMapping.put("float", "float32");
        typeMapping.put("double", "float64");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "string");
        typeMapping.put("UUID", "string");
        typeMapping.put("date", "string");
        typeMapping.put("DateTime", "time.Time");
        typeMapping.put("password", "string");
        typeMapping.put("File", "*os.File");
        typeMapping.put("file", "*os.File");
        // map binary to string as a workaround
        // the correct solution is to use []byte
        typeMapping.put("binary", "[]byte");
        typeMapping.put("ByteArray", "string");
        typeMapping.put("object", "interface{}");
        typeMapping.put("UUID", "string");
        typeMapping.put("Object", "interface{}");

    }

    //load configuration for operations
    @Override
    public void processOpts() {
        super.processOpts();
        if (StringUtils.isBlank(templateDir)) {
            embeddedTemplateDir = templateDir = getTemplateDir();
        }
        // set invokerPackage as basePackage
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
            additionalProperties.put(BASE_PACKAGE, basePackage);
            LOGGER.info("Set base package to invoker package (" + basePackage + ")");
        }
        //addsuporting files
        supportingFiles.add(new SupportingFile("routers.mustache", basePackage + File.separator + "src/controllers", "routers.go"));

    }

    //operationId on golang change into camelcase
    @Override
    public String toOperationId(String operationId) {
        String sanitizedOperationId = sanitizeName(operationId);
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(sanitizedOperationId)) {
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to "
                + camelize("call_" + operationId));
            sanitizedOperationId = "call_" + sanitizedOperationId;
        }
        return camelize(sanitizedOperationId);
    }

    @Override
    public String getName() {
        return "go-gin";
    }


    @Override
    public String getDefaultTemplateDir() {
        return "plugins/go-gin";
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/controllers";
    }

    @Override
    public String toApiFilename(String name) {
        return "controller_" + underscore(name);
    }

    @Override
    public String toVModelName(String name) {
        return "vmodel_" + underscore(name);
    }

    @Override
    public String vModelFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/vmodels";
    }

    @Override
    public String toEntitiesFilename(String name) {
        return "entities_" + underscore(name);
    }
    @Override
    public String toEntitiesName(String name) {
        return "entities_" + underscore(name);
    }

    @Override
    public String entitiesFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/entities";
    }

    @Override
    public String toModelFilename(String name) {
        return "model_" + underscore(name);
    }


    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/models";
    }

    @Override
    public String toModelImport(String name) {
        return null;
    }



    @Override
    //for checking type data declaration
    public String getTypeDeclaration(Schema schema) {
        if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            Schema inner = arraySchema.getItems();
            return "[]" + getTypeDeclaration(inner);
        } else if (schema instanceof MapSchema && hasSchemaProperties(schema)) {
            MapSchema mapSchema = (MapSchema) schema;
            Schema inner = (Schema) mapSchema.getAdditionalProperties();

            return getSchemaType(schema) + "[string]" + getTypeDeclaration(inner);
        }
        // Not using the supertype invocation, because we want to UpperCamelize
        // the type.
        String schemaType = getSchemaType(schema);
        if (typeMapping.containsKey(schemaType)) {
            return typeMapping.get(schemaType);
        }
        if (typeMapping.containsValue(schemaType)) {
            return schemaType;
        }
        if (languageSpecificPrimitives.contains(schemaType)) {
            return schemaType;
        }
        return toModelName(schemaType);
    }

    @Override
    //to variable all properties
    public String toVarName(String name) {
        return camelize(name);
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
                    } else {
                        codegenParameter.dataType = this.toModelImport(codegenParameter.dataType);
                    }
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
    public Map<String, String> createMapping(String key, String value) {
        Map<String, String> customImport = new HashMap<String, String>();
        customImport.put(key, value);

        return customImport;
    }
}
