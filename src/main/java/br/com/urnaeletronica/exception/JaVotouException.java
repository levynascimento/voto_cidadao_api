package br.com.urnaeletronica.exception;

public class JaVotouException extends RuntimeException {
    public JaVotouException() {
        super("O eleitor já realizou sua votação");
    }
}
