package com.example.librarymanage_be.config;


import java.math.BigDecimal;

public class FineConfig {
    public static final BigDecimal LATE_FEE_PER_DAY = BigDecimal.valueOf(5000);
    public static final BigDecimal LOST_PERCENT = BigDecimal.valueOf(0.5);
}
