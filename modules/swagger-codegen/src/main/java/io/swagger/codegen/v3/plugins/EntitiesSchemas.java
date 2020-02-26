package io.swagger.codegen.v3.plugins;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.XML;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class EntitiesSchemas extends Schema
{
    public EntitiesSchemas() {
        super();
    }

    protected EntitiesSchemas(String type, String format) {
        super(type, format);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public Schema name(String name) {
        return super.name(name);
    }

    @Override
    public Discriminator getDiscriminator() {
        return super.getDiscriminator();
    }

    @Override
    public void setDiscriminator(Discriminator discriminator) {
        super.setDiscriminator(discriminator);
    }

    @Override
    public Schema discriminator(Discriminator discriminator) {
        return super.discriminator(discriminator);
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public Schema title(String title) {
        return super.title(title);
    }

    @Override
    public Object getDefault() {
        return super.getDefault();
    }

    @Override
    public void setDefault(Object _default) {
        super.setDefault(_default);
    }

    @Override
    protected Object cast(Object value) {
        return super.cast(value);
    }

    @Override
    public List getEnum() {
        return super.getEnum();
    }

    @Override
    public void setEnum(List _enum) {
        super.setEnum(_enum);
    }

    @Override
    public void addEnumItemObject(Object _enumItem) {
        super.addEnumItemObject(_enumItem);
    }

    @Override
    public BigDecimal getMultipleOf() {
        return super.getMultipleOf();
    }

    @Override
    public void setMultipleOf(BigDecimal multipleOf) {
        super.setMultipleOf(multipleOf);
    }

    @Override
    public Schema multipleOf(BigDecimal multipleOf) {
        return super.multipleOf(multipleOf);
    }

    @Override
    public BigDecimal getMaximum() {
        return super.getMaximum();
    }

    @Override
    public void setMaximum(BigDecimal maximum) {
        super.setMaximum(maximum);
    }

    @Override
    public Schema maximum(BigDecimal maximum) {
        return super.maximum(maximum);
    }

    @Override
    public Boolean getExclusiveMaximum() {
        return super.getExclusiveMaximum();
    }

    @Override
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        super.setExclusiveMaximum(exclusiveMaximum);
    }

    @Override
    public Schema exclusiveMaximum(Boolean exclusiveMaximum) {
        return super.exclusiveMaximum(exclusiveMaximum);
    }

    @Override
    public BigDecimal getMinimum() {
        return super.getMinimum();
    }

    @Override
    public void setMinimum(BigDecimal minimum) {
        super.setMinimum(minimum);
    }

    @Override
    public Schema minimum(BigDecimal minimum) {
        return super.minimum(minimum);
    }

    @Override
    public Boolean getExclusiveMinimum() {
        return super.getExclusiveMinimum();
    }

    @Override
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        super.setExclusiveMinimum(exclusiveMinimum);
    }

    @Override
    public Schema exclusiveMinimum(Boolean exclusiveMinimum) {
        return super.exclusiveMinimum(exclusiveMinimum);
    }

    @Override
    public Integer getMaxLength() {
        return super.getMaxLength();
    }

    @Override
    public void setMaxLength(Integer maxLength) {
        super.setMaxLength(maxLength);
    }

    @Override
    public Schema maxLength(Integer maxLength) {
        return super.maxLength(maxLength);
    }

    @Override
    public Integer getMinLength() {
        return super.getMinLength();
    }

    @Override
    public void setMinLength(Integer minLength) {
        super.setMinLength(minLength);
    }

    @Override
    public Schema minLength(Integer minLength) {
        return super.minLength(minLength);
    }

    @Override
    public String getPattern() {
        return super.getPattern();
    }

    @Override
    public void setPattern(String pattern) {
        super.setPattern(pattern);
    }

    @Override
    public Schema pattern(String pattern) {
        return super.pattern(pattern);
    }

    @Override
    public Integer getMaxItems() {
        return super.getMaxItems();
    }

    @Override
    public void setMaxItems(Integer maxItems) {
        super.setMaxItems(maxItems);
    }

    @Override
    public Schema maxItems(Integer maxItems) {
        return super.maxItems(maxItems);
    }

    @Override
    public Integer getMinItems() {
        return super.getMinItems();
    }

    @Override
    public void setMinItems(Integer minItems) {
        super.setMinItems(minItems);
    }

    @Override
    public Schema minItems(Integer minItems) {
        return super.minItems(minItems);
    }

    @Override
    public Boolean getUniqueItems() {
        return super.getUniqueItems();
    }

    @Override
    public void setUniqueItems(Boolean uniqueItems) {
        super.setUniqueItems(uniqueItems);
    }

    @Override
    public Schema uniqueItems(Boolean uniqueItems) {
        return super.uniqueItems(uniqueItems);
    }

    @Override
    public Integer getMaxProperties() {
        return super.getMaxProperties();
    }

    @Override
    public void setMaxProperties(Integer maxProperties) {
        super.setMaxProperties(maxProperties);
    }

    @Override
    public Schema maxProperties(Integer maxProperties) {
        return super.maxProperties(maxProperties);
    }

    @Override
    public Integer getMinProperties() {
        return super.getMinProperties();
    }

    @Override
    public void setMinProperties(Integer minProperties) {
        super.setMinProperties(minProperties);
    }

    @Override
    public Schema minProperties(Integer minProperties) {
        return super.minProperties(minProperties);
    }

    @Override
    public List<String> getRequired() {
        return super.getRequired();
    }

    @Override
    public void setRequired(List required) {
        super.setRequired(required);
    }

    @Override
    public Schema required(List required) {
        return super.required(required);
    }

    @Override
    public Schema addRequiredItem(String requiredItem) {
        return super.addRequiredItem(requiredItem);
    }

    @Override
    public String getType() {
        return super.getType();
    }

    @Override
    public void setType(String type) {
        super.setType(type);
    }

    @Override
    public Schema type(String type) {
        return super.type(type);
    }

    @Override
    public Schema getNot() {
        return super.getNot();
    }

    @Override
    public void setNot(Schema not) {
        super.setNot(not);
    }

    @Override
    public Schema not(Schema not) {
        return super.not(not);
    }

    @Override
    public Map<String, Schema> getProperties() {
        return super.getProperties();
    }

    @Override
    public void setProperties(Map properties) {
        super.setProperties(properties);
    }

    @Override
    public Schema properties(Map properties) {
        return super.properties(properties);
    }

    @Override
    public Schema addProperties(String key, Schema propertiesItem) {
        return super.addProperties(key, propertiesItem);
    }

    @Override
    public Object getAdditionalProperties() {
        return super.getAdditionalProperties();
    }

    @Override
    public void setAdditionalProperties(Object additionalProperties) {
        super.setAdditionalProperties(additionalProperties);
    }

    @Override
    public Schema additionalProperties(Object additionalProperties) {
        return super.additionalProperties(additionalProperties);
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(description);
    }

    @Override
    public Schema description(String description) {
        return super.description(description);
    }

    @Override
    public String getFormat() {
        return super.getFormat();
    }

    @Override
    public void setFormat(String format) {
        super.setFormat(format);
    }

    @Override
    public Schema format(String format) {
        return super.format(format);
    }

    @Override
    public String get$ref() {
        return super.get$ref();
    }

    @Override
    public void set$ref(String $ref) {
        super.set$ref($ref);
    }

    @Override
    public Schema $ref(String $ref) {
        return super.$ref($ref);
    }

    @Override
    public Boolean getNullable() {
        return super.getNullable();
    }

    @Override
    public void setNullable(Boolean nullable) {
        super.setNullable(nullable);
    }

    @Override
    public Schema nullable(Boolean nullable) {
        return super.nullable(nullable);
    }

    @Override
    public Boolean getReadOnly() {
        return super.getReadOnly();
    }

    @Override
    public void setReadOnly(Boolean readOnly) {
        super.setReadOnly(readOnly);
    }

    @Override
    public Schema readOnly(Boolean readOnly) {
        return super.readOnly(readOnly);
    }

    @Override
    public Boolean getWriteOnly() {
        return super.getWriteOnly();
    }

    @Override
    public void setWriteOnly(Boolean writeOnly) {
        super.setWriteOnly(writeOnly);
    }

    @Override
    public Schema writeOnly(Boolean writeOnly) {
        return super.writeOnly(writeOnly);
    }

    @Override
    public Object getExample() {
        return super.getExample();
    }

    @Override
    public void setExample(Object example) {
        super.setExample(example);
    }

    @Override
    public Schema example(Object example) {
        return super.example(example);
    }

    @Override
    public ExternalDocumentation getExternalDocs() {
        return super.getExternalDocs();
    }

    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        super.setExternalDocs(externalDocs);
    }

    @Override
    public Schema externalDocs(ExternalDocumentation externalDocs) {
        return super.externalDocs(externalDocs);
    }

    @Override
    public Boolean getDeprecated() {
        return super.getDeprecated();
    }

    @Override
    public void setDeprecated(Boolean deprecated) {
        super.setDeprecated(deprecated);
    }

    @Override
    public Schema deprecated(Boolean deprecated) {
        return super.deprecated(deprecated);
    }

    @Override
    public XML getXml() {
        return super.getXml();
    }

    @Override
    public void setXml(XML xml) {
        super.setXml(xml);
    }

    @Override
    public Schema xml(XML xml) {
        return super.xml(xml);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Map<String, Object> getExtensions() {
        return super.getExtensions();
    }

    @Override
    public void addExtension(String name, Object value) {
        super.addExtension(name, value);
    }

    @Override
    public void setExtensions(Map extensions) {
        super.setExtensions(extensions);
    }

    @Override
    public Schema extensions(Map extensions) {
        return super.extensions(extensions);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected String toIndentedString(Object o) {
        return super.toIndentedString(o);
    }
}
