package com.mypet.mypet.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class ConsultaPessoaProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Aqui você pode adicionar lógica de processamento diferente, sem usar JdbcTemplate
        // Por exemplo, se o processamento for baseado em dados que não dependem do banco
        // Você pode pegar o CPF da requisição e realizar outras ações, como integração com API externa

        String cpf = exchange.getIn().getHeader("cpf", String.class);
        // Lógica de processamento alternativa sem JDBC
        Map<String, Object> pessoaData = new HashMap<>();
        pessoaData.put("cpf", cpf); // Exemplo simples, você pode definir a estrutura de dados

        exchange.setProperty("pessoaData", pessoaData);
        exchange.getIn().setBody(pessoaData);
    }
}
