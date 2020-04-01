package io.swagger.codegen.v3.generators.modules.plugins.nodeJs;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.modules.base.plugin.AbstractPlugin;
import io.swagger.codegen.v3.generators.modules.plugins.golang.GolangGin;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public class ExpressJs extends BaseJavascript {
    static Logger LOGGER = LoggerFactory.getLogger(ExpressJs.class);
    protected Set<String> notImport= new TreeSet<>();
    private final String configFolder="src/configurations";
    private final String configMiddleware="src/middleware";
    private final String helperFolder="src/helper";
    private final String utilsRedis="src/utils/redisUtils";
    private final String exceptionFolder="src/exception";
    public ExpressJs() {
        super();
        cliOptions.clear();
        //add api-controller templates loader
        this.apiTemplateFiles.put("api-controller.mustache", ".js");
        //add entities templates loader
        this.entitiesTemplateFiles.put("entities.mustache", ".js");

        //add typing converter
        typeMapping.clear();
        typeMapping.put("integer", "Number");
        typeMapping.put("string", "String");

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
        supportingFiles.add(new SupportingFile("routers.mustache",  File.separator + "src/controllers", "routers.js"));
        supportingFiles.add(new SupportingFile("package.mustache", "", "package.json"));
        supportingFiles.add(new SupportingFile("middleware/index.mustache", File.separator+configMiddleware, "index.js"));
        supportingFiles.add(new SupportingFile("configurations/index.mustache", File.separator+configFolder, "index.js"));
        supportingFiles.add(new SupportingFile("configurations/configurations.response.mustache", File.separator+configFolder, "configurations.response.js"));
        supportingFiles.add(new SupportingFile("app.mustache", File.separator, "app.js"));
        supportingFiles.add(new SupportingFile("helper/helper.status.code.mustache", File.separator+helperFolder, "helper.status.code.js"));
        supportingFiles.add(new SupportingFile("exception/exception.app.mustache", File.separator+exceptionFolder, "exception.app.js"));

        notImport.add("integer");

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
    public String toApiFilename(String name) {
        return "controller." + underscore(name);
    }


    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator +  File.separator + "src/controllers";
    }

    @Override
    public String toApiName(String name) {
        return "controller." + underscore(name);
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
        return outputFolder + File.separator +  File.separator + "src/entities";
    }

    @Override
    public String toEntityImport(String name) {
        if (!notImport.contains(name))
            return "const " + name + "= require('../entities/" + toEntitiesName(name) + "')";
        return null;
    }
}
