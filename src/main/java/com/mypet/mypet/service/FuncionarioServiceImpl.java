package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.FuncionarioDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class FuncionarioServiceImpl {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<?> adicionarFuncionario(FuncionarioDTO funcionarioDTO, String authorizationHeader) {
        try {
            String cpf = funcionarioDTO.getCpf();
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);

            // Log para depuração
            System.out.println("Token recebido no serviço: " + authorizationHeader);
            System.out.println("CPF recebido no serviço: " + cpf);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new ResponseEntity<>("Token de autorização não fornecido.", HttpStatus.BAD_REQUEST);
            }

            // Enviar requisição para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Log da resposta da API
            System.out.println("Resposta da API para buscar pessoa no serviço: " + pessoaJson);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                if (pessoaMap.get("perfis").toString().contains("FUNCIONARIO")) {
                    return new ResponseEntity<>("Essa pessoa já é um funcionário cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",FUNCIONARIO");

                    headers.put("id", pessoaMap.get("id").toString());
                    headers.put("cpf", cpf);
                    headers.put("Authorization", authorizationHeader);

                    // Atualizando os dados da pessoa
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    // Criando e salvando o funcionário
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

                    // Enviando os dados do funcionário para salvar
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
