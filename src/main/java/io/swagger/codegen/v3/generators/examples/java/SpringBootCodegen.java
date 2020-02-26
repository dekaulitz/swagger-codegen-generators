package io.swagger.codegen.v3.generators.examples.java;

import io.swagger.codegen.v3.*;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class SpringBootCodegen extends AbstractJavaCodegen {
    static Logger LOGGER = LoggerFactory.getLogger(SpringBootCodegen.class);
    public static final String CONFIG_PACKAGE = "configPackage";
    public static final String BASE_PACKAGE = "basePackage";
    protected String configPackage = "io.swagger.configuration";
    protected String basePackage = "io.swagger";
    protected String vModels = "vmodels";
    protected String repositoryPackage = "entities";
    public SpringBootCodegen() {
        super();
        additionalProperties.put("jackson", "true");

        cliOptions.add(new CliOption(CONFIG_PACKAGE, "configuration package for generated code"));
        cliOptions.add(new CliOption(BASE_PACKAGE, "base package (invokerPackage) for generated code"));
        this.setApiPackage("controllers");

    }


    @Override
    public CodegenType getTag() {
        return null;
    }

    @Override
    public String getName() {
        return "spring-boot";
    }

    @Override
    public String getHelp() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultTemplateDir() {
        return "JavaSpringBoot";
    }

    @Override
    public void processOpts() {
        if (StringUtils.isBlank(templateDir)) {
            embeddedTemplateDir = templateDir = getTemplateDir();
        }

        // set invokerPackage as basePackage
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
            additionalProperties.put(BASE_PACKAGE, basePackage);
            LOGGER.info("Set base package to invoker package (" + basePackage + ")");
        }
        additionalProperties.put(CodegenConstants.REPOSITORY_PACKAGE, basePackage+"."+repositoryPackage);
        this.setModelPackage(basePackage + "." + "vmodels");
        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
        modelTemplateFiles.put(
            "vmodel.mustache",
            ".java");
        apiTemplateFiles.put(
            "controller-api.mustache",   // the template to use
            ".java");       // the extension for each file to write

        repositoryFileTemplates.put("repository-api.mustache", ".java");
        OpenAPI open=this.openAPI;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        if ("null".equals(property.example)) {
            property.example = null;
        }
    }

    @Override
    public String toModelImport(String name) {
        return basePackage + "." + vModels.replace('.', '/') + "."  + name;
    }


    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);
        //Add imports for Jackson
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
            if (Boolean.TRUE.equals(isEnum) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }


    @Override
    public String toApiFilename(String name) {
        return "Controller" + name;
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + basePackage + "/" + apiPackage().replace('.', '/');
    }

    @Override
    public String toApiName(String name) {
        return camelize(name);
    }


    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + basePackage + "/" + vModels.replace('.', '/');
    }
    @Override
    public String repositoryFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + basePackage + "/" + repositoryPackage.replace('.', '/');
    }

    @Override
    public String toRepositoryFileName(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // e.g. PetApi.go => pet_api.go
        return camelize(name);
    }

    public void setBasePackage(String configPackage) {
        this.basePackage = configPackage;
    }
}
