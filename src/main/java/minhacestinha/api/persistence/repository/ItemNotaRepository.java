package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.ItemNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemNotaRepository extends JpaRepository<ItemNota, Long> {

    @Query("""
            select i from ItemNota i
            join fetch i.nota n
            join fetch n.mercado
            join fetch i.produto
            where n.usuario.id = :usuarioId and n.ativo = true
            order by n.dataEmissao
            """)
    List<ItemNota> findDoUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
            select i from ItemNota i
            join fetch i.nota n
            join fetch n.mercado
            where n.usuario.id = :usuarioId and n.ativo = true and i.produto.id = :produtoId
            order by n.dataEmissao
            """)
    List<ItemNota> findDoUsuarioPorProduto(@Param("usuarioId") Long usuarioId, @Param("produtoId") Long produtoId);

    @Query("""
            select i from ItemNota i
            join fetch i.nota n
            join fetch i.produto
            where n.usuario.id = :usuarioId and n.ativo = true and n.dataEmissao >= :desde
            order by n.dataEmissao
            """)
    List<ItemNota> findDoUsuarioDesde(@Param("usuarioId") Long usuarioId, @Param("desde") LocalDateTime desde);

    @Modifying
    @Query("delete from ItemNota i where i.nota.id in (select n.id from Nota n where n.usuario.id = :usuarioId)")
    void apagarDoUsuario(@Param("usuarioId") Long usuarioId);
}
