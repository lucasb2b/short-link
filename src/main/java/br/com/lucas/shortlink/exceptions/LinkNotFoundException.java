package br.com.lucas.shortlink.exceptions;

public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException() {
        super("Link não encontrado");
    }
}