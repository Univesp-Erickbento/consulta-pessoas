package com.mypet.mypet.routes;

import com.mypet.mypet.domain.dtos.clientedto.ClienteEnvioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClienteRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .log("Erro ao processar a rota: ${exception.message}")
                .handled(true);

        from("direct:salvarCliente")
                .log("Salvando cliente: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String clienteReg = exchange.getIn().getHeader("clienteReg", String.class);
                    String clienteStatus = exchange.getIn().getHeader("clienteStatus", String.class);
                    if (clienteStatus != null) {
                        clienteStatus = clienteStatus.toUpperCase(); // <- Aqui força o valor para "ATIVO"
                    }

                    String idHeader = exchange.getIn().getHeader("id", String.class);

                    // Validações
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (clienteReg == null || clienteReg.isEmpty()) {
                        throw new IllegalArgumentException("clienteReg não fornecido.");
                    }
                    if (clienteStatus == null || clienteStatus.isEmpty()) {
                        throw new IllegalArgumentException("clienteStatus não fornecido.");
                    }
                    if (idHeader == null || idHeader.isEmpty()) {
                        throw new IllegalArgumentException("ID não fornecido.");
                    }

                    long pessoaId = Long.parseLong(idHeader);

                    // Criar ClienteEnvioDTO (sem CPF)
                    ClienteEnvioDTO clienteEnvioDTO = new ClienteEnvioDTO(pessoaId, clienteReg, clienteStatus);

                    exchange.getIn().setBody(clienteEnvioDTO);

                    // Log para depuração
                    System.out.println("ClienteEnvioDTO a ser enviado: " + clienteEnvioDTO);
                })
                .log("Payload enviado: ${body}")
                .marshal().json()
                .to("http://192.168.15.115:9090/api/clientes")
                .log("Cliente salvo com sucesso.");
    }
}
