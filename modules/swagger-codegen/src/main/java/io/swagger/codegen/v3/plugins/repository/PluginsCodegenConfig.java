package io.swagger.codegen.v3.plugins.repository;

import io.swagger.codegen.v3.CodegenModel;

import java.util.Map;

public interface PluginsCodegenConfig {
    String repositoryPackage();

    String repositoryFileFolder();

    String toRepositoryFileName(String name);

    Map<String, String> repositoryTemplateFiles();

//    void postProcessRepositories(CodegenModel codegenModel);

    String repositoryFileName(String name, String tag);

    String servicePackage();
}
