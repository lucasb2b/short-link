package br.com.lucas.shortlink.exceptions;

public class ShortCodeAlreadyExistsException extends RuntimeException {

    public ShortCodeAlreadyExistsException() {
        super("Código encurtado já existe");
    }
}