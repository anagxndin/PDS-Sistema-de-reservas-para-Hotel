package hospedagem.model.service;

import hospedagem.controller.dto.AcomodacaoResponse;
import hospedagem.model.entity.Acomodacao;
import hospedagem.model.repository.AcomodacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AcomodacaoService {

    private final AcomodacaoRepository acomodacaoRepository;

    public AcomodacaoService(AcomodacaoRepository acomodacaoRepository) {
        this.acomodacaoRepository = acomodacaoRepository;
    }

    // Listagem simples, usada na tela "Tipos de acomodacao disponiveis".
    public List<AcomodacaoResponse> listarTodasAtivas() {
        return acomodacaoRepository.findByAtivoTrue()
                .stream()
                .map(AcomodacaoResponse::fromEntity)
                .toList();
    }

    public List<AcomodacaoResponse> listarPorTipo(Acomodacao.TipoAcomodacao tipo) {
        return acomodacaoRepository.findByTipoAndAtivoTrue(tipo)
                .stream()
                .map(AcomodacaoResponse::fromEntity)
                .toList();
    }

    // Usado pelo fluxo de reserva (ReservaController) para exibir os dados da
    // acomodacao escolhida na tela de "Confirme sua reserva".
    public AcomodacaoResponse buscarPorId(Long id) {
        Acomodacao acomodacao = acomodacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Acomodacao nao encontrada."));
        return AcomodacaoResponse.fromEntity(acomodacao);
    }

    /**
     * Consulta de disponibilidade por data (US "consultar disponibilidade de
     * quartos por data"). tipo pode ser null para nao filtrar por tipo.
     */
    public List<AcomodacaoResponse> buscarDisponibilidade(LocalDate checkin, LocalDate checkout,
                                                            Acomodacao.TipoAcomodacao tipo) {
        validarPeriodo(checkin, checkout);
        return acomodacaoRepository.findDisponiveisNoPeriodo(checkin, checkout, tipo)
                .stream()
                .map(AcomodacaoResponse::fromEntity)
                .toList();
    }

    private void validarPeriodo(LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new IllegalArgumentException("Informe data de check-in e check-out.");
        }
        if (!checkout.isAfter(checkin)) {
            throw new IllegalArgumentException("A data de check-out deve ser posterior ao check-in.");
        }
        if (checkin.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de check-in nao pode ser no passado.");
        }
    }

    // CRUD basico para administracao das acomodacoes (cadastro/edicao/remocao logica).
    public Acomodacao salvar(Acomodacao acomodacao) {
        return acomodacaoRepository.save(acomodacao);
    }

    public void desativar(Long id) {
        Acomodacao acomodacao = acomodacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Acomodacao nao encontrada."));
        acomodacao.setAtivo(false);
        acomodacaoRepository.save(acomodacao);
    }
}