package hospedagem.controller;

import hospedagem.controller.dto.CancelamentoRequest;
import hospedagem.controller.dto.ReservaRequest;
import hospedagem.controller.dto.ReservaResponse;
import hospedagem.model.entity.User;
import hospedagem.model.exception.AcomodacaoIndisponivelException;
import hospedagem.model.exception.ReservaNaoPodeSerCanceladaException;
import hospedagem.model.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

 
@RestController
@RequestMapping("/api/reservas")
public class ReservaApiController {

    private final ReservaService reservaService;

    public ReservaApiController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> criar(@Valid @RequestBody ReservaRequest request,
                                                  @AuthenticationPrincipal User usuario) {
        ReservaResponse reserva = reservaService.criarReserva(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    @GetMapping
    public List<ReservaResponse> minhas(@AuthenticationPrincipal User usuario) {
        return reservaService.listarPorUsuario(usuario);
    }

    @GetMapping("/{id}")
    public ReservaResponse buscar(@PathVariable Long id, @AuthenticationPrincipal User usuario) {
        return reservaService.buscarPorId(id, usuario);
    }

 
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id,
                                                      @RequestBody(required = false) CancelamentoRequest request,
                                                      @AuthenticationPrincipal User usuario) {
        String motivo = request != null ? request.getMotivo() : null;
        ReservaResponse reserva = reservaService.cancelarReserva(usuario, id, motivo);
        return ResponseEntity.ok(reserva);
    }

    @ExceptionHandler({IllegalArgumentException.class, AcomodacaoIndisponivelException.class,
            ReservaNaoPodeSerCanceladaException.class})
    public ResponseEntity<Map<String, String>> handleValidacao(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }
}