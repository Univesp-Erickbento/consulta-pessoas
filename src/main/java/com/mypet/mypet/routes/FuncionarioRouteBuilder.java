package com.mypet.mypet.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(createObjectMapper(), Object.class);

        // Rota para salvar funcionário
        from("direct:salvarFuncionario")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal(jacksonDataFormat)
                .to("http://localhost:9090/api/funcionarios");

        // Rota para atualizar funcionário
        from("direct:atualizarFuncionario")
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal(jacksonDataFormat)
                .to("http://localhost:9090/api/funcionarios/${header.id}");

        // Rota para deletar funcionário
        from("direct:deletarFuncionario")
                .setHeader("CamelHttpMethod", constant("DELETE"))
                .to("http://localhost:9090/api/funcionarios/${header.id}");

        // Reutilizando a rota de busca de pessoa por CPF
        from("direct:funcionarioBuscarPessoaPorCpf")
                .to("direct:buscarPessoaPorCpf");
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
}
