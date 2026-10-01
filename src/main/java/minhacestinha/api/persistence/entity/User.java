package minhacestinha.api.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import minhacestinha.api.persistence.entity.common.AuditoriaBase;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class User extends AuditoriaBase implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "senha")
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private UserRole role;

    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "termos_aceitos_em")
    private LocalDateTime termosAceitosEm;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    /** Vai dentro do token. Trocar a senha incrementa e os tokens antigos deixam de valer. */
    @Builder.Default
    @Column(name = "versao_token")
    private Integer versaoToken = 0;

    @Column(name = "senha_alterada_em")
    private LocalDateTime senhaAlteradaEm;

    /** Consentimento pro "preço da galera" (opt-in). */
    @Builder.Default
    @Column(name = "compartilhar_precos")
    private Boolean compartilharPrecos = false;

    @Column(name = "compartilhar_precos_em")
    private LocalDateTime compartilharPrecosEm;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return senha;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return Boolean.TRUE.equals(ativo);
    }
}
