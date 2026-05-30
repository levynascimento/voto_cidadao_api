package br.com.urnaeletronica.config;

import br.com.urnaeletronica.entity.Candidato;
import br.com.urnaeletronica.entity.Cargo;
import br.com.urnaeletronica.entity.Cidadao;
import br.com.urnaeletronica.entity.Eleitor;
import br.com.urnaeletronica.repository.CandidatoRepository;
import br.com.urnaeletronica.repository.CargoRepository;
import br.com.urnaeletronica.repository.CidadaoRepository;
import br.com.urnaeletronica.repository.EleitorRepository;
import br.com.urnaeletronica.service.VotacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final VotacaoService votacaoService;
    private final CargoRepository cargoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final CandidatoRepository candidatoRepository;
    private final EleitorRepository eleitorRepository;

    @Bean
    public CommandLineRunner initializeUrnaState() {
        return args ->{
            votacaoService.garantirUrnaInicializada();
            // Dentro do método initializeData()...
            if (cidadaoRepository.count() == 0) {
            // 1. Seed de Cargos
            Cargo pres = cargoRepository.save(Cargo.builder().nome("Presidente").build());
            Cargo gov = cargoRepository.save(Cargo.builder().nome("Governador").build());
            Cargo depFed = cargoRepository.save(Cargo.builder().nome("Deputador Federal").build());
            Cargo depEst = cargoRepository.save(Cargo.builder().nome("Deputador Estadual").build());
            Cargo senador = cargoRepository.save(Cargo.builder().nome("Senador").build());


            // 2. Seed de Cidadãos
            Cidadao c1 = cidadaoRepository.save(Cidadao.builder().cpf("11111111111").nome("Ana Silva").build());
            Cidadao c2 = cidadaoRepository.save(Cidadao.builder().cpf("22222222222").nome("Bruno Costa").build());
            Cidadao c3 = cidadaoRepository.save(Cidadao.builder().cpf("33333333333").nome("Carlos Pereira").build());

            Cidadao c4 = cidadaoRepository.save(Cidadao.builder().cpf("44444444444").nome("Diana Rocha").build());
            Cidadao c5 = cidadaoRepository.save(Cidadao.builder().cpf("55555555555").nome("Eduardo Mendes").build());
            Cidadao c6 = cidadaoRepository.save(Cidadao.builder().cpf("66666666666").nome("Fernanda Lima").build());
            Cidadao c7 = cidadaoRepository.save(Cidadao.builder().cpf("77777777777").nome("Diana Rocha").build());
            Cidadao c8 = cidadaoRepository.save(Cidadao.builder().cpf("88888888888").nome("Eduardo Mendes").build());
            Cidadao c9 = cidadaoRepository.save(Cidadao.builder().cpf("99999999999").nome("Fernanda Lima").build());

            // 3. Seed de Eleitores (Usando os cidadãos 1, 2 e 3)
            Eleitor e1 = new Eleitor();
            e1.setTituloEleitor("10001");
            e1.setJaVotou(false);
            e1.setCidadao(c1);
            eleitorRepository.save(e1);

            Eleitor e2 = new Eleitor();
            e2.setTituloEleitor("10002");
            e2.setJaVotou(false);
            e2.setCidadao(c2);
            eleitorRepository.save(e2);

            Eleitor e3 = new Eleitor();
            e3.setTituloEleitor("10003");
            e3.setJaVotou(false);
            e3.setCidadao(c3);
            eleitorRepository.save(e3);

            // 4. Seed de Candidatos (Usando os cidadãos 4, 5 e 6)
            Candidato cand1 = new Candidato();
            cand1.setNumeroCandidato(10);
            cand1.setNumeroVotos(0);
            cand1.setStatusEleicao(1);
            cand1.setCidadao(c4);
            cand1.setCargo(pres);
            candidatoRepository.save(cand1);

            Candidato cand2 = new Candidato();
            cand2.setNumeroCandidato(20);
            cand2.setNumeroVotos(0);
            cand2.setStatusEleicao(1);
            cand2.setCidadao(c5);
            cand2.setCargo(gov);
            candidatoRepository.save(cand2);

            Candidato cand3 = new Candidato();
            cand3.setNumeroCandidato(30);
            cand3.setNumeroVotos(0);
            cand3.setStatusEleicao(1);
            cand3.setCidadao(c6);
            cand3.setCargo(depFed);
            candidatoRepository.save(cand3);

        }
        };
    }
}
