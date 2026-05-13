package br.com.lucas.shortlink.exceptions;

public class LinkRevokedException extends RuntimeException {

    public LinkRevokedException(){
        super("Este link foi revogado");
    }
}
