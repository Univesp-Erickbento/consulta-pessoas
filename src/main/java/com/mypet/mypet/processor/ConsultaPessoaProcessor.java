package com.mypet.mypet.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConsultaPessoaProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Lógica para garantir a presença do token de autorização
        String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);

        // Verifica se o cabeçalho Authorization existe
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            // Log para depuração
            System.out.println("Token de autorização não encontrado ou inválido.");

            // Definindo o código de resposta HTTP como 400 (Bad Request) para indicar falha na autenticação
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);

            // Lançando exceção com mensagem personalizada
            throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
        }

        // Log para depuração
        System.out.println("Token de autorização válido: " + authorizationHeader);

        // Garantindo que o cabeçalho Authorization seja passado para a próxima etapa (caso seja necessário)
        exchange.getIn().setHeader("Authorization", authorizationHeader);

        // Recuperando o CPF do cabeçalho da requisição
        String cpf = exchange.getIn().getHeader("cpf", String.class);

        if (cpf == null || cpf.isEmpty()) {
            // Log para depuração
            System.out.println("CPF não encontrado na requisição.");

            // Definindo o código de resposta HTTP como 400 (Bad Request) para indicar falha na requisição
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);

            // Lançando exceção com mensagem personalizada
            throw new IllegalArgumentException("CPF não fornecido.");
        }

        // Criando um mapa para armazenar os dados da pessoa
        Map<String, Object> pessoaData = new HashMap<>();
        pessoaData.put("cpf", cpf);

        // Definindo a propriedade "pessoaData" no exchange para ser usada em etapas posteriores
        exchange.setProperty("pessoaData", pessoaData);

        // Configurando o corpo da mensagem com os dados da pessoa
        exchange.getIn().setBody(pessoaData);

        // Log para depuração (pode ser removido em produção)
        System.out.println("Dados de pessoa: " + pessoaData);
    }
}
