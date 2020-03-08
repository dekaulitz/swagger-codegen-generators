package io.swagger.codegen.v3.generators.modules.plugins.java;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.examples.ExampleGenerator;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static io.swagger.codegen.v3.utils.ModelUtils.processCodegenModels;

public abstract class BaseJava extends AbstractPlugin {
    static Logger LOGGER = LoggerFactory.getLogger(BaseJava.class);

    public BaseJava() {
        super();
        languageSpecificPrimitives = new HashSet<String>(
            Arrays.asList(
                "String",
                "boolean",
                "Boolean",
                "Double",
                "Integer",
                "Long",
                "Float",
                "Object",
                "byte[]")
        );
        instantiationTypes.put("array", "ArrayList");
        instantiationTypes.put("map", "HashMap");
        typeMapping.put("date", "Date");
        typeMapping.put("file", "File");
        typeMapping.put("binary", "File");
    }

    @Override
    public String escapeReservedWord(String name) {
        // Can't start with an underscore, as our fields need to start with an
        // UppercaseLetter so that Go treats them as public/visible.

        // Options?
        // - MyName
        // - AName
        // - TheName
        // - XName
        // - X_Name
        // ... or maybe a suffix?
        // - Name_ ... think this will work.
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return camelize(name) + '_';
    }

    public Map<String, Object> postProcessAllEntities(Map<String, Object> processedModels) {
        // Index all CodegenModels by model name.
        Map<String, CodegenModel> allModels = new HashMap<>();
        for (Map.Entry<String, Object> entry : processedModels.entrySet()) {
            String modelName = toModelName(entry.getKey());
            Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            for (Map<String, Object> mo : models) {
                CodegenModel codegenModel = (CodegenModel) mo.get("model");
                //checking
                if (codegenModel.getVendorExtensions().get("x-repository-primary") != null) {
                    final List<CodegenProperty> enList = new ArrayList<>();
                    LinkedHashMap<String, Object> lnk = (LinkedHashMap<String, Object>) codegenModel.getVendorExtensions().get("x-repository-primary");
                    for (Map.Entry cb : lnk.entrySet()) {
                        Schema sc = Json.mapper().convertValue(cb.getValue(), Schema.class);
                        final CodegenProperty codegenProperty = fromProperty(cb.getKey().toString(), sc);
                        enList.add(codegenProperty);
                    }
                    mo.put("x-repository-primary", enList);
                }
                if (codegenModel.getVendorExtensions().get("x-repository-table") != null) {
                    final List<CodegenProperty> enList = new ArrayList<>();
                    LinkedHashMap<String, Object> lnk = (LinkedHashMap<String, Object>) codegenModel.getVendorExtensions().get("x-repository-table");
                    for (Map.Entry cb : lnk.entrySet()) {
                        Schema sc = Json.mapper().convertValue(cb.getValue(), Schema.class);
                        final CodegenProperty codegenProperty = fromProperty(underscore(cb.getKey().toString()), sc);
                        enList.add(codegenProperty);
                    }
                    mo.put("x-repository-table", enList);
                }
                if (codegenModel.getVendorExtensions().get("x-repository-time") != null) {
                    final List<CodegenProperty> enList = new ArrayList<>();
                    LinkedHashMap<String, Object> lnk = (LinkedHashMap<String, Object>) codegenModel.getVendorExtensions().get("x-repository-time");
                    for (Map.Entry cb : lnk.entrySet()) {
                        Schema sc = Json.mapper().convertValue(cb.getValue(), Schema.class);
                        final CodegenProperty codegenProperty = fromProperty(underscore(cb.getKey().toString()), sc);
                        enList.add(codegenProperty);
                    }
                    ArrayList<Map<String, Object>> moImport = (ArrayList<Map<String, Object>>) ((Map<String, Object>) entry.getValue()).get("imports");
                    Map<String, Object> importTiem = new HashMap<>();
                    importTiem.put("import", "java.util.Date");
                    moImport.add(importTiem);
                    mo.put("x-repository-time", enList);
                }
                allModels.put(modelName, codegenModel);
            }
        }
        if (supportsInheritance) {
            processCodegenModels(allModels);
        }
        for (String modelName : allModels.keySet()) {
            final CodegenModel codegenModel = allModels.get(modelName);
            if (!codegenModel.vendorExtensions.containsKey("x-is-composed-model")) {
                continue;
            }
            List<String> modelNames = (List<String>) codegenModel.vendorExtensions.get("x-model-names");
            if (modelNames == null || modelNames.isEmpty()) {
                continue;
            }
            for (String name : modelNames) {
                final CodegenModel model = allModels.get(name);
                if (model == null) {
                    continue;
                }
                if (model.interfaceModels == null) {
                    model.interfaceModels = new ArrayList<>();
                }
                if (!model.interfaceModels.stream().anyMatch(value -> value.name.equalsIgnoreCase(modelName))) {
                    model.interfaceModels.add(codegenModel);
                }
            }
        }
        return processedModels;
    }

    @Override
    //for checking type data declaration
    public String getTypeDeclaration(Schema schema) {
        if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            Schema inner = arraySchema.getItems();
            if (inner == null) {
                LOGGER.warn(arraySchema.getName() + "(array property) does not have a proper inner type defined");
                // TODO maybe better defaulting to StringProperty than returning null
                return null;
            }
            return String.format("%s<%s>", instantiationTypes.get(getSchemaType(schema)), getTypeDeclaration(inner));
            // return getSwaggerType(propertySchema) + "<" + getTypeDeclaration(inner) + ">";
        } else if (schema instanceof MapSchema && hasSchemaProperties(schema)) {
            Schema inner = (Schema) schema.getAdditionalProperties();
            if (inner == null) {
                LOGGER.warn(schema.getName() + "(map property) does not have a proper inner type defined");
                // TODO maybe better defaulting to StringProperty than returning null
                return null;
            }
            return getSchemaType(schema) + "<String, " + getTypeDeclaration(inner) + ">";
        } else if (schema instanceof MapSchema && hasTrueAdditionalProperties(schema)) {
            Schema inner = new ObjectSchema();
            return getSchemaType(schema) + "<String, " + getTypeDeclaration(inner) + ">";
        }
        String schemaType = getSchemaType(schema);
        String refSchema = schema.get$ref();
        if (refSchema != null) {
            String schemaEntities = "#/components/x-entities/";
            String schemaVmodel = "#/components/schemas/";
            if (refSchema.substring(schemaEntities.length()).equalsIgnoreCase(schemaType)) {
                return toEntitiesName(schemaType);
            } else if (refSchema.substring(schemaVmodel.length()).equalsIgnoreCase(schemaType)) {
                return toVModelName(schemaType);
            }
        }

        return toModelName(schemaType);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
//            operation.setReturnType(this.toModelImport(operation.getReturnType()));
            operation.bodyParams.forEach(codegenParameter -> {
                codegenParameter.dataType=toVModelName(operation.bodyParam.baseType);
//                if (codegenParameter.getItems() != null)
//                    if (codegenParameter.getItems().containerType.equals("array")) {
//                        codegenParameter.dataType = codegenParameter.dataType.replace("[]", "[]vmodels.");
//                    } else {
//                        codegenParameter.dataType = "vmodels." + codegenParameter.dataType;
//                    }
//                else
//                    codegenParameter.dataType = "vmodels." + codegenParameter.dataType;
            });
            if (operation.returnBaseType != null) {
                if (this.openAPI.getComponents().getSchemas().get(operation.returnBaseType) != null) {
                    operation.returnBaseType = toVarName(operation.returnBaseType);
                }
            }
        }
        Set<String> importOperation = new HashSet<>();
        // remove model imports to avoid error
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");

        Iterator<Map<String, String>> iterator = imports.iterator();
        while (iterator.hasNext()) {
            String _import = iterator.next().get("import");
            if (_import.startsWith(apiPackage()))
                iterator.remove();
        }

        boolean addedOptionalImport = false;
        boolean addedTimeImport = false;
        boolean addedOSImport = false;



        for (CodegenOperation operation : operations) {
//            for (CodegenParameter param : operation.allParams) {
//                // import "os" if the operation uses files
//                if (!addedOSImport && param.dataType == "*os.File") {
//                    imports.add(createMapping("import", "os"));
//                    addedOSImport = true;
//                }
//
//                // import "time" if the operation has a required time parameter.
//                if (param.required) {
//                    if (!addedTimeImport && param.dataType == "time.Time") {
//                        imports.add(createMapping("import", "time"));
//                        addedTimeImport = true;
//                    }
//                }
//
//                // import "optionals" package if the parameter is primitive and optional
//                if (!param.required && getBooleanValue(param, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME)) {
//                    // We need to specially map Time type to the optionals package
//                    if (param.dataType == "time.Time") {
//                        param.vendorExtensions.put("x-optionalDataType", "Time");
//                        continue;
//                    }
//                    // Map optional type to dataType
//                    param.vendorExtensions.put("x-optionalDataType", param.dataType.substring(0, 1).toUpperCase() + param.dataType.substring(1));
//                }
//            }

            if ( operation.returnBaseType != null) {
                importOperation.add("import "+this.getBasePakage() +".vmodels."+operation.returnType);
            }
            if(operation.bodyParam != null ){
                importOperation.add("import "+this.getBasePakage() +".vmodels."+toVModelName(operation.bodyParam.baseType));
            }
            if(operation.returnType!=null){
                importOperation.add("import "+this.getBasePakage() +".vmodels."+operation.returnType);
            }

        }
        importOperation.forEach(s -> {
            imports.add(createMapping("import", s));
        });

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

    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
        CodegenOperation codegenOperation = super.fromOperation(path, httpMethod, operation, schemas, openAPI);

        return codegenOperation;
    }

    public Map<String, String> createMapping(String key, String value) {
        Map<String, String> customImport = new HashMap<String, String>();
        customImport.put(key, value);

        return customImport;
    }
}
