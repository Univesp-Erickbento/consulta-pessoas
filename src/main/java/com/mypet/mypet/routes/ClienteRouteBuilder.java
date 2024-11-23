package com.mypet.mypet.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClienteRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Rota para salvar cliente
        from("direct:salvarCliente")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json()
                .to("http://localhost:9090/api/clientes");

        // Reutilizando a rota de busca de pessoa por CPF
        from("direct:clienteBuscarPessoaPorCpf")
                .to("direct:buscarPessoaPorCpf");
    }
}
