package io.swagger.codegen.v3.generators.modules.base.plugin;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.plugins.EntitiesSchemas;
import io.swagger.codegen.v3.plugins.Plugins;

import java.util.*;

public abstract class AbstractPlugin extends DefaultCodegenConfig implements Plugins {
    public static final String CONFIG_PACKAGE = "configPackage";
    public static final String BASE_PACKAGE = "basePackage";
    protected static final String ENTITIES_PACKAGE = "entitiesPackage";
    protected static final String SERVICE_PACKAGE = "servicePackage";
    protected static final String VMODEL_PACKAGE = "vModelPackage";


    protected String basePackage;
    protected String defaultTemplateDir;


    protected String apiPackage = "controllers";
    protected String modelPackage = "models";


    protected String entitiesPackage = "entities";
    protected String entitiesFileFolder;
    protected String entitiesTestFileFolder;
    protected String entitiesDocFileFolder;

    protected String vModelPackage = "vmodels";
    protected String vModelFileFolder;
    protected String vModelTestFileFolder;
    protected String vModelDocFileFolder;

    protected String servicePackage = "services";
    protected String serviceFileFolder;
    protected String serviceTestFileFolder;
    protected String serviceDocFileFolder;

    protected String toEntitiesName;
    protected String toEntitiesFilename;
    protected String toVModelName;
    protected String toVModelFilename;
    protected String toServiceFilename;

    protected String apiFileFolder;

    protected Map<String, String> serviceTemplateFiles = new HashMap<String, String>();
    protected Map<String, String> entitiesTemplateFiles = new HashMap<String, String>();
    protected Map<String, String> vModelTemplateFiles = new HashMap<String, String>();

    public AbstractPlugin() {
        super();
        languageSpecificPrimitives = new HashSet<>(
            Arrays.asList(
                "string",
                "bool",
                "uint",
                "uint32",
                "uint64",
                "int",
                "int32",
                "int64",
                "float32",
                "float64",
                "complex64",
                "complex128",
                "rune",
                "byte")
        );


        if (this.additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.setBasePackage(this.additionalProperties.get(CodegenConstants.INVOKER_PACKAGE).toString());
            additionalProperties.put(basePackage, this.getBasePakage());
        }
        //add variable on templates
        additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        additionalProperties.put(VMODEL_PACKAGE, vModelPackage);
        additionalProperties.put(SERVICE_PACKAGE, servicePackage);
        additionalProperties.put(ENTITIES_PACKAGE, entitiesPackage);
    }


    public String getBasePakage() {
        return basePackage;
    }

    public void setBasePackage(String basePakage) {
        this.basePackage = basePakage;
    }

    @Override
    public String getDefaultTemplateDir() {
        return this.defaultTemplateDir;
    }

    @Override
    public CodegenType getTag() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String toEntitiesName(String name) {
        return this.toEntitiesName;
    }

    @Override
    public String toEntitiesFilename(String name) {
        return this.toEntitiesFilename;
    }

    @Override
    public String entitiesPackage() {
        return this.entitiesPackage;
    }

    @Override
    public String entitiesFileFolder() {
        return this.entitiesFileFolder;
    }

    @Override
    public String entitiesTestFileFolder() {
        return this.entitiesTestFileFolder;
    }

    @Override
    public String entitiesDocFileFolder() {
        return this.entitiesDocFileFolder;
    }

    @Override
    public String vModelPackage() {
        return this.vModelPackage;
    }

    @Override
    public String vModelFileFolder() {
        return this.vModelFileFolder;
    }

    @Override
    public String vModelTestFileFolder() {
        return this.vModelTestFileFolder;
    }

    @Override
    public String vModelDocFileFolder() {
        return this.vModelDocFileFolder;
    }

    @Override
    public String toVModelName(String name) {
        return this.toVModelName;
    }

    @Override
    public String toVModelFilename(String name) {
        return this.toVModelFilename;
    }

    @Override
    public String toServiceFilename(String name) {
        return this.toServiceFilename;
    }

    @Override
    public String serviceTestFileFolder() {
        return this.serviceTestFileFolder;
    }

    @Override
    public String serviceDocFileFolder() {
        return this.serviceDocFileFolder;
    }

    public String toServiceName(String name) {
        return name;
    }


    public String servicePackage() {
        return this.servicePackage;
    }

    public String serviceFileFolder() {
        return this.serviceFileFolder;
    }

    @Override
    public Map<String, String> entitiesTemplateFiles() {
        return this.entitiesTemplateFiles;
    }

    @Override
    public Map<String, String> vModelTemplateFiles() {
        return this.vModelTemplateFiles;
    }

    @Override
    public CodegenModel fromEntities(String name, EntitiesSchemas schema, Map<String, EntitiesSchemas> allDefinitions) {
        return null;
    }
}
