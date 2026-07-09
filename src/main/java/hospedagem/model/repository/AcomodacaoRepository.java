package hospedagem.model.repository;

import hospedagem.model.entity.Acomodacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AcomodacaoRepository extends JpaRepository<Acomodacao, Long> {

    List<Acomodacao> findByAtivoTrue();

    List<Acomodacao> findByTipoAndAtivoTrue(Acomodacao.TipoAcomodacao tipo);

    /**
     * Retorna as acomodacoes ativas cuja quantidade de reservas ATIVAS
     * (PENDENTE ou CONFIRMADA) que se sobrepoem ao periodo informado
     * ainda nao atingiu a quantidadeTotal.
     *
     * Sobreposicao de intervalo classica: reserva.checkin < filtro.checkout
     * AND reserva.checkout > filtro.checkin.
     *
     * OBS: depende da entidade Reserva (a ser criada pelo grupo, ver
     * model/entity/Reserva.java) ter os campos dataCheckin, dataCheckout e status.
     */
    @Query("""
        SELECT a FROM Acomodacao a
        WHERE a.ativo = true
        AND (:tipo IS NULL OR a.tipo = :tipo)
        AND a.quantidadeTotal > (
            SELECT COUNT(r) FROM Reserva r
            WHERE r.acomodacao = a
            AND r.status IN ('PENDENTE', 'CONFIRMADA')
            AND r.dataCheckin < :checkout
            AND r.dataCheckout > :checkin
        )
        """)
    List<Acomodacao> findDisponiveisNoPeriodo(
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout,
            @Param("tipo") Acomodacao.TipoAcomodacao tipo
    );
}