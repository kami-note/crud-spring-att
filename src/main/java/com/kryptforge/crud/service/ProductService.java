package com.kryptforge.crud.service;

import com.kryptforge.crud.exception.ResourceNotFoundException;
import com.kryptforge.crud.model.Product;
import com.kryptforge.crud.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

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

    public Double getProductPriceInBRL(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        // Assuming the product name is a valid CoinGecko coin id (e.g., "bitcoin")
        String coinId = product.getName().toLowerCase();
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coinId + "&vs_currencies=brl";

        Map<String, Map<String, Double>> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey(coinId) && response.get(coinId).containsKey("brl")) {
            Double brlRate = response.get(coinId).get("brl");
            return product.getPrice() * brlRate;
        }

        return null;
    }
}
