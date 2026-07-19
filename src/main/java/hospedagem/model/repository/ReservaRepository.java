package hospedagem.model.repository;

import hospedagem.model.entity.Reserva;
import hospedagem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioOrderByDataCriacaoDesc(User usuario);
}