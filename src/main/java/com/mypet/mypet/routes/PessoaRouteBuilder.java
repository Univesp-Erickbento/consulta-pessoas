package com.mypet.mypet.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PessoaRouteBuilder extends RouteBuilder {

    private static final String PESSOA_SERVICE_URL = "http://localhost:9090/api/pessoas";

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure() throws Exception {
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Object.class);

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);



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
                .log(LoggingLevel.INFO, "Adicionando pessoa: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal(jacksonDataFormat)
                .to(PESSOA_SERVICE_URL);
    }
}
