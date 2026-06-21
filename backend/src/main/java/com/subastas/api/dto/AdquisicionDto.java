package com.subastas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdquisicionDto(
        Integer id,
        Integer productoId,
        String producto,
        BigDecimal importe,
        BigDecimal comision,
        String estado,
        LocalDateTime fecha,
        Integer facturaId,
        BigDecimal total,
        Integer entregaId,
        String entregaTipo,
        String entregaEstado,
        String direccion,
        String codigoRetiro,
        String codigoSeguimiento,
        LocalDate fechaEstimada,
        Integer multaId,
        String multaEstado
) {}
