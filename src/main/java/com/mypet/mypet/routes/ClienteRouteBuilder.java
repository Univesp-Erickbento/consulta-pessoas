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
                .setHeader("Authorization", constant("Bearer eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZXJ2acOnby1kZS1jYWRhc3Ryby1CTVQiLCJzdWIiOiJFcmlja0JlbnRvIiwiZXhwIjoxNzM4NDE1NjYwLCJpYXQiOjE3Mzg0MTUzNjB9.czKdGVdVEtiCRIltDiqTJwUv5qAK_AEe9odN6IEGbzOSFIX042EMTdkOswpVEuddLrEeATa2rTlucT9XXFTUa9YfSO7PX7_Eh9po_Bckean5kamJs8sb5YPVYr6BVPmJSruXtVio-2uw1g1SelaXw1vZIlvFgSEatGGNvE27-9sCKY4h9ZvMtOioGMsi2vCam-LBbW4ShJrY9loIkZAE40IFZslWyvZEfnpXst44bgaC7ZX80JMX8gYA4IjC7iOk9amaxMvmMzhppTHfgHg4FVs62DdTinVHFs9RbZ9iZ5EI4-Wd-ysLNpRuqXMfWo08o9jSwq4DUCuT3jY-LOBT1A")) // Adiciona o token de autorização
                .marshal().json()
                .to("http://localhost:9090/api/clientes");

        // Reutilizando a rota de busca de pessoa por CPF
        from("direct:clienteBuscarPessoaPorCpf")
                .setHeader("Authorization", constant("Bearer SEU_TOKEN_AQUI")) // Adiciona o token de autorização
                .to("direct:buscarPessoaPorCpf");
    }
}