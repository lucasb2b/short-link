package br.com.lucas.shortlink.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Não autorizado");
    }
}