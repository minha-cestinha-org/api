package minhacestinha.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MinhaCestinhaApplication {

    public static void main(String[] args) {
        // Mesmo fuso do Hibernate (hibernate.jdbc.time_zone): sem isso, num servidor em UTC
        // a hora gravada no banco fica 3h deslocada da hora da nota.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(MinhaCestinhaApplication.class, args);
    }
}
