package com.subastas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoDto(
        Integer id,
        Integer adquisicionId,
        Integer multaId,
        Integer medioPagoId,
        BigDecimal importeTotal,
        String moneda,
        String estado,
        LocalDateTime fechaPago
) {}