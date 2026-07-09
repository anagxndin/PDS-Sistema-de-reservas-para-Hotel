package hospedagem.controller;

import hospedagem.controller.dto.AcomodacaoResponse;
import hospedagem.controller.dto.BuscaDisponibilidadeRequest;
import hospedagem.model.entity.Acomodacao;
import hospedagem.model.service.AcomodacaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Telas Thymeleaf de listagem/consulta de acomodacoes.
 * Endpoint JSON de busca fica em AcomodacaoApiController (mesma logica,
 * exposta como REST caso o frontend passe a consumir via fetch/AJAX).
 */
@Controller
@RequestMapping("/acomodacoes")
public class AcomodacaoController {

    private final AcomodacaoService acomodacaoService;

    public AcomodacaoController(AcomodacaoService acomodacaoService) {
        this.acomodacaoService = acomodacaoService;
    }

    // US "visualizar tipos de acomodacao disponiveis" — listagem simples.
    @GetMapping
    public String listar(@RequestParam(required = false) Acomodacao.TipoAcomodacao tipo, Model model) {
        List<AcomodacaoResponse> acomodacoes = (tipo == null)
                ? acomodacaoService.listarTodasAtivas()
                : acomodacaoService.listarPorTipo(tipo);

        model.addAttribute("acomodacoes", acomodacoes);
        model.addAttribute("tipos", Acomodacao.TipoAcomodacao.values());
        model.addAttribute("tipoSelecionado", tipo);
        return "acomodacoes/listagem";
    }

    // US "consultar disponibilidade de quartos por data" — tela com form de busca.
    @GetMapping("/buscar")
    public String telaBusca(Model model) {
        if (!model.containsAttribute("buscaRequest")) {
            model.addAttribute("buscaRequest", new BuscaDisponibilidadeRequest());
        }
        model.addAttribute("tipos", Acomodacao.TipoAcomodacao.values());
        return "acomodacoes/busca";
    }

    @GetMapping("/resultado")
    public String buscar(@Valid @ModelAttribute("buscaRequest") BuscaDisponibilidadeRequest busca,
                          BindingResult bindingResult,
                          Model model) {
        model.addAttribute("tipos", Acomodacao.TipoAcomodacao.values());

        if (bindingResult.hasErrors()) {
            return "acomodacoes/busca";
        }

        try {
            List<AcomodacaoResponse> resultado = acomodacaoService.buscarDisponibilidade(
                    busca.getCheckin(), busca.getCheckout(), busca.getTipo());
            model.addAttribute("resultado", resultado);
            model.addAttribute("checkin", busca.getCheckin());
            model.addAttribute("checkout", busca.getCheckout());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
        }

        return "acomodacoes/resultado";
    }
}