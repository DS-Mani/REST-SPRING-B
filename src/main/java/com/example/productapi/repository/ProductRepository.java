package com.example.productapi.repository;

import com.example.productapi.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Product}.
 *
 * By extending JpaRepository we inherit methods for CRUD operations, pagination,
 * and sorting without writing a single SQL query. Spring generates the
 * implementation at runtime from the interface definition.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
