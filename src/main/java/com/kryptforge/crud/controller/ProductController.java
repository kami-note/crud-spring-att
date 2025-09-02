package com.kryptforge.crud.controller;

import com.kryptforge.crud.dto.ProductRequestDTO;
import com.kryptforge.crud.dto.ProductResponseWithQuotationsDTO;
import com.kryptforge.crud.dto.QuotationDTO;
import com.kryptforge.crud.model.Product;
import com.kryptforge.crud.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseWithQuotationsDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setPrice(productRequestDTO.getPrice());
        product.setStock(productRequestDTO.getStock());

        Product createdProduct = productService.createProduct(product);

        Map<String, Double> rates = productService.getRates();
        List<QuotationDTO> quotations = rates.entrySet().stream()
            .map(entry -> new QuotationDTO(entry.getKey(), entry.getValue() * createdProduct.getPrice()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(toResponseDTO(createdProduct, quotations));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseWithQuotationsDTO>> getAllProducts(Pageable pageable) {
        Page<Product> productsPage = productService.getAllProducts(pageable);
        Map<String, Double> rates = productService.getRates();

        Page<ProductResponseWithQuotationsDTO> dtoPage = productsPage.map(product -> {
            List<QuotationDTO> quotations = rates.entrySet().stream()
                    .map(entry -> new QuotationDTO(entry.getKey(), entry.getValue() * product.getPrice()))
                    .collect(Collectors.toList());
            return toResponseDTO(product, quotations);
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseWithQuotationsDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    Map<String, Double> rates = productService.getRates();
                    List<QuotationDTO> quotations = rates.entrySet().stream()
                        .map(entry -> new QuotationDTO(entry.getKey(), entry.getValue() * product.getPrice()))
                        .collect(Collectors.toList());
                    return ResponseEntity.ok(toResponseDTO(product, quotations));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseWithQuotationsDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setPrice(productRequestDTO.getPrice());
        product.setStock(productRequestDTO.getStock());

        Product updatedProduct = productService.updateProduct(id, product);

        Map<String, Double> rates = productService.getRates();
        List<QuotationDTO> quotations = rates.entrySet().stream()
            .map(entry -> new QuotationDTO(entry.getKey(), entry.getValue() * updatedProduct.getPrice()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(toResponseDTO(updatedProduct, quotations));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductResponseWithQuotationsDTO toResponseDTO(Product product, List<QuotationDTO> quotations) {
        ProductResponseWithQuotationsDTO dto = new ProductResponseWithQuotationsDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setQuotations(quotations);
        return dto;
    }
}
