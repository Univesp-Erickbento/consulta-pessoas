package com.mypet.mypet.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ConsultaPerfisProcessor implements Processor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> pessoaData = exchange.getProperty("pessoaData", Map.class);
        long pessoaId = (long) pessoaData.get("id");

        String query = "SELECT perfil FROM perfis WHERE pessoa_id = ?";
        List<Map<String, Object>> perfis = jdbcTemplate.queryForList(query, pessoaId);

        pessoaData.put("perfis", perfis);
        exchange.setProperty("perfis", perfis);
        exchange.getIn().setBody(pessoaData);
    }
}

