package com.filecomparator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mappings {
    @JsonProperty("attributeField")
    private AttributeField attributeField;
    @JsonProperty("reportFields")
    private ReportFields reportFields;
    @JsonProperty("productFields")
    private ProductFields productFields;

    // Getters and Setters
    public AttributeField getAttributeField() {
        return attributeField;
    }

    public void setAttributeField(AttributeField attributeField) {
        this.attributeField = attributeField;
    }

    public ReportFields getReportFields() {
        return reportFields;
    }

    public void setReportFields(ReportFields reportFields) {
        this.reportFields = reportFields;
    }

    public ProductFields getProductFields() {
        return productFields;
    }

    public void setProductFields(ProductFields productFields) {
        this.productFields = productFields;
    }
}
