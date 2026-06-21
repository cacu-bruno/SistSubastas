package com.subastas.api.service;

import com.subastas.api.common.ApiException;
import com.subastas.api.common.ErrorCodes;
import com.subastas.api.domain.Multa;
import com.subastas.api.dto.MultaDto;
import com.subastas.api.dto.PagoDto;
import com.subastas.api.dto.PagoRequest;
import com.subastas.api.repository.MultaRepository;
import com.subastas.api.service.PagoService;
import com.subastas.api.security.AuthPrincipal;
import com.subastas.api.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultaService {

    private final MultaRepository repo;
    private final PagoService pagoService;

    public MultaService(MultaRepository repo, PagoService pagoService) {
        this.repo = repo;
        this.pagoService = pagoService;
    }

    public List<MultaDto> listar() {
        AuthPrincipal p = CurrentUser.requireCliente();
        return repo.findByCliente(p.clienteId()).stream()
                .map(m -> new MultaDto(m.getId(), m.getAdquisicion(), m.getImporte(), m.getEstado(), m.getFechaLimite()))
                .toList();
    }

    public MultaDto getById(Integer id) {
        AuthPrincipal p = CurrentUser.requireCliente();
        Multa m = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound(ErrorCodes.NOT_FOUND, "Multa no encontrada"));
        if (!m.getCliente().equals(p.clienteId())) {
            throw ApiException.forbidden(ErrorCodes.FORBIDDEN, "Esta multa no es tuya");
        }
        return new MultaDto(m.getId(), m.getAdquisicion(), m.getImporte(), m.getEstado(), m.getFechaLimite());
    }

    public PagoDto payFine(Integer id, PagoRequest req) {
        return pagoService.payFine(id, req);
    }
}
