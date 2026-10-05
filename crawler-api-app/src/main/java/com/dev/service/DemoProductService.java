package com.dev.service;

import com.dev.domain.DemoProduct;
import com.dev.dto.CreateDemoProductRequest;
import com.dev.dto.UpdateDemoPriceRequest;
import com.dev.repository.DemoProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemoProductService {

    private final DemoProductRepository
            repository;

    public DemoProduct create(
            CreateDemoProductRequest request
    ) {

        DemoProduct product =
                DemoProduct.builder()

                        .name(
                                request.getName()
                        )

                        .regularPrice(
                                request.getRegularPrice()
                        )

                        .currentPrice(
                                request.getCurrentPrice()
                        )

                        .inStock(true)

                        .build();

        return repository.save(
                product
        );
    }


    public List<DemoProduct> findAll() {

        return repository.findAll();
    }


    public DemoProduct findByPublicId(
            UUID publicId
    ) {

        return repository
                .findByPublicId(publicId)

                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Demo product not found: "
                                        + publicId
                        )
                );
    }


    public DemoProduct updatePrice(
            Long id,
            UpdateDemoPriceRequest request
    ) {

        DemoProduct product =
                repository.findById(id)

                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Demo product not found: "
                                                + id
                                )
                        );

        product.setCurrentPrice(
                request.getCurrentPrice()
        );

        return repository.save(
                product
        );
    }
}