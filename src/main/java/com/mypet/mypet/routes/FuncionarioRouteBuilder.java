package com.mypet.mypet.routes;

import com.mypet.mypet.domain.dto.FuncionarioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FuncionarioRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Tratar exceções dentro das rotas
        onException(Exception.class)
                .log("Erro ao processar a rota: ${exception.message}")
                .handled(true)
                .to("log:erro?level=ERROR");

        // Rota para salvar o funcionário
        from("direct:salvarFuncionario")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String cpf = exchange.getIn().getHeader("cpf", String.class);
                    String funcionarioTipo = exchange.getIn().getHeader("funcionarioTipo", String.class);
                    String funcionarioReg = exchange.getIn().getHeader("funcionarioReg", String.class);
                    String funcionarioStatus = exchange.getIn().getHeader("funcionarioStatus", String.class);
                    String dataDeAdmissao = exchange.getIn().getHeader("dataDeAdmissao", String.class);
                    String dataDeDemissao = exchange.getIn().getHeader("dataDeDemissao", String.class);

                    // Verificando se os valores obrigatórios estão presentes
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (cpf == null || cpf.isEmpty()) {
                        throw new IllegalArgumentException("CPF não fornecido.");
                    }

                    // Log dos headers recebidos
                    log.info("Recebendo requisição para salvar funcionário com CPF: {} e Authorization: {}", cpf, authorizationHeader);

                    // Log extra para checar os dados recebidos
                    log.info("Dados recebidos: funcionarioTipo={}, funcionarioReg={}, funcionarioStatus={}, dataDeAdmissao={}, dataDeDemissao={}",
                            funcionarioTipo, funcionarioReg, funcionarioStatus, dataDeAdmissao, dataDeDemissao);

                    // Criando o DTO do funcionário
                    FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                    funcionarioDTO.setCpf(cpf);
                    funcionarioDTO.setFuncionarioTipo(funcionarioTipo);
                    funcionarioDTO.setFuncionarioReg(funcionarioReg);
                    funcionarioDTO.setFuncionarioStatus(funcionarioStatus);

                    // Mantendo as datas como String diretamente
                    if (dataDeAdmissao != null && !dataDeAdmissao.isEmpty()) {
                        // A String da data pode ser mantida ou convertida para LocalDate se precisar em algum momento
                        funcionarioDTO.setDataDeAdmissao(dataDeAdmissao); // Mantendo como String
                    }

                    if (dataDeDemissao != null && !dataDeDemissao.isEmpty()) {
                        funcionarioDTO.setDataDeDemissao(dataDeDemissao); // Mantendo como String
                    }

                    // Tentando pegar o "pessoaId" do header
                    String pessoaIdHeader = exchange.getIn().getHeader("id", String.class);
                    if (pessoaIdHeader != null && !pessoaIdHeader.isEmpty()) {
                        funcionarioDTO.setPessoaId(Long.parseLong(pessoaIdHeader));
                    } else {
                        throw new IllegalArgumentException("Pessoa ID não fornecido.");
                    }

                    // Checando os valores do DTO antes de enviar
                    log.info("Dados do DTO: {}", funcionarioDTO);

                    // Adicionando os dados do funcionário ao corpo da requisição
                    exchange.getIn().setBody(funcionarioDTO);
                })
                .marshal().json()
                .to("http://localhost:9090/api/funcionarios")
                .log("Funcionário salvo com sucesso.");
    }
}
