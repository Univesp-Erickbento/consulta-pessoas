package com.mypet.mypet.routes;

import com.mypet.mypet.domain.dtos.clientedto.ClienteEnvioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClienteRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Tratamento genérico de exceções
        onException(Exception.class)
                .log("Erro ao processar a rota: ${exception.message}")
                .handled(true);

        // ----------------- SALVAR CLIENTE (POST) -----------------
        from("direct:salvarCliente")
                .log("Salvando cliente: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String clienteReg = exchange.getIn().getHeader("clienteReg", String.class);
                    String clienteStatus = exchange.getIn().getHeader("clienteStatus", String.class);
                    String idHeader = exchange.getIn().getHeader("id", String.class);

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

                    clienteStatus = clienteStatus.toUpperCase(); // Força "ATIVO", por exemplo
                    long pessoaId = Long.parseLong(idHeader);

                    ClienteEnvioDTO clienteEnvioDTO = new ClienteEnvioDTO(pessoaId, clienteReg, clienteStatus);
                    exchange.getIn().setBody(clienteEnvioDTO);
                })
                .log("Payload enviado: ${body}")
                .marshal().json()
                .to("http://192.168.15.115:9090/api/clientes")
                .log("Cliente salvo com sucesso.");

        // ----------------- BUSCAR CLIENTE POR ID (GET) -----------------
        from("direct:buscarClientePorId")
                .log("Buscando cliente por ID: ${header.id}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("http://192.168.15.115:9090/api/clientes/${header.id}?bridgeEndpoint=true")
                .log("Resposta buscarClientePorId: ${body}");

        // ----------------- BUSCAR CLIENTE POR CPF (GET) -----------------
        from("direct:buscarClientePorCpf")
                .log("Buscando cliente por CPF: ${header.cpf}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("http://192.168.15.115:9090/api/clientes/cpf/${header.cpf}?bridgeEndpoint=true")
                .log("Resposta buscarClientePorCpf: ${body}");

        // ----------------- ATUALIZAR CLIENTE (PUT) -----------------
        from("direct:atualizarCliente")
                .log("Atualizando cliente ID: ${header.id}")
                .marshal().json()
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .toD("http://192.168.15.115:9090/api/clientes/${header.id}?bridgeEndpoint=true")
                .log("Cliente atualizado com sucesso.");

        // ----------------- DELETAR CLIENTE (DELETE) -----------------
        from("direct:deletarCliente")
                .log("Deletando cliente ID: ${header.id}")
                .setHeader("CamelHttpMethod", constant("DELETE"))
                .toD("http://192.168.15.115:9090/api/clientes/${header.id}?bridgeEndpoint=true")
                .log("Cliente deletado com sucesso.");

        // ----------------- BUSCAR CLIENTE POR PESSOAID (GET) -----------------
        from("direct:buscarClientePorPessoaId")
                .log("Buscando cliente por pessoaId: ${header.pessoaId}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("http://192.168.15.115:9090/api/clientes/pessoa/${header.pessoaId}?bridgeEndpoint=true")
                .log("Resposta buscarClientePorPessoaId: ${body}");
    }
}
