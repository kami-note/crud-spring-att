package com.kryptforge.crud.dto;

import java.util.List;

public class ProductResponseWithQuotationsDTO extends ProductResponseDTO {

    private List<QuotationDTO> quotations;

    public List<QuotationDTO> getQuotations() {
        return quotations;
    }

    public void setQuotations(List<QuotationDTO> quotations) {
        this.quotations = quotations;
    }
}
