package com.dev.service;

import com.dev.domain.Product;
import com.dev.dto.CreateProductRequest;
import com.dev.dto.ProductResponse;
import com.dev.dto.UpdateProductRequest;
import com.dev.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository
            productRepository;

    @Cacheable(
            cacheNames = "products",
            key = "T(com.dev.context.TenantContext).getCurrentTenant()"
    )
    public List<ProductResponse> findAll() {
        System.out.println(
                ">>> QUERY PRODUCTS FROM DATABASE"
        );
        return productRepository
                .findAll()
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    @CacheEvict(
            cacheNames = "products",
            key = "T(com.dev.context.TenantContext).getCurrentTenant()"
    )
    public ProductResponse create(
            CreateProductRequest request
    ) {

        String sku =
                request.sku()
                        .trim();


        if (
                productRepository
                        .existsBySkuIgnoreCase(
                                sku
                        )
        ) {

            throw new IllegalArgumentException(
                    "Product SKU already exists: "
                            + sku
            );
        }


        Product product =
                Product.builder()

                        .sku(
                                sku
                        )

                        .productName(
                                request.productName()
                                        .trim()
                        )

                        .ownPrice(
                                request.ownPrice()
                        )

                        .build();


        Product saved =
                productRepository.save(
                        product
                );


        return toResponse(
                saved
        );
    }


    public ProductResponse findById(
            Long id
    ) {

        Product product =
                productRepository
                        .findById(id)

                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found: "
                                                + id
                                )
                        );


        return toResponse(
                product
        );
    }


    private ProductResponse toResponse(
            Product product
    ) {

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getProductName(),
                product.getOwnPrice()
        );
    }

    @CacheEvict(
            cacheNames = "products",
            key = "T(com.dev.context.TenantContext).getCurrentTenant()"
    )
    public ProductResponse update(
            Long id,
            UpdateProductRequest request
    ) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Product not found: " + id
                                        )
                        );


        product.setProductName(
                request.productName
        );

        product.setOwnPrice(
                request.ownPrice
        );


        Product saved =
                productRepository.save(
                        product
                );


        return new ProductResponse(
                saved.getId(),
                saved.getSku(),
                saved.getProductName(),
                saved.getOwnPrice()
        );
    }

    @CacheEvict(
            cacheNames = "products",
            key = "T(com.dev.context.TenantContext).getCurrentTenant()"
    )
    public void delete(
            Long id
    ) {

        productRepository.deleteById(
                id
        );
    }
}