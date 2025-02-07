package com.mypet.mypet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.FuncionarioDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;  // Certificando-se de que o ObjectMapper seja injetado

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(@RequestBody FuncionarioDTO funcionarioDTO, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String cpf = funcionarioDTO.getCpf();

            // Log para depuração
            System.out.println("Token recebido no controlador: " + authorizationHeader);
            System.out.println("CPF recebido no controlador: " + cpf);

            // Verificando se o header de Authorization está vazio
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new ResponseEntity<>("Token de autorização não fornecido.", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);

            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Log da resposta da API
            System.out.println("Resposta da API para buscar pessoa: " + pessoaJson);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Verifica se a pessoa já é um funcionário
                if (pessoaMap.get("perfis").toString().contains("FUNCIONARIO")) {
                    return new ResponseEntity<>("Essa pessoa já é um funcionário cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",FUNCIONARIO");

                    headers.put("id", pessoaMap.get("id").toString());
                    headers.put("cpf", cpf);
                    headers.put("Authorization", authorizationHeader);

                    // Atualizando os dados da pessoa
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    Map<String, Object> funcionarioMap = new HashMap<>();
                    funcionarioMap.put("pessoaId", pessoaMap.get("id"));
                    funcionarioMap.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
                    funcionarioMap.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
                    funcionarioMap.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());

                    if (funcionarioDTO.getDataDeAdmissao() != null) {
                        funcionarioMap.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao().toString());
                    }

                    if (funcionarioDTO.getDataDeDemissao() != null) {
                        funcionarioMap.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao().toString());
                    }

                    // Enviando os dados do funcionário
                    producerTemplate.sendBodyAndHeaders("direct:salvarFuncionario", funcionarioMap, headers);

                    return new ResponseEntity<>("Funcionário adicionado com sucesso!", HttpStatus.CREATED);
                }
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Melhorando o tratamento de exceções
            System.err.println("Erro ao adicionar funcionário: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
