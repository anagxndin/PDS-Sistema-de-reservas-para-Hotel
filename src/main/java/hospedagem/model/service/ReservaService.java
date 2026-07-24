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
import hospedagem.model.exception.ReservaNaoPodeSerCanceladaException;
import hospedagem.model.repository.AcomodacaoRepository;
import hospedagem.model.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
public class ReservaService {

     
    private static final long HORAS_ANTECEDENCIA_MINIMA_CANCELAMENTO = 24;

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

   
    @Transactional
    public ReservaResponse criarReserva(User usuario, ReservaRequest request) {
        Acomodacao acomodacao = acomodacaoRepository.findById(request.getAcomodacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Acomodacao nao encontrada."));

       
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

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorUsuario(User usuario) {
        return reservaRepository.findByUsuarioOrderByDataCriacaoDesc(usuario)
                .stream()
                .map(ReservaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id, User usuario) {
        Reserva reserva = buscarEValidarDono(id, usuario);
        return ReservaResponse.fromEntity(reserva);
    }

    
    @Transactional
    public ReservaResponse cancelarReserva(User usuario, Long reservaId, String motivo) {
        Reserva reserva = buscarEValidarDono(reservaId, usuario);

        if (reserva.getStatus() == Reserva.StatusReserva.CANCELADA) {
            throw new ReservaNaoPodeSerCanceladaException("Esta reserva ja esta cancelada.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioCheckin = reserva.getDataCheckin().atStartOfDay();

        if (!inicioCheckin.isAfter(agora)) {
            throw new ReservaNaoPodeSerCanceladaException(
                    "Nao e possivel cancelar: o check-in desta reserva ja ocorreu.");
        }

        if (agora.isAfter(inicioCheckin.minusHours(HORAS_ANTECEDENCIA_MINIMA_CANCELAMENTO))) {
            throw new ReservaNaoPodeSerCanceladaException(
                    "Cancelamento nao permitido: e preciso cancelar com pelo menos "
                            + HORAS_ANTECEDENCIA_MINIMA_CANCELAMENTO + "h de antecedencia do check-in.");
        }

        reserva.setStatus(Reserva.StatusReserva.CANCELADA);
        reserva.setDataCancelamento(agora);
        reserva.setMotivoCancelamento(motivo);

        Reserva salva = reservaRepository.save(reserva);
        return ReservaResponse.fromEntity(salva);
    }

     
    private Reserva buscarEValidarDono(Long id, User usuario) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva nao encontrada."));
        if (!Objects.equals(reserva.getUsuario().getId(), usuario.getId())) {
            throw new IllegalArgumentException("Reserva nao encontrada.");
        }
        return reserva;
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