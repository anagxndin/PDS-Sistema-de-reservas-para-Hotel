package hospedagem.model.exception;

 
public class ReservaNaoPodeSerCanceladaException extends RuntimeException {

    public ReservaNaoPodeSerCanceladaException(String message) {
        super(message);
    }
}