package hospedagem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * IMPORTANTE (seguranca / PCI-DSS): numero completo do cartao e CVV NUNCA sao
 * persistidos. So guardamos os 4 ultimos digitos (para exibir "final 1234"
 * na confirmacao) e metadados nao sensiveis. Ver ReservaService, metodo
 * validarEMontarPagamento, onde numero/cvv completos sao descartados logo
 * apos a validacao. Um sistema real de pagamento usaria um gateway
 * (Stripe/PagSeguro/Mercado Pago) que tokeniza o cartao e nunca deixa esse
 * dado passar pelo seu backend.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadosPagamento {

    @Enumerated(EnumType.STRING)
    @Column(name = "pagamento_tipo", nullable = false, length = 20)
    private TipoPagamento tipo;

    @Column(name = "pagamento_nome_titular", length = 100)
    private String nomeTitular;

    @Column(name = "pagamento_ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(name = "pagamento_bandeira", length = 30)
    private String bandeira;

    @Column(name = "pagamento_validade_mes")
    private Integer validadeMes;

    @Column(name = "pagamento_validade_ano")
    private Integer validadeAno;

    public enum TipoPagamento {
        CARTAO_CREDITO, CARTAO_DEBITO, PIX
    }
}