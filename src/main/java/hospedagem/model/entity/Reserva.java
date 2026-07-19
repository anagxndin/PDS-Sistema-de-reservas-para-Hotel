package hospedagem.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acomodacao_id", nullable = false)
    private Acomodacao acomodacao;

    @NotNull
    @Column(name = "data_checkin", nullable = false)
    private LocalDate dataCheckin;

    @NotNull
    @Column(name = "data_checkout", nullable = false)
    private LocalDate dataCheckout;

    @Column(name = "preco_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoDiaria;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // ---- US "fornecer informacoes pessoais para completar a reserva" ----
    // Nome/e-mail ja vem do usuario logado (this.usuario); aqui so o que falta.
    @Column(nullable = false, length = 14)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Embedded
    private DadosPagamento dadosPagamento;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = this.dataCriacao;
        if (this.status == null) {
            this.status = StatusReserva.PENDENTE;
        }
        calcularValorTotal();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void calcularValorTotal() {
        if (dataCheckin != null && dataCheckout != null && precoDiaria != null) {
            long noites = ChronoUnit.DAYS.between(dataCheckin, dataCheckout);
            this.valorTotal = precoDiaria.multiply(BigDecimal.valueOf(noites));
        }
    }

    public enum StatusReserva {
        PENDENTE, CONFIRMADA, CANCELADA
    }
}