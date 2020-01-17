package io.swagger.codegen.v3.plugins.repository;

import io.swagger.codegen.v3.*;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CodegenConfigPlugins implements PluginsCodegenConfig {
    protected final Logger LOGGER = LoggerFactory.getLogger(CodegenConfigPlugins.class);
    protected Boolean generateServicePackage = false;
    protected String outputFolder = StringUtils.EMPTY;
    protected String repositoryPackage = StringUtils.EMPTY;
    protected String servicePackage;
    //add model service related with api controller
    protected Map<String, String> serviceApiTemplate = new HashMap<String, String>();

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


    public Map<String, String> serviceApiTemplateFiles() {
        return serviceApiTemplate;
    }

    @Override
    public String serviceApiFileName(String templateName, String tag) {
        return null;
    }

    @Override
    public String serviceFileFolder() {
        return null;
    }

    @Override
    public List<String> serviceImportMapping(CodegenOperation codegenOperation, Tag tag) {
//     codegenOperation.importModelPackage.add(codegenOperation.getTags().get(0)));
        return null;
    }

    @Override
    public List<Map<String, String>> importServices(List<CodegenOperation> ops) {
        List<Map<String, String>> importServices = new ArrayList<>();
        Map<String, String> im = new LinkedHashMap<>();
        im.put("importService", ops.get(0).operationId);
        importServices.add(0,im);
        return importServices;
    }



    public Boolean generateService() {
        return generateServicePackage;
    }

    //fitting with new path :id
    protected String fittingPathParams(CodegenOperation operation) {
        String[] newReplacements = operation.path.split("/");
        int i = 0;
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
