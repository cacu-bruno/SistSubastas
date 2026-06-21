package com.subastas.api.service;

import com.subastas.api.common.ApiException;
import com.subastas.api.common.ErrorCodes;
import com.subastas.api.domain.Producto;
import com.subastas.api.domain.Entrega;
import com.subastas.api.domain.Factura;
import com.subastas.api.domain.Multa;
import com.subastas.api.domain.RegistroDeSubasta;
import com.subastas.api.dto.AdquisicionDto;
import com.subastas.api.dto.AdquisicionResumenDto;
import com.subastas.api.dto.FacturaDto;
import com.subastas.api.repository.EntregaRepository;
import com.subastas.api.repository.FacturaRepository;
import com.subastas.api.repository.MultaRepository;
import com.subastas.api.repository.ProductoRepository;
import com.subastas.api.repository.RegistroDeSubastaRepository;
import com.subastas.api.security.AuthPrincipal;
import com.subastas.api.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdquisicionService {

    private final RegistroDeSubastaRepository rdsRepo;
    private final ProductoRepository productoRepo;
    private final FacturaRepository facturaRepo;
    private final EntregaRepository entregaRepo;
    private final MultaRepository multaRepo;

    public AdquisicionService(RegistroDeSubastaRepository rdsRepo, ProductoRepository productoRepo,
                              FacturaRepository facturaRepo, EntregaRepository entregaRepo,
                              MultaRepository multaRepo) {
        this.rdsRepo = rdsRepo;
        this.productoRepo = productoRepo;
        this.facturaRepo = facturaRepo;
        this.entregaRepo = entregaRepo;
        this.multaRepo = multaRepo;
    }

    public List<AdquisicionDto> listar(String estado) {
        AuthPrincipal p = CurrentUser.requireCliente();
        List<RegistroDeSubasta> regs = (estado != null)
                ? rdsRepo.findByClienteAndEstado(p.clienteId(), estado)
                : rdsRepo.findByCliente(p.clienteId());
        return regs.stream().map(this::toDto).toList();
    }

    public AdquisicionDto getById(Integer id) {
        AuthPrincipal p = CurrentUser.requireCliente();
        RegistroDeSubasta r = rdsRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Adquisicion no encontrada"));
        if (!r.getCliente().equals(p.clienteId())) {
            throw ApiException.forbidden(ErrorCodes.FORBIDDEN, "Esta adquisicion no es tuya");
        }
        return toDto(r);
    }

    public AdquisicionResumenDto resumen() {
        AuthPrincipal p = CurrentUser.requireCliente();
        List<RegistroDeSubasta> regs = rdsRepo.findByCliente(p.clienteId());
        long pendientes = regs.stream().filter(r -> estado(r).equals("pendiente")).count();
        long pagadas = regs.stream().filter(r -> estado(r).equals("pagado")).count();
        long entregadas = regs.stream().filter(r -> estado(r).equals("entregado")).count();
        long enMora = regs.stream().filter(r -> estado(r).equals("en_mora")).count();
        BigDecimal totalImporte = regs.stream().map(RegistroDeSubasta::getImporte).reduce(BigDecimal.ZERO, this::sum);
        BigDecimal totalComision = regs.stream().map(RegistroDeSubasta::getComision).reduce(BigDecimal.ZERO, this::sum);
        BigDecimal totalPagado = regs.stream()
                .map(this::totalDe)
                .reduce(BigDecimal.ZERO, this::sum);
        return new AdquisicionResumenDto(regs.size(), pendientes, pagadas, entregadas, enMora,
                totalImporte, totalComision, totalPagado);
    }

    public FacturaDto facturaFromAdquisicion(Integer id) {
        RegistroDeSubasta r = requireOwned(id);
        Factura f = facturaRepo.findByAdquisicion(r.getIdentificador())
                .orElseGet(() -> createFactura(r));
        return toFacturaDto(f);
    }

    private AdquisicionDto toDto(RegistroDeSubasta r) {
        String desc = productoRepo.findById(r.getProducto())
                .map(Producto::getDescripcionCatalogo).orElse(null);
        Factura factura = facturaRepo.findByAdquisicion(r.getIdentificador()).orElse(null);
        Entrega entrega = entregaRepo.findByAdquisicion(r.getIdentificador()).orElse(null);
        Multa multa = multaRepo.findByAdquisicion(r.getIdentificador()).orElse(null);
        return new AdquisicionDto(
                r.getIdentificador(),
                r.getProducto(),
                desc,
                r.getImporte(),
                r.getComision(),
                r.getEstado(),
                r.getFecha(),
                factura != null ? factura.getId() : null,
                factura != null ? factura.getTotal() : totalDe(r),
                entrega != null ? entrega.getId() : null,
                entrega != null ? entrega.getTipo() : null,
                entrega != null ? entrega.getEstado() : null,
                entrega != null ? entrega.getDireccion() : null,
                entrega != null ? entrega.getCodigoRetiro() : null,
                entrega != null ? entrega.getCodigoSeguimiento() : null,
                entrega != null ? entrega.getFechaEstimada() : null,
                multa != null ? multa.getId() : null,
                multa != null ? multa.getEstado() : null
        );
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

    private BigDecimal totalDe(RegistroDeSubasta r) {
        return nullToZero(r.getImporte()).add(nullToZero(r.getComision()));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal sum(BigDecimal left, BigDecimal right) {
        return nullToZero(left).add(nullToZero(right));
    }

    private String estado(RegistroDeSubasta r) {
        return r.getEstado() == null ? "pendiente" : r.getEstado();
    }

    private Factura createFactura(RegistroDeSubasta r) {
        Factura f = new Factura();
        f.setAdquisicion(r.getIdentificador());
        f.setNumeroFactura("F" + r.getSubasta() + "-" + r.getIdentificador());
        f.setImporte(r.getImporte());
        f.setComision(r.getComision());
        f.setCostoEnvio(BigDecimal.ZERO);
        f.setTotal(totalDe(r));
        f.setFecha(r.getFecha());
        return facturaRepo.save(f);
    }

    private FacturaDto toFacturaDto(Factura f) {
        return new FacturaDto(f.getId(), f.getAdquisicion(), f.getNumeroFactura(), f.getImporte(),
                f.getComision(), f.getCostoEnvio(), f.getTotal(), f.getFecha());
    }
}
