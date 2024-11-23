package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.FuncionarioDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class FuncionarioServiceImpl {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<?> adicionarFuncionario(FuncionarioDTO funcionarioDTO) {
        try {
            String cpf = funcionarioDTO.getCpf();
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);

            // Chama a rota Camel para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                // Converte JSON para Map usando o ObjectMapper configurado
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                if (pessoaMap.get("perfis").toString().contains("FUNCIONARIO")) {
                    return new ResponseEntity<>("Essa pessoa já é um funcionário cadastrado.", HttpStatus.BAD_REQUEST);
                } else if (pessoaMap.get("perfis").toString().contains("CLIENTE")) {
                    // Adiciona perfil de funcionário à pessoa existente
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",FUNCIONARIO");
                    headers.put("id", pessoaMap.get("id"));
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    // Cria e salva o funcionário
                    Map<String, Object> funcionarioMap = new HashMap<>();
                    funcionarioMap.put("pessoaId", pessoaMap.get("id"));
                    funcionarioMap.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
                    funcionarioMap.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
                    funcionarioMap.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());
                    funcionarioMap.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao());
                    funcionarioMap.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao());

                    producerTemplate.sendBody("direct:salvarFuncionario", funcionarioMap);

                    return new ResponseEntity<>("Funcionário adicionado com sucesso!", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Erro ao adicionar funcionário: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
