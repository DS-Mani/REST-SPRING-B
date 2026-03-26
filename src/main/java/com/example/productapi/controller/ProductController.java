package com.example.productapi.controller;

import com.example.productapi.dto.PagedResponse;
import com.example.productapi.dto.ProductPatchRequest;
import com.example.productapi.dto.ProductRequest;
import com.example.productapi.dto.ProductResponse;
import com.example.productapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that maps every HTTP method to a product operation.
 *
 * <ul>
 *   <li>POST   /api/products         — create a new product</li>
 *   <li>GET    /api/products          — list all products (paginated &amp; sorted)</li>
 *   <li>GET    /api/products/{id}     — fetch a single product</li>
 *   <li>PUT    /api/products/{id}     — replace a product completely</li>
 *   <li>PATCH  /api/products/{id}     — update selected fields</li>
 *   <li>DELETE /api/products/{id}     — remove a product</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ──────────────────────────────────────────────
    // POST – Create a new product
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ──────────────────────────────────────────────
    // GET – Fetch a single product by ID
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ──────────────────────────────────────────────
    // GET – Fetch all products (with pagination & sorting)
    //   ?page=0&size=10&sortBy=name&direction=asc
    //   ?category=Electronics
    //   ?search=phone
    // ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ProductResponse> response;

        if (category != null && !category.isBlank()) {
            response = productService.getProductsByCategory(category, pageable);
        } else if (search != null && !search.isBlank()) {
            response = productService.searchProducts(search, pageable);
        } else {
            response = productService.getAllProducts(pageable);
        }

        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────
    // PUT – Full update of a product
    // ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // ──────────────────────────────────────────────
    // PATCH – Partial update of a product
    // ──────────────────────────────────────────────
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> patchProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductPatchRequest request) {
        return ResponseEntity.ok(productService.patchProduct(id, request));
    }

    // ──────────────────────────────────────────────
    // DELETE – Remove a product
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
