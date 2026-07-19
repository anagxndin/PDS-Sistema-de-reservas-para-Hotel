package hospedagem.controller.dto;

import hospedagem.model.entity.DadosPagamento.TipoPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * numeroCartao e cvv sao usados apenas para validacao no ReservaService e
 * NUNCA sao persistidos (ver DadosPagamento e ReservaService). Obrigatorios
 * apenas quando tipo != PIX; essa validacao condicional e feita no service,
 * porque Bean Validation puro nao expressa bem "obrigatorio se outro campo
 * for X" sem grupos/validador customizado adicional.
 */
@Data
public class DadosPagamentoRequest {

    @NotNull(message = "Selecione a forma de pagamento")
    private TipoPagamento tipo;

    private String nomeTitular;
    private String numeroCartao;
    private String validade; // formato MM/AA
    private String cvv;
    private String bandeira;
}