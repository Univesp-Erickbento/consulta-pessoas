package com.mypet.mypet.processor;


import com.mypet.mypet.domain.dtos.clientedto.ClienteDTO;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class AdicionarClienteProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Obtém o token de autorização do header
        String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);

        // Verifica se o token está presente
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
        }

        // Preenche o ClienteDTO com os dados do CPF
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setCpf(exchange.getIn().getHeader("cpf", String.class));
        clienteDTO.setPessoaId(12345);  // Exemplo de atribuição
        clienteDTO.setClienteReg("REG123");
        clienteDTO.setClienteStatus("Ativo");

        // Define o corpo da mensagem com os dados do cliente
        exchange.getIn().setBody(clienteDTO);

        // Adiciona o token no cabeçalho da requisição HTTP
        exchange.getIn().setHeader("Authorization", authorizationHeader);

        // Log para depuração
        System.out.println("Enviando ClienteDTO com os dados: " + clienteDTO);
    }
}

