package com.subastas.api.service;

import com.subastas.api.common.ApiException;
import com.subastas.api.common.ErrorCodes;
import com.subastas.api.domain.Factura;
import com.subastas.api.domain.MedioPago;
import com.subastas.api.domain.Multa;
import com.subastas.api.domain.Pago;
import com.subastas.api.domain.RegistroDeSubasta;
import com.subastas.api.dto.PagoDto;
import com.subastas.api.dto.PagoRequest;
import com.subastas.api.repository.FacturaRepository;
import com.subastas.api.repository.MedioPagoRepository;
import com.subastas.api.repository.MultaRepository;
import com.subastas.api.repository.PagoRepository;
import com.subastas.api.repository.RegistroDeSubastaRepository;
import com.subastas.api.security.AuthPrincipal;
import com.subastas.api.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PagoService {

    private final PagoRepository pagoRepo;
    private final RegistroDeSubastaRepository rdsRepo;
    private final FacturaRepository facturaRepo;
    private final MedioPagoRepository medioPagoRepo;
    private final MultaRepository multaRepo;

    public PagoService(PagoRepository pagoRepo,
                       RegistroDeSubastaRepository rdsRepo,
                       FacturaRepository facturaRepo,
                       MedioPagoRepository medioPagoRepo,
                       MultaRepository multaRepo) {
        this.pagoRepo = pagoRepo;
        this.rdsRepo = rdsRepo;
        this.facturaRepo = facturaRepo;
        this.medioPagoRepo = medioPagoRepo;
        this.multaRepo = multaRepo;
    }

    @Transactional
    public PagoDto payAdquisicion(Integer adquisicionId, PagoRequest req) {
        AuthPrincipal p = CurrentUser.requireCliente();
        RegistroDeSubasta r = requireOwnedAdquisicion(adquisicionId, p);
        if (pagoRepo.existsByAdquisicion(adquisicionId)) {
            throw ApiException.badRequest(ErrorCodes.CONFLICT, "La adquisicion ya fue pagada");
        }

        MedioPago medio = requireVerifiedPaymentMethod(req.medioPagoId(), p.clienteId());
        Factura factura = facturaRepo.findByAdquisicion(adquisicionId)
                .orElseGet(() -> createFactura(r));

        Pago pago = new Pago();
        pago.setAdquisicion(adquisicionId);
        pago.setMedioPago(medio.getId());
        pago.setImporteTotal(factura.getTotal());
        pago.setMoneda(resolveMoneda(medio));
        pago.setEstado("pagado");
        pago.setFechaPago(LocalDateTime.now());
        pagoRepo.save(pago);

        r.setEstado("pagado");
        rdsRepo.save(r);

        return toDto(pago);
    }

    @Transactional
    public PagoDto payFine(Integer multaId, PagoRequest req) {
        AuthPrincipal p = CurrentUser.requireCliente();
        Multa multa = requireOwnedFine(multaId, p);
        if (pagoRepo.existsByMulta(multaId)) {
            throw ApiException.badRequest(ErrorCodes.CONFLICT, "La multa ya fue pagada");
        }

        MedioPago medio = requireVerifiedPaymentMethod(req.medioPagoId(), p.clienteId());

        Pago pago = new Pago();
        pago.setMulta(multaId);
        pago.setMedioPago(medio.getId());
        pago.setImporteTotal(multa.getImporte());
        pago.setMoneda(resolveMoneda(medio));
        pago.setEstado("pagado");
        pago.setFechaPago(LocalDateTime.now());
        pagoRepo.save(pago);

        multa.setEstado("paid");
        multaRepo.save(multa);

        return toDto(pago);
    }

    private Factura createFactura(RegistroDeSubasta r) {
        Factura f = new Factura();
        f.setAdquisicion(r.getIdentificador());
        f.setNumeroFactura("F" + r.getSubasta() + "-" + r.getIdentificador());
        f.setImporte(r.getImporte());
        f.setComision(r.getComision());
        f.setCostoEnvio(BigDecimal.ZERO);
        f.setTotal(nullToZero(r.getImporte()).add(nullToZero(r.getComision())));
        f.setFecha(LocalDateTime.now());
        return facturaRepo.save(f);
    }

    private RegistroDeSubasta requireOwnedAdquisicion(Integer id, AuthPrincipal p) {
        RegistroDeSubasta r = rdsRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Adquisicion no encontrada"));
        if (!r.getCliente().equals(p.clienteId())) {
            throw ApiException.forbidden(ErrorCodes.FORBIDDEN, "Esta adquisicion no es tuya");
        }
        return r;
    }

    private Multa requireOwnedFine(Integer id, AuthPrincipal p) {
        Multa multa = multaRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Multa no encontrada"));
        if (!multa.getCliente().equals(p.clienteId())) {
            throw ApiException.forbidden(ErrorCodes.FORBIDDEN, "Esta multa no es tuya");
        }
        return multa;
    }

    private MedioPago requireVerifiedPaymentMethod(Integer medioPagoId, Integer clienteId) {
        MedioPago medio = medioPagoRepo.findById(medioPagoId)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.PAYMENT_METHOD_NOT_FOUND, "Medio de pago no encontrado"));
        if (!clienteId.equals(medio.getCliente())) {
            throw ApiException.forbidden(ErrorCodes.NOT_OWNER, "Este medio de pago no es tuyo");
        }
        if (!"verified".equals(medio.getEstado())) {
            throw ApiException.forbidden(ErrorCodes.NO_VERIFIED_PAYMENT_METHOD, "El medio de pago debe estar verificado");
        }
        return medio;
    }

    private String resolveMoneda(MedioPago medio) {
        return medio.getMoneda() == null || medio.getMoneda().isBlank() ? "ARS" : medio.getMoneda();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private PagoDto toDto(Pago pago) {
        return new PagoDto(pago.getId(), pago.getAdquisicion(), pago.getMulta(), pago.getMedioPago(),
                pago.getImporteTotal(), pago.getMoneda(), pago.getEstado(), pago.getFechaPago());
    }
}