package com.kryptforge.crud.service.quotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class CurrencyQuotationService {

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Double> getRates() {
        String url = "https://api.frankfurter.app/latest?from=USD&to=BRL,EUR,JPY";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey("rates")) {
            return (Map<String, Double>) response.get("rates");
        }
        return Collections.emptyMap();
    }
}
