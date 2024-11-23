package com.mypet.mypet.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InserirPerfilProcessor implements Processor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> pessoaData = exchange.getProperty("pessoaData", Map.class);
        long pessoaId = (long) pessoaData.get("id");
        String perfil = exchange.getIn().getHeader("perfil", String.class);

        String insertPerfil = "INSERT INTO perfis (pessoa_id, perfil) VALUES (?, ?)";
        jdbcTemplate.update(insertPerfil, pessoaId, perfil);

        if ("CLIENTE".equalsIgnoreCase(perfil)) {
            String insertCliente = "INSERT INTO clientes (pessoa_id, clienteReg, clienteStatus) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertCliente, pessoaId, "Registro do Cliente", "ATIVO");
        } else if ("FUNCIONARIO".equalsIgnoreCase(perfil)) {
            String insertFuncionario = "INSERT INTO funcionarios (pessoa_id, dataDeAdmissao) VALUES (?, ?)";
            jdbcTemplate.update(insertFuncionario, pessoaId, "2024-01-01");
        }
    }
}

