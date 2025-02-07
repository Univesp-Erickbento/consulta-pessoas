package com.mypet.mypet.routes;

import com.mypet.mypet.domain.dto.FuncionarioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

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

                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (cpf == null || cpf.isEmpty()) {
                        throw new IllegalArgumentException("CPF não fornecido.");
                    }

                    // Setando os headers
                    exchange.getIn().setHeader("Authorization", authorizationHeader);
                    exchange.getIn().setHeader("cpf", cpf);

                    FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                    funcionarioDTO.setCpf(cpf);
                    funcionarioDTO.setPessoaId(Long.parseLong(exchange.getIn().getHeader("id", String.class)));
                    funcionarioDTO.setFuncionarioTipo(exchange.getIn().getHeader("funcionarioTipo", String.class));
                    funcionarioDTO.setFuncionarioReg(exchange.getIn().getHeader("funcionarioReg", String.class));
                    funcionarioDTO.setFuncionarioStatus(exchange.getIn().getHeader("funcionarioStatus", String.class));

                    // Adicionando os dados do funcionário ao corpo da requisição
                    exchange.getIn().setBody(funcionarioDTO);
                })
                .marshal().json()
                .to("http://localhost:9090/api/funcionarios")
                .log("Funcionário salvo com sucesso.");
    }
}
