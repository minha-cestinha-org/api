package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.ProdutoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoUsuarioRepository extends JpaRepository<ProdutoUsuario, Long> {

    List<ProdutoUsuario> findByUsuarioId(Long usuarioId);

    Optional<ProdutoUsuario> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);

    @Modifying
    @Query("delete from ProdutoUsuario p where p.usuario.id = :usuarioId")
    void apagarDoUsuario(@Param("usuarioId") Long usuarioId);
}
