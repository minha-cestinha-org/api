package minhacestinha.api.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String ISSUER_ACCESS = "minhacestinha_api";
    private static final String ISSUER_REFRESH = "minhacestinha_refresh";
    private static final String CLAIM_VERSAO = "versao";
    private static final Duration DURACAO_ACCESS = Duration.ofHours(1);
    private static final Duration DURACAO_REFRESH = Duration.ofDays(30);

    private final Mensagens mensagens;

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        return gerar(user, ISSUER_ACCESS, DURACAO_ACCESS, "auth.erro.gerar.token");
    }

    public String generateRefreshToken(User user) {
        return gerar(user, ISSUER_REFRESH, DURACAO_REFRESH, "auth.erro.gerar.refresh");
    }

    /** Devolve o email (subject) de um access token válido, ou {@code null}. */
    public String validateToken(String token) {
        return validar(token, ISSUER_ACCESS);
    }

    /** Devolve o email (subject) de um refresh token válido, ou {@code null}. */
    public String validateRefreshToken(String token) {
        return validar(token, ISSUER_REFRESH);
    }

    /** O token foi emitido antes da última troca de senha? Token sem versão conta como 0. */
    public boolean versaoValida(String token, User user) {
        Integer versao = JWT.decode(token).getClaim(CLAIM_VERSAO).asInt();
        return (versao == null ? 0 : versao) == user.getVersaoToken();
    }

    private String gerar(User user, String issuer, Duration duracao, String codigoErro) {
        try {
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(user.getEmail())
                    .withClaim("role", user.getRole().name())
                    .withClaim(CLAIM_VERSAO, user.getVersaoToken())
                    .withExpiresAt(Instant.now().plus(duracao))
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception) {
            throw new IllegalStateException(mensagens.get(codigoErro), exception);
        }
    }

    private String validar(String token, String issuer) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
