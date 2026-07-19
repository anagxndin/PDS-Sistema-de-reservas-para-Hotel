package hospedagem.controller;

import hospedagem.controller.dto.ReservaRequest;
import hospedagem.controller.dto.ReservaResponse;
import hospedagem.model.entity.DadosPagamento;
import hospedagem.model.entity.User;
import hospedagem.model.exception.AcomodacaoIndisponivelException;
import hospedagem.model.service.AcomodacaoService;
import hospedagem.model.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Telas Thymeleaf do fluxo de reserva, disparado a partir de um resultado da
 * busca de disponibilidade (AcomodacaoController#buscar):
 *   1) GET  /reservas/nova              -> formulario (dados pessoais + pagamento)
 *   2) POST /reservas                   -> cria a reserva
 *   3) GET  /reservas/{id}/confirmacao  -> tela de confirmacao
 *   4) GET  /reservas                   -> "Minhas reservas"
 * Endpoint JSON equivalente (para AJAX) fica em ReservaApiController.
 */
@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final AcomodacaoService acomodacaoService;

    public ReservaController(ReservaService reservaService, AcomodacaoService acomodacaoService) {
        this.reservaService = reservaService;
        this.acomodacaoService = acomodacaoService;
    }

    @GetMapping("/nova")
    public String novaReservaForm(@RequestParam Long acomodacaoId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
                                   Model model) {

        if (!model.containsAttribute("reservaRequest")) {
            ReservaRequest request = new ReservaRequest();
            request.setAcomodacaoId(acomodacaoId);
            request.setCheckin(checkin);
            request.setCheckout(checkout);
            model.addAttribute("reservaRequest", request);
        }

        carregarInfoAcomodacao(acomodacaoId, checkin, checkout, model);
        model.addAttribute("tiposPagamento", DadosPagamento.TipoPagamento.values());
        return "reservas/nova";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("reservaRequest") ReservaRequest request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal User usuario,
                         Model model) {

        model.addAttribute("tiposPagamento", DadosPagamento.TipoPagamento.values());
        carregarInfoAcomodacao(request.getAcomodacaoId(), request.getCheckin(), request.getCheckout(), model);

        if (bindingResult.hasErrors()) {
            return "reservas/nova";
        }

        try {
            ReservaResponse reserva = reservaService.criarReserva(usuario, request);
            return "redirect:/reservas/" + reserva.getId() + "/confirmacao";
        } catch (IllegalArgumentException | AcomodacaoIndisponivelException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "reservas/nova";
        }
    }

    @GetMapping("/{id}/confirmacao")
    public String confirmacao(@PathVariable Long id, @AuthenticationPrincipal User usuario, Model model) {
        model.addAttribute("reserva", reservaService.buscarPorId(id, usuario));
        return "reservas/confirmacao";
    }

    @GetMapping
    public String minhasReservas(@AuthenticationPrincipal User usuario, Model model) {
        model.addAttribute("reservas", reservaService.listarPorUsuario(usuario));
        return "reservas/minhas";
    }

    private void carregarInfoAcomodacao(Long acomodacaoId, LocalDate checkin, LocalDate checkout, Model model) {
        try {
            model.addAttribute("acomodacao", acomodacaoService.buscarPorId(acomodacaoId));
        } catch (IllegalArgumentException ignored) {
            // acomodacao invalida: o erro de validacao do form/service ja cobre esse caso
        }
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
    }
}