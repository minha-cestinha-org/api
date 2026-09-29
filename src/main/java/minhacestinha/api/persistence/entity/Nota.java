package minhacestinha.api.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import minhacestinha.api.persistence.entity.common.AuditoriaBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nota")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class Nota extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave_acesso")
    private String chaveAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id")
    private Mercado mercado;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    private BigDecimal descontos;

    @Column(name = "valor_pago")
    private BigDecimal valorPago;

    @Enumerated(EnumType.STRING)
    private OrigemNota origem;

    @Builder.Default
    private Boolean ativo = true;

    @Builder.Default
    @OrderBy("numero")
    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemNota> itens = new ArrayList<>();

    public void adicionarItem(ItemNota item) {
        item.setNota(this);
        itens.add(item);
    }
}
