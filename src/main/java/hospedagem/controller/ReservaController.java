package hospedagem.controller;

import hospedagem.controller.dto.CancelamentoRequest;
import hospedagem.controller.dto.ReservaRequest;
import hospedagem.controller.dto.ReservaResponse;
import hospedagem.model.entity.DadosPagamento;
import hospedagem.model.entity.User;
import hospedagem.model.exception.AcomodacaoIndisponivelException;
import hospedagem.model.exception.ReservaNaoPodeSerCanceladaException;
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

     
    @GetMapping("/{id}/cancelar")
    public String telaCancelamento(@PathVariable Long id, @AuthenticationPrincipal User usuario, Model model) {
        model.addAttribute("reserva", reservaService.buscarPorId(id, usuario));
        if (!model.containsAttribute("cancelamentoRequest")) {
            model.addAttribute("cancelamentoRequest", new CancelamentoRequest());
        }
        return "reservas/cancelar";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                            @Valid @ModelAttribute("cancelamentoRequest") CancelamentoRequest request,
                            BindingResult bindingResult,
                            @AuthenticationPrincipal User usuario,
                            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("reserva", reservaService.buscarPorId(id, usuario));
            return "reservas/cancelar";
        }

        try {
            reservaService.cancelarReserva(usuario, id, request.getMotivo());
        } catch (IllegalArgumentException | ReservaNaoPodeSerCanceladaException ex) {
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("reserva", reservaService.buscarPorId(id, usuario));
            return "reservas/cancelar";
        }

        return "redirect:/reservas?cancelamentoSucesso";
    }

    private void carregarInfoAcomodacao(Long acomodacaoId, LocalDate checkin, LocalDate checkout, Model model) {
        try {
            model.addAttribute("acomodacao", acomodacaoService.buscarPorId(acomodacaoId));
        } catch (IllegalArgumentException ignored) {
             
        }
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
    }
}