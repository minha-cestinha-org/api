package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.Nota;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    boolean existsByUsuarioIdAndChaveAcesso(Long usuarioId, String chaveAcesso);

    @EntityGraph(attributePaths = "mercado")
    List<Nota> findByUsuarioIdAndAtivoTrueOrderByDataEmissaoDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"mercado", "itens", "itens.produto"})
    Optional<Nota> findByIdAndUsuarioIdAndAtivoTrue(Long id, Long usuarioId);

    Optional<Nota> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Nota> findByUsuarioIdAndAtivoTrueAndDataEmissaoGreaterThanEqual(Long usuarioId, LocalDateTime desde);

    @EntityGraph(attributePaths = {"mercado", "itens", "itens.produto"})
    List<Nota> findByUsuarioIdOrderByDataEmissaoDesc(Long usuarioId);

    @Modifying
    @Query("delete from Nota n where n.usuario.id = :usuarioId")
    void apagarDoUsuario(@Param("usuarioId") Long usuarioId);
}
