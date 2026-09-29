package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.ItemNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
