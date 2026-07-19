package hospedagem.model.exception;
 
public class AcomodacaoIndisponivelException extends RuntimeException {

    public AcomodacaoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}