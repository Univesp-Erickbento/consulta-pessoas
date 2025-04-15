package com.mypet.mypet.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private static final String PESSOA_SERVICE_URL = "http://192.168.15.115:9090/api/pessoas";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Override
    public void configure() throws Exception {
        // Configura o ObjectMapper para suportar Java 8 Date/Time
        objectMapper.registerModule(new JavaTimeModule());
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Object.class);

        // Tratamento de exceções
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    return exception.getStatusCode() == 401;
                })
                .log(LoggingLevel.ERROR, "Erro de autenticação: Token inválido ou expirado. Status: ${exception.statusCode} - ${exception.message}")
                .handled(true);

        onException(JsonProcessingException.class)
                .log(LoggingLevel.ERROR, "Erro ao processar o corpo JSON: ${exception.message}")
                .handled(true);

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar pessoa por CPF
        from("direct:buscarPessoaPorCpf")
                .log(LoggingLevel.INFO, "Cabeçalhos recebidos: ${headers}")
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    log.info("Cabeçalho Authorization: " + authorizationHeader);
                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        log.error("Token de autorização inválido ou não fornecido.");
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    String token = authService.extractToken(exchange);
                    if (token == null || token.trim().isEmpty()) {
                        log.error("Token extraído está vazio ou inválido.");
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    log.info("Token de autorização extraído: " + token);
                    exchange.getIn().setHeader("Authorization", "Bearer " + token);
                })
                .log(LoggingLevel.INFO, "Token de autorização após processamento: ${header.Authorization}")
                .log(LoggingLevel.INFO, "URL da Requisição: " + PESSOA_SERVICE_URL + "/cpf/${header.cpf}")
                .toD(PESSOA_SERVICE_URL + "/cpf/${header.cpf}")
                .log(LoggingLevel.INFO, "Resposta da API para o CPF ${header.cpf}: ${body}")
                .convertBodyTo(String.class);

        // Rota para atualizar pessoa
        from("direct:atualizarPessoa")
                .log(LoggingLevel.INFO, "Cabeçalhos Iniciais: ${headers}")
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        log.error("Token de autorização inválido ou não fornecido.");
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    String token = authService.extractToken(exchange);
                    if (token == null || token.trim().isEmpty()) {
                        log.error("Token extraído está vazio ou inválido.");
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    exchange.getIn().setHeader("Authorization", "Bearer " + token);
                })
                .log(LoggingLevel.INFO, "Token de autorização após processamento: ${header.Authorization}")
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Accept", constant("application/json"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    try {
                        Object body = exchange.getIn().getBody();
                        String jsonBody = objectMapper.writeValueAsString(body);
                        exchange.getIn().setBody(jsonBody, String.class);
                        log.info("Corpo da mensagem convertido para JSON com sucesso.");
                    } catch (JsonProcessingException e) {
                        log.error("Erro ao converter o corpo da mensagem para JSON: " + e.getMessage(), e);
                        exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
                        throw new IllegalArgumentException("Erro ao converter o corpo da mensagem para JSON", e);
                    }
                })
                .log(LoggingLevel.INFO, "URL da Requisição de Atualização: " + PESSOA_SERVICE_URL + "/${header.id}")
                .toD(PESSOA_SERVICE_URL + "/${header.id}")
                .convertBodyTo(String.class);
    }
}
