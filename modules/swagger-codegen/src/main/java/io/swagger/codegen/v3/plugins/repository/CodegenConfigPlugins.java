package io.swagger.codegen.v3.plugins.repository;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.CodegenConstants.*;

public class CodegenConfigPlugins implements PluginsCodegenConfig {
    protected final Logger LOGGER = LoggerFactory.getLogger(CodegenConfigPlugins.class);
    protected String repositoryPackage = StringUtils.EMPTY;
    //template repository
    protected Map<String, String> repositoryFileTemplates = new HashMap<String, String>();

    @Override
    public String repositoryPackage() {
        return null;
    }

    @Override
    public String repositoryFileFolder() {
        return null;
    }

    @Override
    public String toRepositoryFileName(String name) {
        return null;
    }

    @Override
    public Map<String, String> repositoryTemplateFiles() {
        return null;
    }

    @Override
    public String repositoryFileName(String name, String tag) {
        return null;
    }

    @Override
    public String servicePackage() {
        return null;
    }

//
//    public void postProcessRepositories(CodegenModel codegenModel) {
//
//        codegenModel.repository = Json.mapper().convertValue(codegenModel.getVendorExtensions().get(X_REPOSITORY), CodeGenRepositoryModel.class);
//        CodeGenRepositoryModel repository = codegenModel.repository;
//        repository.vars = addRepositoryVars(codegenModel);
//    }

//    private List<CodegenProperty> addRepositoryVars(CodegenModel codegenModel) {
//        List<CodegenProperty> codegenProperties = null;
//        List<Map.Entry<String, Schema>> propertyList = new ArrayList<Map.Entry<String, Schema>>(codegenModel.getRepository().getProperties().entrySet());
//        final int totalCount = propertyList.size();
//        for (int i = 0; i < totalCount; i++) {
//            Map.Entry<String, Schema> entry = propertyList.get(i);
//
//            final String key = entry.getKey();
//            final Schema propertySchema = entry.getValue();
//
//            if (propertySchema == null) {
//                LOGGER.warn("null property for " + key);
//                continue;
//            }
//            final CodegenProperty codegenProperty = fromProperty(key, propertySchema);
//            if (propertySchema.get$ref() != null) {
//                if (this.openAPI == null) {
//                    LOGGER.warn("open api utility object was not properly set.");
//                } else {
//                    OpenAPIUtil.addPropertiesFromRef(this.openAPI, propertySchema, codegenProperty);
//                }
//            }
//
//            boolean hasRequired = getBooleanValue(codegenModel, HAS_REQUIRED_EXT_NAME) || codegenProperty.required;
//            boolean hasOptional = getBooleanValue(codegenModel, HAS_OPTIONAL_EXT_NAME) || !codegenProperty.required;
//
//            codegenModel.getVendorExtensions().put(HAS_REQUIRED_EXT_NAME, hasRequired);
//            codegenModel.getVendorExtensions().put(HAS_OPTIONAL_EXT_NAME, hasOptional);
//
//            boolean isEnum = getBooleanValue(codegenProperty, IS_ENUM_EXT_NAME);
//            if (isEnum) {
//                // FIXME: if supporting inheritance, when called a second time for allProperties it is possible for
//                // m.hasEnums to be set incorrectly if allProperties has enumerations but properties does not.
//                codegenModel.getVendorExtensions().put(CodegenConstants.HAS_ENUMS_EXT_NAME, true);
//            }
//
//            // set model's hasOnlyReadOnly to false if the property is read-only
//            if (!getBooleanValue(codegenProperty, CodegenConstants.IS_READ_ONLY_EXT_NAME)) {
//                codegenModel.getVendorExtensions().put(HAS_ONLY_READ_ONLY_EXT_NAME, Boolean.FALSE);
//            }
//
//            if (i + 1 != totalCount) {
//                codegenProperty.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.TRUE);
//                // check the next entry to see if it's read only
//                if (!Boolean.TRUE.equals(propertyList.get(i + 1).getValue().getReadOnly())) {
//                    codegenProperty.getVendorExtensions().put(CodegenConstants.HAS_MORE_NON_READ_ONLY_EXT_NAME, Boolean.TRUE);
//                }
//            }
//
//            if (getBooleanValue(codegenProperty, CodegenConstants.IS_CONTAINER_EXT_NAME)) {
//                addImport(codegenModel, typeMapping.get("array"));
//            }
//
//            addImport(codegenModel, codegenProperty.baseType);
//            CodegenProperty innerCp = codegenProperty;
//            while (innerCp != null) {
//                addImport(codegenModel, innerCp.complexType);
//                innerCp = innerCp.items;
//            }
//            vars.add(codegenProperty);
//
//            // if required, add to the list "requiredVars"
//            if (Boolean.TRUE.equals(codegenProperty.required)) {
//                codegenModel.requiredVars.add(codegenProperty);
//            } else { // else add to the list "optionalVars" for optional property
//                codegenModel.optionalVars.add(codegenProperty);
//            }
//
//            // if readonly, add to readOnlyVars (list of properties)
//            if (getBooleanValue(codegenProperty, CodegenConstants.IS_READ_ONLY_EXT_NAME)) {
//                codegenModel.readOnlyVars.add(codegenProperty);
//            } else { // else add to readWriteVars (list of properties)
//                // FIXME: readWriteVars can contain duplicated properties. Debug/breakpoint here while running C# generator (Dog and Cat models)
//                codegenModel.readWriteVars.add(codegenProperty);
//            }
//        }
//        }
//        return codegenProperties;
//    }
}
