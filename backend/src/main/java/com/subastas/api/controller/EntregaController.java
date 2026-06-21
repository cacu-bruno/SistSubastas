package com.subastas.api.controller;

import com.subastas.api.dto.EntregaDto;
import com.subastas.api.dto.EntregaRequest;
import com.subastas.api.service.EntregaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/me/adquisiciones/{id}/entrega")
public class EntregaController {

    private final EntregaService service;

    public EntregaController(EntregaService service) {
        this.service = service;
    }

    @GetMapping
    public EntregaDto getDeliveryByAdquisicion(@PathVariable Integer id) {
        return service.getDeliveryByAdquisicion(id);
    }

    @GetMapping("/retiro")
    public EntregaDto getPickupDetails(@PathVariable Integer id) {
        return service.getPickupDetails(id);
    }

    @GetMapping("/envio")
    public EntregaDto getShippingStatus(@PathVariable Integer id) {
        return service.getShippingStatus(id);
    }

    @PostMapping("/envio")
    public EntregaDto selectShippingDelivery(@PathVariable Integer id, @RequestBody EntregaRequest req) {
        return service.selectShippingDelivery(id, req);
    }

    @PostMapping("/retiro")
    public EntregaDto selectPickupDelivery(@PathVariable Integer id, @RequestBody EntregaRequest req) {
        return service.selectPickupDelivery(id, req);
    }

    @PostMapping("/confirmar")
    public EntregaDto confirmReception(@PathVariable Integer id) {
        return service.confirmReception(id);
    }
}