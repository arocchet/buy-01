package com.letsplay.letsplay.service;

import com.letsplay.letsplay.model.Product;
import com.letsplay.letsplay.repository.ProductRepository;
import com.letsplay.letsplay.security.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    public Product createProduct(Product product) {
        inputSanitizer.validateUserInput(product.getName(), product.getDescription());

        product.setName(inputSanitizer.sanitize(product.getName()));
        product.setDescription(inputSanitizer.sanitize(product.getDescription()));

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByUserId(String userId) {
        return productRepository.findByUserId(userId);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Product updateProduct(String id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }

        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    public boolean isProductOwner(String productId, String userId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.isPresent() && product.get().getUserId().equals(userId);
    }
}