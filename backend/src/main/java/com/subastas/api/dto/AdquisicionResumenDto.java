package com.subastas.api.dto;

import java.math.BigDecimal;

public record AdquisicionResumenDto(
        long total,
        long pendientes,
        long pagadas,
        long entregadas,
        long enMora,
        BigDecimal totalImporte,
        BigDecimal totalComision,
        BigDecimal totalPagado
) {}