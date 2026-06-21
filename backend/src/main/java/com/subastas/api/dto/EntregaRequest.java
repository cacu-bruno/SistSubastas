package com.subastas.api.dto;

import java.time.LocalDate;

public record EntregaRequest(
        String direccion,
        String transportista,
        String codigoSeguimiento,
        String codigoRetiro,
        LocalDate fechaEstimada,
        java.math.BigDecimal costoEnvio
) {}