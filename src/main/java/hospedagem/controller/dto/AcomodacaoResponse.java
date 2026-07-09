package hospedagem.controller.dto;

import hospedagem.model.entity.Acomodacao;

import java.math.BigDecimal;

/**
 * DTO de saida — evita expor a entidade JPA direto na view/API
 * e serve tanto para o Thymeleaf quanto para um eventual endpoint JSON.
 */
public record AcomodacaoResponse(
        Long id,
        String nome,
        String descricao,
        String tipo,
        Integer capacidade,
        BigDecimal precoDiaria,
        String urlImagem
) {
    public static AcomodacaoResponse fromEntity(Acomodacao a) {
        return new AcomodacaoResponse(
                a.getId(),
                a.getNome(),
                a.getDescricao(),
                a.getTipo().name(),
                a.getCapacidade(),
                a.getPrecoDiaria(),
                a.getUrlImagem()
        );
    }
}