package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crawler_domain_config", schema= "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String domain;

    @Column(name = "is_dynamic", nullable = false)
    private boolean isDynamic;

    @Column(name = "timeout_ms")
    private Integer timeoutMs = 5000;

    @Column(name = "price_selector")
    private String priceSelector;

    @Column(name = "product_name_selector")
    private String productNameSelector;

    @Column(name = "title_selector")
    private String titleSelector;

    @Column(name = "description_selector")
    private String descriptionSelector;
}