package io.swagger.codegen.v3.plugins;

import io.swagger.codegen.v3.CodegenObject;
import io.swagger.codegen.v3.plugins.repository.CodeGenRepositoryModel;

import java.util.List;

public class CodeGenPluginsModel extends CodegenObject {
    public CodeGenRepositoryModel repository;

    public List<String> importServicePackage;

    public List<String> importModelPackage;

    public CodeGenRepositoryModel getRepository() {
        return repository;
    }

    public void setRepository(CodeGenRepositoryModel repository) {
        this.repository = repository;
    }

    public List<String> getImportServicePackage() {
        return importServicePackage;
    }

    public void setImportServicePackage(List<String> importServicePackage) {
        this.importServicePackage = importServicePackage;
    }

    public List<String> getImportModelPackage() {
        return importModelPackage;
    }

    public void setImportModelPackage(List<String> importModelPackage) {
        this.importModelPackage = importModelPackage;
    }
}
