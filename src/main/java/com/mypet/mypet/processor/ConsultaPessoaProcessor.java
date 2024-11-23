package com.mypet.mypet.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsultaPessoaProcessor implements Processor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        String cpf = exchange.getIn().getHeader("cpf", String.class);
        String query = "SELECT * FROM pessoas WHERE cpf = ?";

        Map<String, Object> pessoaData = jdbcTemplate.queryForMap(query, cpf);
        exchange.setProperty("pessoaData", pessoaData);
        exchange.getIn().setBody(pessoaData);
    }
}
