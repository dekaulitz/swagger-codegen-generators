package io.swagger.codegen.v3.generators.modules.plugins.golang;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.swagger.codegen.v3.CodegenConstants.*;
import static io.swagger.codegen.v3.CodegenConstants.HAS_ONLY_READ_ONLY_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static io.swagger.codegen.v3.utils.ModelUtils.processCodegenModels;

public abstract class BaseGolang extends AbstractPlugin {

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

    public Map<String, String> createMapping(String key, String value) {
        Map<String, String> customImport = new HashMap<String, String>();
        customImport.put(key, value);

        return customImport;
    }
}
