package br.com.urnaeletronica.config;

import br.com.urnaeletronica.service.VotacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final VotacaoService votacaoService;

    @Bean
    public CommandLineRunner initializeUrnaState() {
        return args -> votacaoService.garantirUrnaInicializada();
    }
}
