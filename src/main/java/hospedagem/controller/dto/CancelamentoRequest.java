package hospedagem.controller.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelamentoRequest {

    @Size(max = 300, message = "O motivo deve ter no maximo 300 caracteres")
    private String motivo;
}