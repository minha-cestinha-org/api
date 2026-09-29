package minhacestinha.api.service.gasto;

import lombok.RequiredArgsConstructor;
import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.NotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GastoServiceImpl implements GastoService {

    private final NotaRepository notaRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<GastoMensalResponse> porMes(User usuario, int meses) {
        YearMonth atual = YearMonth.now(clock);
        YearMonth primeiro = atual.minusMonths(meses - 1L);

        Map<YearMonth, List<Nota>> notasPorMes = notaRepository
                .findByUsuarioIdAndAtivoTrueAndDataEmissaoGreaterThanEqual(usuario.getId(), primeiro.atDay(1).atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(nota -> YearMonth.from(nota.getDataEmissao())));

        return IntStream.range(0, meses)
                .mapToObj(primeiro::plusMonths)
                .map(mes -> {
                    List<Nota> notas = notasPorMes.getOrDefault(mes, List.of());
                    BigDecimal total = notas.stream().map(Nota::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new GastoMensalResponse(mes.toString(), total, notas.size());
                })
                .toList();
    }
}
