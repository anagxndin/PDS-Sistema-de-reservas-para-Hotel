package hospedagem.controller.dto;

import hospedagem.model.entity.Acomodacao;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Formato dos dados enviados pelo formulario/filtro de busca por data.
public class BuscaDisponibilidadeRequest {

    @NotNull(message = "Informe a data de check-in.")
    @FutureOrPresent(message = "A data de check-in nao pode ser no passado.")
    private LocalDate checkin;

    @NotNull(message = "Informe a data de check-out.")
    private LocalDate checkout;

    // Opcional — null significa "todos os tipos".
    private Acomodacao.TipoAcomodacao tipo;

    public LocalDate getCheckin() { return checkin; }
    public void setCheckin(LocalDate checkin) { this.checkin = checkin; }

    public LocalDate getCheckout() { return checkout; }
    public void setCheckout(LocalDate checkout) { this.checkout = checkout; }

    public Acomodacao.TipoAcomodacao getTipo() { return tipo; }
    public void setTipo(Acomodacao.TipoAcomodacao tipo) { this.tipo = tipo; }
}