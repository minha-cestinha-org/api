package minhacestinha.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Conteúdo lido do QR code da nota (URL) ou a chave de acesso digitada")
public record NotaQrCodeRequest(
        @Schema(example = "https://www.nfce.fazenda.sp.gov.br/NFCeConsultaPublica/Paginas/ConsultaQRCode.aspx?p=35260912345678000190650010000123451000123456|2|1|1|ABC")
        @NotBlank(message = "Conteúdo do QR code é obrigatório.")
        @Size(max = 1000, message = "Conteúdo do QR code muito longo.")
        String conteudo
) {
}
