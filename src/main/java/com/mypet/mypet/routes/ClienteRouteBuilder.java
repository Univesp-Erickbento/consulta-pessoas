package com.mypet.mypet.routes;

import com.mypet.mypet.domain.dto.ClienteDTO;
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
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String cpf = exchange.getIn().getHeader("cpf", String.class);
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (cpf == null || cpf.isEmpty()) {
                        throw new IllegalArgumentException("CPF não fornecido.");
                    }
                    exchange.getIn().setHeader("Authorization", authorizationHeader);
                    exchange.getIn().setHeader("cpf", cpf);

                    System.out.println("Token na rota salvarCliente: " + authorizationHeader);
                    System.out.println("CPF na rota salvarCliente: " + cpf);

                    ClienteDTO clienteDTO = new ClienteDTO();
                    clienteDTO.setCpf(cpf);
                    clienteDTO.setPessoaId((Integer) exchange.getIn().getHeader("id"));
                    clienteDTO.setClienteReg(exchange.getIn().getHeader("clienteReg", String.class));
                    clienteDTO.setClienteStatus(exchange.getIn().getHeader("clienteStatus", String.class));

                    exchange.getIn().setBody(clienteDTO);
                })
                .marshal().json()
                .to("http://localhost:9090/api/clientes")
                .log("Cliente salvo com sucesso.");

        from("direct:clienteBuscarPessoaPorCpf")
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String cpf = exchange.getIn().getHeader("cpf", String.class);
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (cpf == null || cpf.isEmpty()) {
                        throw new IllegalArgumentException("CPF não fornecido.");
                    }
                    exchange.getIn().setHeader("Authorization", authorizationHeader);
                    exchange.getIn().setHeader("cpf", cpf);

                    System.out.println("Token na rota clienteBuscarPessoaPorCpf: " + authorizationHeader);
                    System.out.println("CPF na rota clienteBuscarPessoaPorCpf: " + cpf);
                })
                .to("direct:buscarPessoaPorCpf")
                .log("Pessoa buscada com sucesso.");
    }
}
