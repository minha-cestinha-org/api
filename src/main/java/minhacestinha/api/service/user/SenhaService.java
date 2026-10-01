package minhacestinha.api.service.user;

import minhacestinha.api.dto.auth.RedefinirSenhaRequest;

public interface SenhaService {

    /** Manda um código por e-mail. Não diz se o e-mail existe, pra ninguém descobrir quem tem conta. */
    void solicitarRedefinicao(String email);

    /** Troca a senha se o código for válido. Tokens antigos deixam de valer. */
    void redefinir(RedefinirSenhaRequest dto);
}
