package minhacestinha.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import minhacestinha.api.persistence.entity.common.AuditoriaBase;

import java.time.LocalDateTime;

/** Código de 6 dígitos mandado por e-mail pra redefinir a senha. Só o hash fica no banco. */
@Entity
@Table(name = "redefinicao_senha")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class RedefinicaoSenha extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @Column(name = "codigo_hash")
    private String codigoHash;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Builder.Default
    private Integer tentativas = 0;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;
}
