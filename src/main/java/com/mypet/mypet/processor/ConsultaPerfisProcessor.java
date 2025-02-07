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
        // Extrair o CPF e o token de autorização dos cabeçalhos
        String cpf = exchange.getIn().getHeader("cpf", String.class);
        String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);

        // Adicionar logs para depuração
        System.out.println("CPF no Processor: " + cpf);
        System.out.println("Token no Processor: " + authorizationHeader);

        // Verificação de cabeçalhos
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não fornecido.");
        }
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Token de autorização não fornecido.");
        }

        // Obter dados da pessoa da propriedade de troca
        Map<String, Object> pessoaData = exchange.getProperty("pessoaData", Map.class);
        long pessoaId = (long) pessoaData.get("id");

        // Consultar os perfis no banco de dados
        String query = "SELECT perfil FROM perfis WHERE pessoa_id = ?";
        List<Map<String, Object>> perfis = jdbcTemplate.queryForList(query, pessoaId);

        // Atualizar os dados da pessoa com os perfis
        pessoaData.put("perfis", perfis);
        exchange.setProperty("perfis", perfis);
        exchange.getIn().setBody(pessoaData);
    }
}
