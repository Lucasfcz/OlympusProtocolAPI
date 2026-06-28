package io.github.lucasfcz.olympusprotocol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OlympusProtocolApplication {

    public static void main(String[] args) {
        SpringApplication.run(OlympusProtocolApplication.class, args);
    }

}
