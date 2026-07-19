package hospedagem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Captura qualquer excecao nao tratada nos controllers e mostra uma tela de
 * erro generica, em vez do "Whitelabel Error Page" padrao do Spring.
 * O restante do grupo pode ir adicionando @ExceptionHandler mais especificos
 * aqui conforme forem criando as outras funcionalidades.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Erro nao tratado", ex);
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente.");
        return "error";
    }
}
