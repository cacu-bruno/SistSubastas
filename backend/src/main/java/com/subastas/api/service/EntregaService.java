package com.subastas.api.service;

import com.subastas.api.common.ApiException;
import com.subastas.api.common.ErrorCodes;
import com.subastas.api.domain.Entrega;
import com.subastas.api.domain.Factura;
import com.subastas.api.domain.RegistroDeSubasta;
import com.subastas.api.dto.EntregaDto;
import com.subastas.api.dto.EntregaRequest;
import com.subastas.api.repository.EntregaRepository;
import com.subastas.api.repository.FacturaRepository;
import com.subastas.api.repository.RegistroDeSubastaRepository;
import com.subastas.api.security.AuthPrincipal;
import com.subastas.api.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepo;
    private final RegistroDeSubastaRepository rdsRepo;
    private final FacturaRepository facturaRepo;

    public EntregaService(EntregaRepository entregaRepo,
                          RegistroDeSubastaRepository rdsRepo,
                          FacturaRepository facturaRepo) {
        this.entregaRepo = entregaRepo;
        this.rdsRepo = rdsRepo;
        this.facturaRepo = facturaRepo;
    }

    public EntregaDto getDeliveryByAdquisicion(Integer adquisicionId) {
        requireOwned(adquisicionId);
        return entregaRepo.findByAdquisicion(adquisicionId)
                .map(this::toDto)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Entrega no encontrada"));
    }

    public EntregaDto getPickupDetails(Integer adquisicionId) {
        EntregaDto dto = getDeliveryByAdquisicion(adquisicionId);
        if (!"retiro".equals(dto.tipo())) {
            throw ApiException.badRequest(ErrorCodes.INVALID_DATA, "La adquisicion no tiene retiro seleccionado");
        }
        return dto;
    }

    public EntregaDto getShippingStatus(Integer adquisicionId) {
        EntregaDto dto = getDeliveryByAdquisicion(adquisicionId);
        if (!"envio".equals(dto.tipo())) {
            throw ApiException.badRequest(ErrorCodes.INVALID_DATA, "La adquisicion no tiene envio seleccionado");
        }
        return dto;
    }

    @Transactional
    public EntregaDto selectShippingDelivery(Integer adquisicionId, EntregaRequest req) {
        RegistroDeSubasta r = requireOwned(adquisicionId);
        Entrega entrega = entregaRepo.findByAdquisicion(adquisicionId).orElseGet(Entrega::new);
        entrega.setAdquisicion(adquisicionId);
        entrega.setTipo("envio");
        entrega.setEstado("pendiente");
        entrega.setDireccion(req.direccion());
        entrega.setCodigoRetiro(null);
        entrega.setTransportista(req.transportista());
        entrega.setCodigoSeguimiento(resolveCodigoSeguimiento(req.codigoSeguimiento(), adquisicionId));
        entrega.setFechaEstimada(req.fechaEstimada());
        entregaRepo.save(entrega);

        actualizarCostoEnvio(r, req.costoEnvio());
        return toDto(entrega);
    }

    @Transactional
    public EntregaDto selectPickupDelivery(Integer adquisicionId, EntregaRequest req) {
        RegistroDeSubasta r = requireOwned(adquisicionId);
        Entrega entrega = entregaRepo.findByAdquisicion(adquisicionId).orElseGet(Entrega::new);
        entrega.setAdquisicion(adquisicionId);
        entrega.setTipo("retiro");
        entrega.setEstado("pendiente");
        entrega.setDireccion(null);
        entrega.setCodigoRetiro(resolveCodigoRetiro(req.codigoRetiro(), adquisicionId));
        entrega.setTransportista(null);
        entrega.setCodigoSeguimiento(null);
        entrega.setFechaEstimada(req.fechaEstimada());
        entregaRepo.save(entrega);

        actualizarCostoEnvio(r, null);
        return toDto(entrega);
    }

    @Transactional
    public EntregaDto confirmReception(Integer adquisicionId) {
        RegistroDeSubasta r = requireOwned(adquisicionId);
        if (!"pagado".equals(r.getEstado())) {
            throw ApiException.badRequest(ErrorCodes.INVALID_DATA, "La adquisicion debe estar pagada antes de confirmar la recepcion");
        }
        Entrega entrega = entregaRepo.findByAdquisicion(adquisicionId)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Entrega no encontrada"));
        entrega.setEstado("entregado");
        entregaRepo.save(entrega);
        r.setEstado("entregado");
        rdsRepo.save(r);
        return toDto(entrega);
    }

    private RegistroDeSubasta requireOwned(Integer id) {
        AuthPrincipal p = CurrentUser.requireCliente();
        RegistroDeSubasta r = rdsRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Adquisicion no encontrada"));
        if (!r.getCliente().equals(p.clienteId())) {
            throw ApiException.forbidden(ErrorCodes.FORBIDDEN, "Esta adquisicion no es tuya");
        }
        return r;
    }

    private void actualizarCostoEnvio(RegistroDeSubasta r, java.math.BigDecimal costoEnvio) {
        Factura factura = facturaRepo.findByAdquisicion(r.getIdentificador()).orElse(null);
        if (factura == null) {
            factura = new Factura();
            factura.setAdquisicion(r.getIdentificador());
            factura.setNumeroFactura("F" + r.getSubasta() + "-" + r.getIdentificador());
            factura.setImporte(r.getImporte());
            factura.setComision(r.getComision());
            factura.setFecha(LocalDateTime.now());
        }
        factura.setCostoEnvio(costoEnvio == null ? java.math.BigDecimal.ZERO : costoEnvio);
        factura.setTotal(nullToZero(factura.getImporte())
                .add(nullToZero(factura.getComision()))
                .add(nullToZero(factura.getCostoEnvio())));
        facturaRepo.save(factura);
    }

    private String resolveCodigoSeguimiento(String provided, Integer adquisicionId) {
        return provided == null || provided.isBlank() ? "ENV-" + adquisicionId : provided;
    }

    private String resolveCodigoRetiro(String provided, Integer adquisicionId) {
        return provided == null || provided.isBlank() ? "RET-" + adquisicionId : provided;
    }

    private java.math.BigDecimal nullToZero(java.math.BigDecimal value) {
        return value == null ? java.math.BigDecimal.ZERO : value;
    }

    private EntregaDto toDto(Entrega entrega) {
        return new EntregaDto(entrega.getId(), entrega.getAdquisicion(), entrega.getTipo(), entrega.getEstado(),
                entrega.getDireccion(), entrega.getCodigoRetiro(), entrega.getTransportista(),
                entrega.getCodigoSeguimiento(), entrega.getFechaEstimada());
    }
}