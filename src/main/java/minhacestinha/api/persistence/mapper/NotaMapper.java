package minhacestinha.api.persistence.mapper;

import minhacestinha.api.dto.response.ItemNotaResponse;
import minhacestinha.api.dto.response.MercadoResponse;
import minhacestinha.api.dto.response.NotaResponse;
import minhacestinha.api.dto.response.NotaResumoResponse;
import minhacestinha.api.persistence.entity.ItemNota;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Nota;
import org.springframework.stereotype.Component;

@Component
public class NotaMapper {

    public NotaResponse toResponse(Nota nota) {
        return new NotaResponse(
                nota.getId(),
                nota.getChaveAcesso(),
                toResponse(nota.getMercado()),
                nota.getDataEmissao(),
                nota.getValorTotal(),
                nota.getDescontos(),
                nota.getValorPago(),
                nota.getItens().stream().map(this::toResponse).toList());
    }

    public NotaResumoResponse toResumo(Nota nota) {
        return new NotaResumoResponse(
                nota.getId(),
                nota.getMercado().getNome(),
                nota.getDataEmissao(),
                nota.getValorPago(),
                nota.getItens().size());
    }

    public MercadoResponse toResponse(Mercado mercado) {
        return new MercadoResponse(mercado.getId(), mercado.getCnpj(), mercado.getNome(), mercado.getEndereco());
    }

    private ItemNotaResponse toResponse(ItemNota item) {
        return new ItemNotaResponse(
                item.getNumero(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getDescricaoBruta(),
                item.getQuantidade(),
                item.getUnidade(),
                item.getPrecoUnitario(),
                item.getPrecoTotal());
    }
}
