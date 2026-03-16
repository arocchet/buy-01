package com.letsplay.productservice.service;

import com.letsplay.productservice.dto.ProductRequest;
import com.letsplay.productservice.exception.BadRequestException;
import com.letsplay.productservice.exception.ResourceNotFoundException;
import com.letsplay.productservice.kafka.ProductEventProducer;
import com.letsplay.productservice.model.Product;
import com.letsplay.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    public ProductService(ProductRepository productRepository, ProductEventProducer productEventProducer) {
        this.productRepository = productRepository;
        this.productEventProducer = productEventProducer;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getProductsByUserId(String userId) {
        return productRepository.findByUserId(userId);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Full search + filter: keyword (name/description), minPrice, maxPrice.
     * Pass null to skip a filter.
     */
    public List<Product> searchAndFilter(String keyword, Double minPrice, Double maxPrice) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasMin = minPrice != null;
        boolean hasMax = maxPrice != null;

        if (hasKeyword && hasMin && hasMax) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(keyword, minPrice, maxPrice);
        }
        if (hasKeyword) {
            List<Product> byKeyword = productRepository.searchByKeyword(keyword);
            if (hasMin) {
                double min = minPrice;
                byKeyword = byKeyword.stream().filter(p -> p.getPrice() >= min).collect(java.util.stream.Collectors.toList());
            }
            if (hasMax) {
                double max = maxPrice;
                byKeyword = byKeyword.stream().filter(p -> p.getPrice() <= max).collect(java.util.stream.Collectors.toList());
            }
            return byKeyword;
        }
        if (hasMin && hasMax) {
            return productRepository.findByPriceBetween(minPrice, maxPrice);
        }
        if (hasMin) {
            return productRepository.findByPriceGreaterThanEqual(minPrice);
        }
        if (hasMax) {
            return productRepository.findByPriceLessThanEqual(maxPrice);
        }
        return productRepository.findAll();
    }

    public Product createProduct(ProductRequest request, String userId, String userRole) {
        // Only sellers can create products
        if (!"seller".equals(userRole)) {
            throw new BadRequestException("Only sellers can create products");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUserId(userId);

        Product savedProduct = productRepository.save(product);

        // Publish event
        productEventProducer.sendProductCreatedEvent(savedProduct.getId(), savedProduct.getUserId());

        return savedProduct;
    }

    public Product updateProduct(String id, ProductRequest request, String userId, String userRole) {
        Product product = getProductById(id);

        // Check ownership
        if (!product.getUserId().equals(userId)) {
            throw new BadRequestException("You can only update your own products");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        Product updatedProduct = productRepository.save(product);

        // Publish event
        productEventProducer.sendProductUpdatedEvent(updatedProduct.getId(), updatedProduct.getUserId());

        return updatedProduct;
    }

    public void deleteProduct(String id, String userId, String userRole) {
        Product product = getProductById(id);

        // Check ownership
        if (!product.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own products");
        }

        productRepository.delete(product);

        // Publish event
        productEventProducer.sendProductDeletedEvent(id, userId);
    }

    public void deleteProductsByUserId(String userId) {
        productRepository.deleteByUserId(userId);
    }
}
