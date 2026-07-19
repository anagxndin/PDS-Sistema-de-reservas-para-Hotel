package hospedagem.model.service;

import hospedagem.controller.dto.AcomodacaoResponse;
import hospedagem.controller.dto.DadosPagamentoRequest;
import hospedagem.controller.dto.ReservaRequest;
import hospedagem.controller.dto.ReservaResponse;
import hospedagem.model.entity.Acomodacao;
import hospedagem.model.entity.DadosPagamento;
import hospedagem.model.entity.Reserva;
import hospedagem.model.entity.User;
import hospedagem.model.exception.AcomodacaoIndisponivelException;
import hospedagem.model.repository.AcomodacaoRepository;
import hospedagem.model.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final AcomodacaoRepository acomodacaoRepository;
    private final AcomodacaoService acomodacaoService;

    public ReservaService(ReservaRepository reservaRepository,
                           AcomodacaoRepository acomodacaoRepository,
                           AcomodacaoService acomodacaoService) {
        this.reservaRepository = reservaRepository;
        this.acomodacaoRepository = acomodacaoRepository;
        this.acomodacaoService = acomodacaoService;
    }

    /**
     * US "registro de reserva" + US "fornecer informacoes pessoais".
     * Fluxo: acomodacao existe -> ainda esta disponivel no periodo (revalidado
     * aqui, nao so na tela de busca, para evitar corrida entre dois usuarios
     * reservando a mesma vaga ao mesmo tempo) -> dados de pagamento validos ->
     * persiste com status PENDENTE. valorTotal e calculado pela propria
     * entidade Reserva (@PrePersist -> calcularValorTotal), nao aqui.
     */
    @Transactional
    public ReservaResponse criarReserva(User usuario, ReservaRequest request) {
        Acomodacao acomodacao = acomodacaoRepository.findById(request.getAcomodacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Acomodacao nao encontrada."));

        // Reaproveita a validacao de periodo (datas nulas/invertidas/no passado)
        // e a regra de disponibilidade (quantidadeTotal vs reservas ativas
        // sobrepostas) ja usadas na tela de busca.
        List<AcomodacaoResponse> disponiveis = acomodacaoService.buscarDisponibilidade(
                request.getCheckin(), request.getCheckout(), acomodacao.getTipo());

        boolean disponivel = disponiveis.stream()
                .anyMatch(a -> Objects.equals(a.id(), acomodacao.getId()));
        if (!disponivel) {
            throw new AcomodacaoIndisponivelException(
                    "Essa acomodacao nao esta mais disponivel para o periodo selecionado.");
        }

        DadosPagamento dadosPagamento = validarEMontarPagamento(request.getDadosPagamento());

        Reserva reserva = Reserva.builder()
                .acomodacao(acomodacao)
                .usuario(usuario)
                .dataCheckin(request.getCheckin())
                .dataCheckout(request.getCheckout())
                .precoDiaria(acomodacao.getPrecoDiaria())
                .status(Reserva.StatusReserva.PENDENTE)
                .cpf(request.getCpf().replaceAll("\\D", ""))
                .telefone(request.getTelefone().replaceAll("\\D", ""))
                .dadosPagamento(dadosPagamento)
                .build();

        reserva = reservaRepository.save(reserva); // @PrePersist calcula valorTotal
        return ReservaResponse.fromEntity(reserva);
    }

    public List<ReservaResponse> listarPorUsuario(User usuario) {
        return reservaRepository.findByUsuarioOrderByDataCriacaoDesc(usuario)
                .stream()
                .map(ReservaResponse::fromEntity)
                .toList();
    }

    public ReservaResponse buscarPorId(Long id, User usuario) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva nao encontrada."));
        if (!Objects.equals(reserva.getUsuario().getId(), usuario.getId())) {
            // mensagem generica de proposito: nao revela que o id existe e pertence a outro usuario
            throw new IllegalArgumentException("Reserva nao encontrada.");
        }
        return ReservaResponse.fromEntity(reserva);
    }

    private DadosPagamento validarEMontarPagamento(DadosPagamentoRequest dto) {
        if (dto == null || dto.getTipo() == null) {
            throw new IllegalArgumentException("Selecione a forma de pagamento.");
        }

        if (dto.getTipo() == DadosPagamento.TipoPagamento.PIX) {
            return DadosPagamento.builder()
                    .tipo(DadosPagamento.TipoPagamento.PIX)
                    .build();
        }

        // Cartao de credito/debito
        if (isBlank(dto.getNomeTitular())) {
            throw new IllegalArgumentException("Informe o nome do titular do cartao.");
        }
        String numero = dto.getNumeroCartao() == null ? "" : dto.getNumeroCartao().replaceAll("\\D", "");
        if (numero.length() < 13 || numero.length() > 19) {
            throw new IllegalArgumentException("Numero de cartao invalido.");
        }
        if (dto.getCvv() == null || !dto.getCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV invalido.");
        }

        YearMonth validade = parseValidade(dto.getValidade());
        if (validade.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Cartao vencido.");
        }

        // numero e dto.getCvv() completos nunca sao gravados: usamos so os
        // 4 ultimos digitos abaixo, o resto sai de escopo aqui de proposito.
        return DadosPagamento.builder()
                .tipo(dto.getTipo())
                .nomeTitular(dto.getNomeTitular())
                .ultimosDigitos(numero.substring(numero.length() - 4))
                .bandeira(dto.getBandeira())
                .validadeMes(validade.getMonthValue())
                .validadeAno(validade.getYear())
                .build();
    }

    private YearMonth parseValidade(String validade) {
        if (validade == null || !validade.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Validade do cartao invalida. Use o formato MM/AA.");
        }
        String[] partes = validade.split("/");
        int mes = Integer.parseInt(partes[0]);
        int ano = 2000 + Integer.parseInt(partes[1]);
        return YearMonth.of(ano, mes);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}