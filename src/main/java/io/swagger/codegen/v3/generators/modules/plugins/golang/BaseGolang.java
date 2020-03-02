package io.swagger.codegen.v3.generators.modules.plugins.golang;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static io.swagger.codegen.v3.CodegenConstants.*;
import static io.swagger.codegen.v3.CodegenConstants.HAS_ONLY_READ_ONLY_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static io.swagger.codegen.v3.utils.ModelUtils.processCodegenModels;

public abstract class BaseGolang extends AbstractPlugin {
    protected final String vmodelDirName = "src/vmodels";

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reserved words
     *
     * @return the escaped term
     */
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

    protected boolean needToImport(String type) {
        return StringUtils.isNotBlank(type) && !defaultIncludes.contains(type)
            && !languageSpecificPrimitives.contains(type);
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
                    importTiem.put("import", "time");
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
    public Map<String, Object> postProcessEntities(Map<String, Object> objs) {
        // remove model imports to avoid error
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        final String prefix = modelPackage();
        Iterator<Map<String, String>> iterator = imports.iterator();
        while (iterator.hasNext()) {
            String _import = iterator.next().get("import");
            if (_import.startsWith(prefix))
                iterator.remove();
        }

        boolean addedTimeImport = false;
        boolean addedOSImport = false;
        List<Map<String, Object>> models = (List<Map<String, Object>>) objs.get("models");
        for (Map<String, Object> m : models) {
            Object v = m.get("model");
            if (v instanceof CodegenModel) {
                CodegenModel model = (CodegenModel) v;
                for (CodegenProperty param : model.vars) {
                    if (!addedTimeImport && param.baseType == "time.Time") {
                        imports.add(createMapping("import", "time"));
                        addedTimeImport = true;
                    }
                    if (!addedOSImport && param.baseType == "*os.File") {
                        imports.add(createMapping("import", "os"));
                        addedOSImport = true;
                    }
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

        return postProcessModelsEnum(objs);
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

    public Map<String, String> createMapping(String key, String value) {
        Map<String, String> customImport = new HashMap<String, String>();
        customImport.put(key, value);

        return customImport;
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
                        codegenParameter.dataType = "vmodels." + codegenParameter.dataType;
                    }
                else
                    codegenParameter.dataType = "vmodels." + codegenParameter.dataType;
            });
            operation.path = this.fittingPathWithGinFormat(operation);
            if (operation.returnBaseType != null) {
                if (this.openAPI.getComponents().getSchemas().get(operation.returnBaseType) != null) {
                    operation.returnBaseType = "vmodels." + toVarName(operation.returnBaseType);
                }
            }
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

        boolean addedOptionalImport = false;
        boolean addedTimeImport = false;
        boolean addedOSImport = false;
        Set<String> importOperation=new HashSet<>();
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
                    // We need to specially map Time type to the optionals package
                    if (param.dataType == "time.Time") {
                        param.vendorExtensions.put("x-optionalDataType", "Time");
                        continue;
                    }
                    // Map optional type to dataType
                    param.vendorExtensions.put("x-optionalDataType", param.dataType.substring(0, 1).toUpperCase() + param.dataType.substring(1));
                }
            }

            if(operation.bodyParam!=null || operation.returnBaseType !=null) {
                importOperation.add(this.getBasePakage() + File.separator + this.vmodelDirName);
            }

        }
        imports.add(createMapping("import", (String) importOperation.toArray()[0]));
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
