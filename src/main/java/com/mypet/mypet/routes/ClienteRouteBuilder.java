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
                .log("Salvando cliente: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String cpf = exchange.getIn().getHeader("cpf", String.class);
                    String clienteReg = exchange.getIn().getHeader("clienteReg", String.class);
                    String clienteStatus = exchange.getIn().getHeader("clienteStatus", String.class);
                    String idHeader = exchange.getIn().getHeader("id", String.class);

                    // Log para verificar os cabeçalhos
                    System.out.println("Token recebido: " + authorizationHeader);
                    System.out.println("CPF recebido: " + cpf);
                    System.out.println("clienteReg recebido: " + clienteReg);
                    System.out.println("clienteStatus recebido: " + clienteStatus);
                    System.out.println("idHeader recebido: " + idHeader);

                    // Validação de cabeçalhos essenciais
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (cpf == null || cpf.isEmpty()) {
                        throw new IllegalArgumentException("CPF não fornecido.");
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

                    // Criando o ClienteDTO
                    ClienteDTO clienteDTO = new ClienteDTO();
                    clienteDTO.setCpf(cpf);

                    try {
                        Long pessoaId = Long.valueOf(idHeader);
                        clienteDTO.setPessoaId(pessoaId);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("O ID fornecido não é válido.");
                    }

                    clienteDTO.setClienteReg(clienteReg);
                    clienteDTO.setClienteStatus(clienteStatus);

                    // Log para verificar o clienteDTO antes de enviar
                    System.out.println("ClienteDTO antes de enviar para o endpoint: " + clienteDTO);

                    // Colocando o clienteDTO no corpo da mensagem para ser enviado
                    exchange.getIn().setBody(clienteDTO);
                })
                .log("Cliente DTO antes de enviar para o endpoint: ${body}")
                .marshal().json() // Transforma o objeto ClienteDTO em JSON
                .to("http://192.168.15.115:9090/api/clientes") // Endpoint do Spring Boot para salvar o cliente
                .log("Cliente salvo com sucesso.");
    }
}
