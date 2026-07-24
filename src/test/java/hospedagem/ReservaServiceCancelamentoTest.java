package hospedagem;

import hospedagem.model.service.ReservaService;

import hospedagem.controller.dto.ReservaResponse;
import hospedagem.model.entity.Acomodacao;
import hospedagem.model.entity.Reserva;
import hospedagem.model.entity.User;
import hospedagem.model.exception.ReservaNaoPodeSerCanceladaException;
import hospedagem.model.repository.AcomodacaoRepository;
import hospedagem.model.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

 
@ExtendWith(MockitoExtension.class)
class ReservaServiceCancelamentoTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AcomodacaoRepository acomodacaoRepository;

    @Mock
    private AcomodacaoService acomodacaoService;

    private ReservaService reservaService;

    private User dono;
    private User outroUsuario;
    private Acomodacao acomodacao;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(reservaRepository, acomodacaoRepository, acomodacaoService);

        dono = User.builder().id(1L).nome("Dono").email("dono@teste.com").senha("hash").build();
        outroUsuario = User.builder().id(2L).nome("Outro").email("outro@teste.com").senha("hash").build();
        acomodacao = Acomodacao.builder().id(10L).nome("Suite Master").tipo(Acomodacao.TipoAcomodacao.SUITE)
                .precoDiaria(BigDecimal.valueOf(200)).build();
    }

    private Reserva reservaBase(LocalDate checkin, Reserva.StatusReserva status) {
        return Reserva.builder()
                .id(100L)
                .usuario(dono)
                .acomodacao(acomodacao)
                .dataCheckin(checkin)
                .dataCheckout(checkin.plusDays(2))
                .precoDiaria(BigDecimal.valueOf(200))
                .valorTotal(BigDecimal.valueOf(400))
                .status(status)
                .cpf("12345678900")
                .telefone("11999998888")
                .build();
    }

    @Test
    void deveCancelarReservaDentroDoPrazoPermitido() {
        Reserva reserva = reservaBase(LocalDate.now().plusDays(5), Reserva.StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse resposta = reservaService.cancelarReserva(dono, 100L, "Mudanca de planos");

        assertThat(resposta.getStatus()).isEqualTo(Reserva.StatusReserva.CANCELADA);
        verify(reservaRepository).save(argThat(r ->
                r.getStatus() == Reserva.StatusReserva.CANCELADA
                        && "Mudanca de planos".equals(r.getMotivoCancelamento())
                        && r.getDataCancelamento() != null));
    }

    @Test
    void deveCancelarSemMotivoInformado() {
        Reserva reserva = reservaBase(LocalDate.now().plusDays(5), Reserva.StatusReserva.PENDENTE);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse resposta = reservaService.cancelarReserva(dono, 100L, null);

        assertThat(resposta.getStatus()).isEqualTo(Reserva.StatusReserva.CANCELADA);
    }

    @Test
    void naoDeveCancelarReservaDeOutroUsuario() {
        Reserva reserva = reservaBase(LocalDate.now().plusDays(5), Reserva.StatusReserva.PENDENTE);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(outroUsuario, 100L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void naoDeveCancelarReservaJaCancelada() {
        Reserva reserva = reservaBase(LocalDate.now().plusDays(5), Reserva.StatusReserva.CANCELADA);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(dono, 100L, null))
                .isInstanceOf(ReservaNaoPodeSerCanceladaException.class)
                .hasMessageContaining("cancelada");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void naoDeveCancelarReservaComCheckinJaOcorrido() {
        Reserva reserva = reservaBase(LocalDate.now().minusDays(1), Reserva.StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(dono, 100L, null))
                .isInstanceOf(ReservaNaoPodeSerCanceladaException.class)
                .hasMessageContaining("ja ocorreu");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void naoDeveCancelarDentroDoPrazoMinimoDeAntecedencia() {
       
        Reserva reserva = reservaBase(LocalDate.now().plusDays(1), Reserva.StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(dono, 100L, null))
                .isInstanceOf(ReservaNaoPodeSerCanceladaException.class)
                .hasMessageContaining("antecedencia");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroQuandoReservaNaoExiste() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.cancelarReserva(dono, 999L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}s