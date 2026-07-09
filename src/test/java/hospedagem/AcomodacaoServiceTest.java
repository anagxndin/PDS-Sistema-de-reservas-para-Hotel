package hospedagem;

import hospedagem.controller.dto.AcomodacaoResponse;
import hospedagem.model.entity.Acomodacao;
import hospedagem.model.repository.AcomodacaoRepository;
import hospedagem.model.service.AcomodacaoService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcomodacaoServiceTest {

    @Mock
    private AcomodacaoRepository acomodacaoRepository;

    @InjectMocks
    private AcomodacaoService acomodacaoService;

    @Test
    void deveLancarErroQuandoCheckoutNaoForDepoisDoCheckin() {
        LocalDate checkin = LocalDate.now().plusDays(2);
        LocalDate checkout = LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> acomodacaoService.buscarDisponibilidade(checkin, checkout, null));
    }

    @Test
    void deveLancarErroQuandoCheckinForNoPassado() {
        LocalDate checkin = LocalDate.now().minusDays(1);
        LocalDate checkout = LocalDate.now().plusDays(2);

        assertThrows(IllegalArgumentException.class,
                () -> acomodacaoService.buscarDisponibilidade(checkin, checkout, null));
    }

    @Test
    void deveRetornarAcomodacoesDisponiveisNoPeriodo() {
        LocalDate checkin = LocalDate.now().plusDays(1);
        LocalDate checkout = LocalDate.now().plusDays(3);

        Acomodacao acomodacao = Acomodacao.builder()
                .id(1L)
                .nome("Suíte Master")
                .tipo(Acomodacao.TipoAcomodacao.SUITE)
                .capacidade(2)
                .precoDiaria(new BigDecimal("250.00"))
                .quantidadeTotal(3)
                .ativo(true)
                .build();

        when(acomodacaoRepository.findDisponiveisNoPeriodo(checkin, checkout, null))
                .thenReturn(List.of(acomodacao));

        List<AcomodacaoResponse> resultado = acomodacaoService.buscarDisponibilidade(checkin, checkout, null);

        assertEquals(1, resultado.size());
        assertEquals("Suíte Master", resultado.get(0).nome());
    }

    @Test
    void deveFiltrarAcomodacoesAtivasNaListagemSimples() {
        Acomodacao acomodacao = Acomodacao.builder()
                .id(2L)
                .nome("Quarto Duplo")
                .tipo(Acomodacao.TipoAcomodacao.DUPLO)
                .capacidade(2)
                .precoDiaria(new BigDecimal("150.00"))
                .quantidadeTotal(5)
                .ativo(true)
                .build();

        when(acomodacaoRepository.findByAtivoTrue()).thenReturn(List.of(acomodacao));

        List<AcomodacaoResponse> resultado = acomodacaoService.listarTodasAtivas();

        assertEquals(1, resultado.size());
        assertEquals("DUPLO", resultado.get(0).tipo());
    }
}