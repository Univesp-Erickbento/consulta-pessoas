package com.mypet.mypet.routes;

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

    private static final String PESSOA_SERVICE_URL = "http://localhost:9090/api/pessoas";

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
                    return exception.getStatusCode() == 401; // Trata especificamente erros 401
                })
                .log(LoggingLevel.ERROR, "Erro de autenticação: Token inválido ou expirado")
                .handled(true);

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar pessoa por CPF
        from("direct:buscarPessoaPorCpf")
                .log(LoggingLevel.INFO, "Cabeçalhos Iniciais: ${headers}")
                .setHeader("Authorization", simple("Bearer ${bean:authService.getToken}"))
                .log(LoggingLevel.INFO, "Token de autorização: ${header.Authorization}")
                .choice()
                .when(header("Authorization").isNull())
                .log(LoggingLevel.ERROR, "Token de autorização não fornecido")
                .throwException(new IllegalArgumentException("Token de autorização não fornecido"))
                .otherwise()
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Accept", constant("application/json"))
                .log(LoggingLevel.INFO, "Token de autorização configurado corretamente.")
                .toD(PESSOA_SERVICE_URL + "/cpf/${header.cpf}")
                .convertBodyTo(String.class);
    }
}
