package hospedagem.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import hospedagem.controller.dto.AcomodacaoResponse;
import hospedagem.model.entity.Acomodacao;
import hospedagem.model.service.AcomodacaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoint REST usado pelo componente de data no front (AJAX/fetch), para
 * quando a tela de resultados for atualizada via JS sem recarregar a pagina.
 * Retorna JSON puro, sem view.
 */
@RestController
@RequestMapping("/api/acomodacoes")
public class AcomodacaoApiController {

    private final AcomodacaoService acomodacaoService;

    public AcomodacaoApiController(AcomodacaoService acomodacaoService) {
        this.acomodacaoService = acomodacaoService;
    }

    @GetMapping("/disponibilidade")
    public List<AcomodacaoResponse> buscarDisponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @RequestParam(required = false) Acomodacao.TipoAcomodacao tipo) {

        return acomodacaoService.buscarDisponibilidade(checkin, checkout, tipo);
    }
}