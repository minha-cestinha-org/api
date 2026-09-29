package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.MapeamentoDescricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MapeamentoDescricaoRepository extends JpaRepository<MapeamentoDescricao, Long> {

    Optional<MapeamentoDescricao> findByDescricaoBrutaAndMercadoId(String descricaoBruta, Long mercadoId);
}
