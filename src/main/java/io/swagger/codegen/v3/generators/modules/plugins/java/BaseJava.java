package io.swagger.codegen.v3.generators.modules.plugins.java;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
            return String.format("%s<%s>",instantiationTypes.get(getSchemaType(schema)),getTypeDeclaration(inner));
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
}
