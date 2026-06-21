package com.subastas.api.controller;

import com.subastas.api.dto.AdquisicionDto;
import com.subastas.api.dto.AdquisicionResumenDto;
import com.subastas.api.dto.FacturaDto;
import com.subastas.api.dto.PagoDto;
import com.subastas.api.dto.PagoRequest;
import com.subastas.api.service.AdquisicionService;
import com.subastas.api.service.PagoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/me/adquisiciones")
public class AdquisicionController {

    private final AdquisicionService service;
    private final PagoService pagoService;

    public AdquisicionController(AdquisicionService service, PagoService pagoService) {
        this.service = service;
        this.pagoService = pagoService;
    }

    @GetMapping
    public List<AdquisicionDto> listar(@RequestParam(required = false) String estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public AdquisicionDto getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/resumen")
    public AdquisicionResumenDto resumen() {
        return service.resumen();
    }

    @GetMapping("/{id}/factura")
    public FacturaDto getFacturaFromAdquisicion(@PathVariable Integer id) {
        return service.facturaFromAdquisicion(id);
    }

    @PostMapping("/{id}/payment")
    public PagoDto payAdquisicion(@PathVariable Integer id, @RequestBody PagoRequest req) {
        return pagoService.payAdquisicion(id, req);
    }
}
