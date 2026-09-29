package minhacestinha.api.controller.impl;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.controller.ProdutoController;
import minhacestinha.api.dto.request.ProdutoPutRequest;
import minhacestinha.api.dto.response.ProdutoHistoricoResponse;
import minhacestinha.api.dto.response.ProdutoResumoResponse;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.service.produto.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProdutoControllerImpl implements ProdutoController {

    private final ProdutoService produtoService;

    @Override
    public ResponseEntity<List<ProdutoResumoResponse>> listar(User usuario) {
        return ResponseEntity.ok(produtoService.listar(usuario));
    }

    @Override
    public ResponseEntity<ProdutoHistoricoResponse> historico(Long id, User usuario) {
        return ResponseEntity.ok(produtoService.historico(id, usuario));
    }

    @Override
    public ResponseEntity<Void> atualizar(Long id, ProdutoPutRequest dto, User usuario) {
        produtoService.atualizar(id, dto, usuario);
        return ResponseEntity.noContent().build();
    }
}
