package minhacestinha.api.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mensagens {

    private final MessageSource messageSource;

    public String get(String codigo, Object... args) {
        try {
            return messageSource.getMessage(codigo, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException exception) {
            return codigo;
        }
    }
}
