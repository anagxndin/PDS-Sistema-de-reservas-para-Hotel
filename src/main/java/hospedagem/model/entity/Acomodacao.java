package hospedagem.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

 
@Entity
@Table(name = "acomodacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acomodacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAcomodacao tipo;

    @Column(nullable = false)
    private Integer capacidade;

    @Column(name = "preco_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoDiaria;

    // Quantidade total de unidades desse tipo/nome no hotel.
    // Usado para calcular disponibilidade junto com as reservas no periodo.
    @Column(nullable = false)
    private Integer quantidadeTotal;

    @Column(name = "url_imagem", length = 300)
    private String urlImagem;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        if (this.ativo == null) {
            this.ativo = true;
        }
    }

    public enum TipoAcomodacao {
        SOLTEIRO, DUPLO, SUITE, FAMILIA
    }
}