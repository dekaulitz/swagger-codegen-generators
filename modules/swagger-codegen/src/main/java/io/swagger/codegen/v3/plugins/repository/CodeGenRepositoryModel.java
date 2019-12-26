package io.swagger.codegen.v3.plugins.repository;

import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeGenRepositoryModel extends CodeGenPluginsModel {
    public String tableName;
    public ExternalDocumentation externalDocs;
    public Boolean generateDate;

    public List<CodegenProperty> varRepositories = new ArrayList<CodegenProperty>();
    public Map<String, Schema> properties;

    public Map<String, Schema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }


    public List<CodegenProperty> getVarRepositories() {
        return varRepositories;
    }

    public void setVarRepositories(List<CodegenProperty> varRepositories) {
        this.varRepositories = varRepositories;
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ExternalDocumentation getExternalDocs() {
        return externalDocs;
    }

    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    public Boolean getGenerateDate() {
        return generateDate;
    }

    public void setGenerateDate(Boolean generateDate) {
        this.generateDate = generateDate;
    }


}
