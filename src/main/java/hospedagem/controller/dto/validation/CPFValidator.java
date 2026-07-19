package hospedagem.controller.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null) {
            return false;
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false; // rejeita tamanho errado e sequencias tipo 111.111.111-11
        }
        return digitoValido(digits, 9) && digitoValido(digits, 10);
    }

    private boolean digitoValido(String cpf, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * ((posicao + 1) - i);
        }
        int resto = soma % 11;
        int digitoEsperado = (resto < 2) ? 0 : 11 - resto;
        return digitoEsperado == Character.getNumericValue(cpf.charAt(posicao));
    }
}