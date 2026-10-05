package com.dev.engine;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class PriceResult {
    private BigDecimal price;

    private String title;

    private boolean available;
}
