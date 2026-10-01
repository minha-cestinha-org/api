package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.RedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RedefinicaoSenhaRepository extends JpaRepository<RedefinicaoSenha, Long> {

    Optional<RedefinicaoSenha> findFirstByUsuarioIdOrderByIdDesc(Long usuarioId);

    @Modifying
    @Query("update RedefinicaoSenha r set r.usadoEm = :agora where r.usuario.id = :usuarioId and r.usadoEm is null")
    void invalidarPendentes(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Modifying
    @Query("delete from RedefinicaoSenha r where r.usuario.id = :usuarioId")
    void apagarDoUsuario(@Param("usuarioId") Long usuarioId);
}
