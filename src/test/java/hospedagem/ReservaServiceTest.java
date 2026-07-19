package hospedagem;

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
import hospedagem.model.service.AcomodacaoService;
import hospedagem.model.service.ReservaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Todos os tipos usados aqui (Acomodacao, User, Reserva, AcomodacaoResponse -
 * este ultimo e um record com acessores id()/nome()/etc., sem prefixo "get")
 * foram validados contra os arquivos reais do projeto.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private AcomodacaoRepository acomodacaoRepository;
    @Mock private AcomodacaoService acomodacaoService;

    private ReservaService reservaService;

    private Acomodacao acomodacao;
    private User usuario;
    private ReservaRequest request;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(reservaRepository, acomodacaoRepository, acomodacaoService);

        acomodacao = Acomodacao.builder()
                .id(1L)
                .nome("Suíte Standard")
                .descricao("Suíte confortável")
                .tipo(Acomodacao.TipoAcomodacao.SUITE)
                .capacidade(2)
                .precoDiaria(new BigDecimal("200.00"))
                .quantidadeTotal(5)
                .ativo(true)
                .build();

        usuario = User.builder().id(10L).nome("Maria").email("maria@teste.com").build();

        DadosPagamentoRequest pagamentoPix = new DadosPagamentoRequest();
        pagamentoPix.setTipo(DadosPagamento.TipoPagamento.PIX);

        request = new ReservaRequest();
        request.setAcomodacaoId(1L);
        request.setCheckin(LocalDate.now().plusDays(5));
        request.setCheckout(LocalDate.now().plusDays(8));
        request.setCpf("111.444.777-35"); // CPF valido (digitos verificadores corretos) para fins de teste
        request.setTelefone("(34) 99999-0000");
        request.setDadosPagamento(pagamentoPix);
    }

    @Test
    void deveCriarReservaComSucessoQuandoAcomodacaoDisponivel() {
        when(acomodacaoRepository.findById(1L)).thenReturn(Optional.of(acomodacao));
        when(acomodacaoService.buscarDisponibilidade(any(), any(), any()))
                .thenReturn(List.of(AcomodacaoResponse.fromEntity(acomodacao)));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(99L);
            r.calcularValorTotal(); // simula o @PrePersist real da entidade
            return r;
        });

        ReservaResponse response = reservaService.criarReserva(usuario, request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getStatus()).isEqualTo(Reserva.StatusReserva.PENDENTE);
        assertThat(response.getValorTotal()).isEqualByComparingTo("600.00"); // 3 diarias x 200
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void deveLancarExcecaoQuandoAcomodacaoIndisponivelNoPeriodo() {
        when(acomodacaoRepository.findById(1L)).thenReturn(Optional.of(acomodacao));
        when(acomodacaoService.buscarDisponibilidade(any(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> reservaService.criarReserva(usuario, request))
                .isInstanceOf(AcomodacaoIndisponivelException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAcomodacaoNaoExiste() {
        when(acomodacaoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.criarReserva(usuario, request))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void deveLancarExcecaoQuandoCartaoVencido() {
        DadosPagamentoRequest pagamentoCartao = new DadosPagamentoRequest();
        pagamentoCartao.setTipo(DadosPagamento.TipoPagamento.CARTAO_CREDITO);
        pagamentoCartao.setNomeTitular("Maria Teste");
        pagamentoCartao.setNumeroCartao("4111111111111111");
        pagamentoCartao.setCvv("123");
        pagamentoCartao.setBandeira("Visa");
        pagamentoCartao.setValidade(YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MM/yy")));
        request.setDadosPagamento(pagamentoCartao);

        when(acomodacaoRepository.findById(1L)).thenReturn(Optional.of(acomodacao));
        when(acomodacaoService.buscarDisponibilidade(any(), any(), any()))
                .thenReturn(List.of(AcomodacaoResponse.fromEntity(acomodacao)));

        assertThatThrownBy(() -> reservaService.criarReserva(usuario, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vencido");
    }

    @Test
    void deveRejeitarCpfInvalidoNaValidacaoDoRequest() {
        // CPF e validado na camada de Bean Validation (@CPF em ReservaRequest),
        // nao dentro do ReservaService - por isso valida-se o Validator direto.
        request.setCpf("111.111.111-11"); // digitos repetidos: sempre invalido

        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            jakarta.validation.Validator validator = factory.getValidator();
            var violacoes = validator.validateProperty(request, "cpf");
            assertThat(violacoes).isNotEmpty();
        }
    }
}