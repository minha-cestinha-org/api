package minhacestinha.api.service.security;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final Mensagens mensagens;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(mensagens.get("usuario.nao.encontrado")));
    }
}
