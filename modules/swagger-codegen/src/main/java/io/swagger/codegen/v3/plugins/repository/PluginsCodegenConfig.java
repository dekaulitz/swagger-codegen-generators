package io.swagger.codegen.v3.plugins.repository;

import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.List;
import java.util.Map;

public interface PluginsCodegenConfig {
    String repositoryPackage();

    String repositoryFileFolder();

    String toRepositoryFileName(String name);

    Map<String, String> repositoryTemplateFiles();

//    void postProcessRepositories(CodegenModel codegenModel);

    String repositoryFileName(String name, String tag);

    String servicePackage();


    /**
     * checking is config allow to generate service class
     *
     * @return
     */
    Boolean generateService();

    Map<String, String> serviceApiTemplateFiles();

    String serviceApiFileName(String templateName, String tag);

    String serviceFileFolder();

    //@TODO need to be deleted
    List<String> serviceImportMapping(CodegenOperation codegenOperation, Tag tag);

    List<Map<String, String>> importServices(List<CodegenOperation> ops);



}
