package com.faculdade.hospedagem.controller.java;

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

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente.");
        return "error";
    }
}
