package hospedagem.controller.dto;

import hospedagem.controller.dto.validation.CPF;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ReservaRequest {

    @NotNull(message = "Selecione uma acomodacao")
    private Long acomodacaoId;

    @NotNull(message = "Informe a data de check-in")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkin;

    @NotNull(message = "Informe a data de check-out")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkout;

    @NotBlank(message = "Informe o CPF")
    @CPF(message = "CPF invalido")
    private String cpf;

    @NotBlank(message = "Informe o telefone")
    @Pattern(regexp = "\\D*\\d{2}\\D*\\d{4,5}\\D*\\d{4}\\D*", message = "Telefone invalido")
    private String telefone;

    @NotNull(message = "Informe os dados de pagamento")
    @Valid
    private DadosPagamentoRequest dadosPagamento;
}