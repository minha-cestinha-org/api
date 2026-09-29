package minhacestinha.api.persistence.repository;

import minhacestinha.api.persistence.entity.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    Optional<Mercado> findByCnpj(String cnpj);
}
