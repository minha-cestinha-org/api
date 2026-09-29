package minhacestinha.api.service.nota;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import minhacestinha.api.dto.request.NotaQrCodeRequest;
import minhacestinha.api.dto.response.NotaResponse;
import minhacestinha.api.dto.response.NotaResumoResponse;
import minhacestinha.api.message.Mensagens;
import minhacestinha.api.persistence.entity.ItemNota;
import minhacestinha.api.persistence.entity.Mercado;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.OrigemNota;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.mapper.NotaMapper;
import minhacestinha.api.persistence.repository.MercadoRepository;
import minhacestinha.api.persistence.repository.NotaRepository;
import minhacestinha.api.service.nfce.NfceLeitor;
import minhacestinha.api.service.nfce.NotaLida;
import minhacestinha.api.service.nfce.QrCodeNfce;
import minhacestinha.api.service.produto.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final MercadoRepository mercadoRepository;
    private final ProdutoService produtoService;
    private final NfceLeitor nfceLeitor;
    private final NotaMapper notaMapper;
    private final TransactionTemplate transactionTemplate;
    private final Mensagens mensagens;

    /**
     * A consulta à Sefaz fica fora da transação: ela pode levar segundos e não deve segurar conexão com o banco.
     */
    @Override
    public NotaResponse importarPorQrCode(NotaQrCodeRequest dto, User usuario) {
        QrCodeNfce qrCode = QrCodeNfce.ler(dto.conteudo());
        validarNaoImportada(usuario, qrCode.chave().valor());

        NotaLida notaLida = nfceLeitor.ler(qrCode);
        notaLida.avisos().forEach(aviso -> log.warn("Nota {}: {}", notaLida.chaveAcesso(), aviso));

        return transactionTemplate.execute(status -> notaMapper.toResponse(salvar(notaLida, usuario)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaResumoResponse> listar(User usuario) {
        return notaRepository.findByUsuarioIdAndAtivoTrueOrderByDataEmissaoDesc(usuario.getId())
                .stream()
                .map(notaMapper::toResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotaResponse buscar(Long id, User usuario) {
        return notaRepository.findByIdAndUsuarioIdAndAtivoTrue(id, usuario.getId())
                .map(notaMapper::toResponse)
                .orElseThrow(this::notaNaoEncontrada);
    }

    @Override
    @Transactional
    public void desativar(Long id, User usuario) {
        Nota nota = notaRepository.findByIdAndUsuarioId(id, usuario.getId()).orElseThrow(this::notaNaoEncontrada);
        nota.setAtivo(false);
        notaRepository.save(nota);
    }

    private Nota salvar(NotaLida notaLida, User usuario) {
        validarNaoImportada(usuario, notaLida.chaveAcesso());
        Mercado mercado = buscarOuCriarMercado(notaLida);

        Nota nota = Nota.builder()
                .chaveAcesso(notaLida.chaveAcesso())
                .usuario(usuario)
                .mercado(mercado)
                .dataEmissao(notaLida.dataEmissao())
                .valorTotal(notaLida.valorTotal())
                .descontos(notaLida.descontos())
                .valorPago(notaLida.valorPago())
                .origem(OrigemNota.QR)
                .ativo(true)
                .build();

        notaLida.itens().forEach(item -> nota.adicionarItem(ItemNota.builder()
                .numero(item.numero())
                .descricaoBruta(item.descricaoBruta())
                .codigo(item.codigo())
                .quantidade(item.quantidade())
                .unidade(item.unidade())
                .precoUnitario(item.precoUnitario())
                .precoTotal(item.precoTotal())
                .produto(produtoService.resolver(item, mercado))
                .build()));

        return notaRepository.save(nota);
    }

    private Mercado buscarOuCriarMercado(NotaLida notaLida) {
        NotaLida.MercadoLido lido = notaLida.mercado();
        return mercadoRepository.findByCnpj(lido.cnpj())
                .orElseGet(() -> mercadoRepository.save(Mercado.builder()
                        .cnpj(lido.cnpj())
                        .nome(lido.nome().isBlank() ? lido.cnpj() : lido.nome())
                        .endereco(lido.endereco())
                        .uf(notaLida.uf())
                        .build()));
    }

    private void validarNaoImportada(User usuario, String chaveAcesso) {
        if (notaRepository.existsByUsuarioIdAndChaveAcesso(usuario.getId(), chaveAcesso)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, mensagens.get("nota.ja.importada"));
        }
    }

    private ResponseStatusException notaNaoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensagens.get("nota.nao.encontrada"));
    }
}
