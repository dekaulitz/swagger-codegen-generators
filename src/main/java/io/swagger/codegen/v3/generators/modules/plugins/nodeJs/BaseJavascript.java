package io.swagger.codegen.v3.generators.modules.plugins.nodeJs;

import com.google.common.base.Strings;
import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;

import static io.swagger.codegen.v3.utils.ModelUtils.processCodegenModels;

public abstract class BaseJavascript extends AbstractPlugin {
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
    public Map<String, Object> postProcessAllVModels(Map<String, Object> processedModels) {
        // Index all CodegenModels by model name.
        return processedModels;
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

        String refSchema = schema.get$ref();
        if (refSchema != null) {
            String schemaEntities = "#/components/x-entities/";
            String type = refSchema.substring(schemaEntities.length());
            if (type.equalsIgnoreCase(schemaType)) {
                return schemaType + "." + camelize(schemaType);
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
            operation.httpMethod = operation.httpMethod.toLowerCase();
            operation.path = this.fittingWithExpressFormat(operation);
            operation.headerParams.forEach(codegenParameter -> {
                codegenParameter.baseName = codegenParameter.baseName.toLowerCase();
            });
            List<CodegenParameter> params = operation.allParams;
            if (params != null && params.size() == 0) {
                operation.allParams = null;
            }

            List<CodegenResponse> responses = operation.responses;
            if (responses != null) {
                for (CodegenResponse resp : responses) {
                    if ("0".equals(resp.code)) {
                        resp.code = "default";
                    }
                }
            }
            if (operation.examples != null && !operation.examples.isEmpty()) {
                // Leave application/json* items only
                for (Iterator<Map<String, String>> it = operation.examples.iterator(); it.hasNext(); ) {
                    final Map<String, String> example = it.next();
                    final String contentType = example.get("contentType");
                    if (contentType == null || !contentType.startsWith("application/json")) {
                        it.remove();
                    }
                }
            }
        }
        return objs;
    }


    //fitting with go gin new path
    private String fittingWithExpressFormat(CodegenOperation operation) {
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
