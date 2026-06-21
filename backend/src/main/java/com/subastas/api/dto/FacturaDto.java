package com.subastas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FacturaDto(
        Integer id,
        Integer adquisicionId,
        String numeroFactura,
        BigDecimal importe,
        BigDecimal comision,
        BigDecimal costoEnvio,
        BigDecimal total,
        LocalDateTime fecha
) {}