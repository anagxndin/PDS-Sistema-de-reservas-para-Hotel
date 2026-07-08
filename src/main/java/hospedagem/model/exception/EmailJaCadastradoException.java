package hospedagem.model.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Ja existe uma conta cadastrada com o e-mail: " + email);
    }
}
