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

/**
 * Cache global "descrição da nota + mercado → produto": o mesmo texto bruto nunca é padronizado duas vezes.
 */
@Entity
@Table(name = "mapeamento_descricao")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class MapeamentoDescricao extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao_bruta")
    private String descricaoBruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id")
    private Mercado mercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;
}
