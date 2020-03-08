package io.swagger.codegen.v3.generators.modules.plugins.java;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.codegen.v3.generators.modules.plugins.golang.GolangGin;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SpringBoot extends BaseJava {
    static Logger LOGGER = LoggerFactory.getLogger(SpringBoot.class);
    protected String projectFolder = "src" + File.separator + "main";
    protected String sourceFolder = projectFolder + File.separator + "java";

    @Override
    public String getName() {
        return "spring-boot";
    }

    public SpringBoot() {
        super();
        cliOptions.clear();
        //add vModel templates loader
        this.vModelTemplateFiles.put("vmodel.mustache", ".java");
        //add api-controller templates loader
        this.apiTemplateFiles.put("api-controller.mustache", ".java");
        //add entities templates loader
        this.entitiesTemplateFiles.put("entities.mustache", ".java");
        //add srevice templates loader
        this.serviceTemplateFiles.put("service.mustache", ".go");
        //add model templates loader
        this.modelTemplateFiles.put("model.mustache", ".go");

    }

    @Override
    public String getDefaultTemplateDir() {
        return "plugins/spring-boot";
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
            additionalProperties.put(ENTITIES_PACKAGE, basePackage + "." + entitiesPackage);
            additionalProperties.put(VMODEL_PACKAGE, basePackage + "." + vModelPackage);
            additionalProperties.put(SERVICE_PACKAGE, basePackage + "." + servicePackage);
            additionalProperties.put(API_PACKAGE, basePackage + "." + apiPackage);
            LOGGER.info("Set base package to invoker package (" + basePackage + ")");

        }
        //addsuporting files
//        supportingFiles.add(new SupportingFile("routers.mustache", basePackage + File.separator + "src/controllers", "routers.go"));

    }

    @Override
    public String apiPackage() {
        return getBasePakage() + super.apiPackage();
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + basePackage + File.separator + "controllers";
    }

    @Override
    public String toApiFilename(String name) {
        return"Controller" + camelize(name);
    }

    @Override
    public String toVModelName(String name) {
        return "Vmodel" + camelize(name);
    }

    @Override
    public String vModelFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + basePackage + File.separator + "vmodels";
    }

    @Override
    public String toEntitiesFilename(String name) {
        return "Entity" + camelize(name);
    }

    @Override
    public String toEntitiesName(String name) {
        return "Entity" + camelize(name);
    }

    @Override
    public String entitiesFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + basePackage + File.separator + "entities";
    }

    @Override
    public String toModelFilename(String name) {
        return "Model" + camelize(name);
    }


    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + basePackage + File.separator + "models";
    }

    @Override
    public String entitiesPackage() {
        return getBasePakage() + super.entitiesPackage();
    }

    @Override
    //to variable all properties
    public String toVarName(String name) {
        return camelize(name);
    }

    @Override
    public String toEntityImport(String name) {
        return null;
    }

    @Override
    public String toVmodelImport(String name) {
        return null;
    }
}
