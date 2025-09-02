package com.kryptforge.crud.service;

import com.kryptforge.crud.exception.ResourceNotFoundException;
import com.kryptforge.crud.model.Product;
import com.kryptforge.crud.repository.ProductRepository;
import com.kryptforge.crud.service.quotation.CurrencyQuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CurrencyQuotationService currencyQuotationService;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        // Example of business logic: check if product with same name already exists
        if (!productRepository.findByNameContaining(product.getName()).isEmpty()) {
            throw new IllegalStateException("Product with name " + product.getName() + " already exists.");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        product.setName(productDetails.getName());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        productRepository.delete(product);
    }

    public List<Product> findProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public Map<String, Double> getRates() {
        return currencyQuotationService.getRates();
    }
}
