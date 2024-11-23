package com.mypet.mypet.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PessoaRouteBuilder extends RouteBuilder {

    private static final String PESSOA_SERVICE_URL = "http://localhost:9090/api/pessoas";

    @Override
    public void configure() throws Exception {
        // Rota para buscar pessoa por CPF
        from("direct:buscarPessoaPorCpf")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Accept", constant("application/json"))
                .toD(PESSOA_SERVICE_URL + "/cpf/${header.cpf}")
                .convertBodyTo(String.class);

        // Rota para atualizar pessoa
        from("direct:atualizarPessoa")
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json()
                .toD(PESSOA_SERVICE_URL + "/${header.id}");

        // Rota para adicionar pessoa
        from("direct:adicionarPessoa")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json()
                .to(PESSOA_SERVICE_URL);
    }
}
