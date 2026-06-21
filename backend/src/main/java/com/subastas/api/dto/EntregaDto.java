package com.subastas.api.dto;

import java.time.LocalDate;

public record EntregaDto(
        Integer id,
        Integer adquisicionId,
        String tipo,
        String estado,
        String direccion,
        String codigoRetiro,
        String transportista,
        String codigoSeguimiento,
        LocalDate fechaEstimada
) {}