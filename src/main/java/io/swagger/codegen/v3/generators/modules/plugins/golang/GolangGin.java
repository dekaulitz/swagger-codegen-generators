package io.swagger.codegen.v3.generators.modules.plugins.golang;

import io.swagger.codegen.v3.*;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class GolangGin extends BaseGolang {
    static Logger LOGGER = LoggerFactory.getLogger(GolangGin.class);
    private final String configFolder="src/configurations";
    private final String configMiddleware="src/middleware";
    private final String helperFolder="src/helper";
    private final String utilsRedis="src/utils/redisUtils";
    private final String exceptionFolder="src/exception";
    public GolangGin() {
        super();
        //cleaning up cli before
        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Go package name (convention: lowercase).")
            .defaultValue("swagger"));
        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC)
            .defaultValue(Boolean.TRUE.toString()));

        //add vModel templates loader
        this.vModelTemplateFiles.put("vmodel.mustache", ".go");
        //add api-controller templates loader
       this.apiTemplateFiles.put("api-controller.mustache", ".go");
     //   add entities templates loader
        this.entitiesTemplateFiles.put("entities.mustache", ".go");
     //   add srevice templates loader
        this.serviceTemplateFiles.put("service.mustache", ".go");
     //   add model templates loader
        this.modelTemplateFiles.put("model.mustache",".go");

        //add typing converter
        typeMapping.clear();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int64");
        typeMapping.put("number", "float32");
        typeMapping.put("float", "float32");
        typeMapping.put("double", "float64");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "string");
        typeMapping.put("UUID", "string");
        typeMapping.put("date", "string");
        typeMapping.put("DateTime", "time.Time");
        typeMapping.put("password", "string");
        typeMapping.put("File", "*os.File");
        typeMapping.put("file", "*os.File");
        // map binary to string as a workaround
        // the correct solution is to use []byte
        typeMapping.put("binary", "[]byte");
        typeMapping.put("ByteArray", "string");
        typeMapping.put("object", "interface{}");
        typeMapping.put("UUID", "string");
        typeMapping.put("Object", "interface{}");


        //import mapping related with variable type
        importMapping.put("Datetime","time");
        importMapping.put("DateTime", "time");
        importMapping.put("LocalDateTime", "time");
        importMapping.put("LocalDate", "time");
        importMapping.put("LocalTime", "time");

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
        supportingFiles.add(new SupportingFile("routers.mustache",  basePackage+File.separator+"src/controllers", "routers.go"));
        supportingFiles.add(new SupportingFile("env.mustache", basePackage, "env"));
        supportingFiles.add(new SupportingFile("main.mustache", basePackage, "main.go"));
        supportingFiles.add(new SupportingFile("runner.mustache", basePackage, "runner.sh"));
        supportingFiles.add(new SupportingFile("Dockerfile.mustache", basePackage, "Dockerfile"));


        supportingFiles.add(new SupportingFile("configurations/database_configuration.go", basePackage+File.separator+configFolder, "database_configuration.go"));
        supportingFiles.add(new SupportingFile("configurations/env_configuration.mustache", basePackage+File.separator+configFolder, "env_configuration.go"));
        supportingFiles.add(new SupportingFile("middleware/middleware_configuration.mustache", basePackage+File.separator+configMiddleware, "middleware_configuration.go"));
        supportingFiles.add(new SupportingFile("utils/redisUtils/redis.mustache", basePackage+File.separator+utilsRedis, "redis.go"));
        supportingFiles.add(new SupportingFile("exception/exception_handler.mustache", basePackage+File.separator+exceptionFolder, "exception_handler.go"));

        supportingFiles.add(new SupportingFile("configurations/response_configuration.mustache", basePackage+File.separator+configFolder, "response_configuration.go"));
        supportingFiles.add(new SupportingFile("configurations/http_configuration.go", basePackage+File.separator+configFolder, "http_configuration.go"));
        supportingFiles.add(new SupportingFile("configurations/redis_configuration.go", basePackage+File.separator+configFolder, "redis_configuration.go"));
        supportingFiles.add(new SupportingFile("helper/helper.go", basePackage+File.separator+helperFolder, "helper.go"));
        supportingFiles.add(new SupportingFile("helper/status_code_helper.go", basePackage+File.separator+helperFolder, "status_code_helper.go"));
        supportingFiles.add(new SupportingFile("application.mustache", basePackage, "application.yaml"));


    }

    //operationId on golang change into camelcase
    @Override
    public String toOperationId(String operationId) {
        String sanitizedOperationId = sanitizeName(operationId);
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(sanitizedOperationId)) {
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to "
                + camelize("call_" + operationId));
            sanitizedOperationId = "call_" + sanitizedOperationId;
        }
        return camelize(sanitizedOperationId);
    }

    @Override
    public String getName() {
        return "go-gin";
    }


    @Override
    public String getDefaultTemplateDir() {
        return "plugins/go-gin";
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/controllers";
    }

    @Override
    public String toApiFilename(String name) {
        return "controller_" + underscore(name);
    }

    @Override
    public String toVModelName(String name) {
        return "vmodel_" + underscore(name);
    }

    @Override
    public String vModelFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/vmodels";
    }

    @Override
    public String toEntitiesFilename(String name) {
        return "entity_" + underscore(name);
    }
    @Override
    public String toEntitiesName(String name) {
        return "entity_" + underscore(name);
    }

    @Override
    public String entitiesFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/entities";
    }

    @Override
    public String toModelFilename(String name) {
        return "model_" + underscore(name);
    }


    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + basePackage + File.separator + "src/models";
    }

    @Override
    public String toModelImport(String name) {
        return null;
    }
    public String toEntityImport(String name) {
        return null;
    }

    @Override
    public String toVmodelImport(String name) {
        return null;
    }

    @Override
    //to variable all properties
    public String toVarName(String name) {
        return camelize(name);
    }



}
