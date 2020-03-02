package io.swagger.codegen.v3.generators.modules.plugins.nodeJs;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.codegen.v3.generators.modules.plugins.golang.GolangGin;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ExpressJs extends BaseJavascript {
    static Logger LOGGER = LoggerFactory.getLogger(ExpressJs.class);


    public ExpressJs() {
        super();
        cliOptions.clear();
        //add vModel templates loader
        this.vModelTemplateFiles.put("vmodel.mustache", ".go");
        //add api-controller templates loader
        this.apiTemplateFiles.put("api-controller.mustache", ".go");
        //add entities templates loader
        this.entitiesTemplateFiles.put("entities.mustache", ".js");
        //add srevice templates loader
        this.serviceTemplateFiles.put("service.mustache", ".go");
        //add model templates loader
        this.modelTemplateFiles.put("model.mustache", ".go");

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

    @Override
    public String getName() {
        return "express-js";
    }
    @Override
    public String getDefaultTemplateDir() {
        return "plugins/express-js";
    }

    @Override
    public String toEntitiesFilename(String name) {
        return "entity." + underscore(name);
    }
    @Override
    public String toEntitiesName(String name) {
        return "entity." + underscore(name);
    }

    @Override
    public String entitiesFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/entities";
    }

    @Override
    public String toEntityImport(String name) {
        return "const " +name+"= require('../entities/"+toEntitiesName(name)+"')";
    }
}
