package com.kryptforge.crud.dto;

public class QuotationDTO {

    private String currency;
    private Double value;

    public QuotationDTO(String currency, Double value) {
        this.currency = currency;
        this.value = value;
    }

    // Getters and Setters
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
