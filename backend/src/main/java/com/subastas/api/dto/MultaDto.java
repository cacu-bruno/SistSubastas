package com.subastas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MultaDto(
        Integer id,
        Integer adquisicionId,
        BigDecimal importe,
        String estado,
        LocalDate fechaLimite
) {}
