package com.mypet.mypet.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mypet.mypet.config.PessoaProperties;
import com.mypet.mypet.service.AuthService;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class PessoaRouteBuilder extends RouteBuilder {

    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final PessoaProperties pessoaProperties;

    @Autowired
    public PessoaRouteBuilder(ObjectMapper objectMapper, AuthService authService, PessoaProperties pessoaProperties) {
        this.objectMapper = objectMapper;
        this.authService = authService;
        this.pessoaProperties = pessoaProperties;
    }

    @Override
    public void configure() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Object.class);

        // Exceções
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    return exception.getStatusCode() == 401;
                })
                .log(LoggingLevel.ERROR, "Erro de autenticação: ${exception.statusCode} - ${exception.message}")
                .handled(true);

        onException(JsonProcessingException.class)
                .log(LoggingLevel.ERROR, "Erro ao processar JSON: ${exception.message}")
                .handled(true);

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro geral: ${exception.message}")
                .handled(true);

        from("direct:buscarPessoaPorCpf")
                .log(LoggingLevel.INFO, "Cabeçalhos recebidos: ${headers}")
                .process(exchange -> {
                    String token = authService.extractToken(exchange);
                    if (token == null || token.trim().isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    exchange.getIn().setHeader("Authorization", "Bearer " + token);
                })
                .log(LoggingLevel.INFO, "URL da Requisição: " + pessoaProperties.getApiBaseUrl() + "/cpf/${header.cpf}")
                .toD(pessoaProperties.getApiBaseUrl() + "/cpf/${header.cpf}")
                .convertBodyTo(String.class);

        from("direct:atualizarPessoa")
                .process(exchange -> {
                    String token = authService.extractToken(exchange);
                    if (token == null || token.trim().isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    exchange.getIn().setHeader("Authorization", "Bearer " + token);
                })
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Accept", constant("application/json"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    Object body = exchange.getIn().getBody();
                    String jsonBody = objectMapper.writeValueAsString(body);
                    exchange.getIn().setBody(jsonBody, String.class);
                })
                .log(LoggingLevel.INFO, "URL da Requisição de Atualização: " + pessoaProperties.getApiBaseUrl() + "/${header.id}")
                .toD(pessoaProperties.getApiBaseUrl() + "/${header.id}")
                .convertBodyTo(String.class);
    }
}
