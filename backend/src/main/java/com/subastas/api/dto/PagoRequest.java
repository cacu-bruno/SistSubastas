package com.subastas.api.dto;

import jakarta.validation.constraints.NotNull;

public record PagoRequest(
        @NotNull Integer medioPagoId
) {}