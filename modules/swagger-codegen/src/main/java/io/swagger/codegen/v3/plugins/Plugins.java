package io.swagger.codegen.v3.plugins;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenModel;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Plugins extends CodegenConfig {

    String toEntitiesName(String name);

    String toEntitiesFilename(String name);

    String entitiesPackage();

    String entitiesFileFolder();

    String entitiesTestFileFolder();

    String entitiesDocFileFolder();

    String toVModelName(String name);

    String toVModelFilename(String name);

    String vModelPackage();

    String vModelFileFolder();

    String vModelTestFileFolder();

    String vModelDocFileFolder();

    String toServiceName(String name);

    String toServiceFilename(String name);

    String servicePackage();

    String serviceFileFolder();

    String serviceTestFileFolder();

    String serviceDocFileFolder();

    public Map<String, String> entitiesTemplateFiles();

    public Map<String, String> vModelTemplateFiles();

    public CodegenModel fromEntities(String name,EntitiesSchemas schema, Map<String, EntitiesSchemas>allDefinitions);


}
