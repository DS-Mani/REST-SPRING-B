package com.example.productapi.service;

import com.example.productapi.dto.PagedResponse;
import com.example.productapi.dto.ProductPatchRequest;
import com.example.productapi.dto.ProductRequest;
import com.example.productapi.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for product operations. The controller depends on this
 * interface — not the implementation — making it easy to swap or mock.
 */
public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    PagedResponse<ProductResponse> getAllProducts(Pageable pageable);

    PagedResponse<ProductResponse> getProductsByCategory(String category, Pageable pageable);

    PagedResponse<ProductResponse> searchProducts(String query, Pageable pageable);

    ProductResponse updateProduct(Long id, ProductRequest request);

    ProductResponse patchProduct(Long id, ProductPatchRequest request);

    void deleteProduct(Long id);
}
