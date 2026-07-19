package hospedagem.controller.dto;

import hospedagem.model.entity.DadosPagamento;
import hospedagem.model.entity.Reserva;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservaResponse {

    private Long id;
    private Long acomodacaoId;
    private String acomodacaoNome;
    private String acomodacaoTipo;
    private LocalDate checkin;
    private LocalDate checkout;
    private Reserva.StatusReserva status;
    private String cpf;
    private String telefone;
    private DadosPagamento.TipoPagamento formaPagamento;
    private String pagamentoUltimosDigitos;
    private BigDecimal precoDiaria;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;

    public static ReservaResponse fromEntity(Reserva reserva) {
        return ReservaResponse.builder()
                .id(reserva.getId())
                .acomodacaoId(reserva.getAcomodacao().getId())
                .acomodacaoNome(reserva.getAcomodacao().getNome())
                .acomodacaoTipo(reserva.getAcomodacao().getTipo().name())
                .checkin(reserva.getDataCheckin())
                .checkout(reserva.getDataCheckout())
                .status(reserva.getStatus())
                .cpf(reserva.getCpf())
                .telefone(reserva.getTelefone())
                .formaPagamento(reserva.getDadosPagamento().getTipo())
                .pagamentoUltimosDigitos(reserva.getDadosPagamento().getUltimosDigitos())
                .precoDiaria(reserva.getPrecoDiaria())
                .valorTotal(reserva.getValorTotal())
                .dataCriacao(reserva.getDataCriacao())
                .build();
    }
}