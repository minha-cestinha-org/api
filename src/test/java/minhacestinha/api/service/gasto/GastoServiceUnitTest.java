package minhacestinha.api.service.gasto;

import minhacestinha.api.dto.response.GastoMensalResponse;
import minhacestinha.api.persistence.entity.Nota;
import minhacestinha.api.persistence.entity.User;
import minhacestinha.api.persistence.repository.NotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GastoServiceUnitTest {

    @Mock
    private NotaRepository notaRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-29T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void somaPorMesEPreencheMesesSemCompra() {
        User usuario = User.builder().id(1L).build();
        when(notaRepository.findByUsuarioIdAndAtivoTrueAndDataEmissaoGreaterThanEqual(1L, LocalDateTime.of(2026, 7, 1, 0, 0)))
                .thenReturn(List.of(
                        nota("100.00", LocalDateTime.of(2026, 7, 10, 9, 0)),
                        nota("214.87", LocalDateTime.of(2026, 9, 26, 10, 0)),
                        nota("50.13", LocalDateTime.of(2026, 9, 12, 18, 0))));

        List<GastoMensalResponse> gastos = new GastoServiceImpl(notaRepository, clock).porMes(usuario, 3);

        assertThat(gastos).extracting(GastoMensalResponse::mes).containsExactly("2026-07", "2026-08", "2026-09");
        assertThat(gastos.get(0).total()).isEqualByComparingTo("100.00");
        assertThat(gastos.get(1).total()).isEqualByComparingTo("0");
        assertThat(gastos.get(1).compras()).isZero();
        assertThat(gastos.get(2).total()).isEqualByComparingTo("265.00");
        assertThat(gastos.get(2).compras()).isEqualTo(2);
    }

    private static Nota nota(String valorPago, LocalDateTime data) {
        return Nota.builder().valorPago(new BigDecimal(valorPago)).dataEmissao(data).build();
    }
}
