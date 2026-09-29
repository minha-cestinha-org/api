package minhacestinha.api.persistence.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditoriaBase {

    @CreationTimestamp
    @Column(name = "data_inclusao", updatable = false)
    private LocalDateTime dtInclusao;

    @UpdateTimestamp
    @Column(name = "data_alteracao")
    private LocalDateTime dtAlteracao;
}
