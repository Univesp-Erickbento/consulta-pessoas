package com.mypet.mypet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.FuncionarioDTO;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(
            @RequestBody FuncionarioDTO funcionarioDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String cpf = funcionarioDTO.getCpf();

            // Verificação do CPF
            if (cpf == null || cpf.trim().isEmpty()) {
                return new ResponseEntity<>("CPF não informado.", HttpStatus.BAD_REQUEST);
            }

            // Preparar cabeçalhos para a rota Camel
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);

            // Chamar a rota Camel para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Verificar se a pessoa já é um funcionário
                if (isFuncionario(pessoaMap)) {
                    return new ResponseEntity<>("Essa pessoa já é um funcionário cadastrado.", HttpStatus.BAD_REQUEST);
                }

                // Adicionar o perfil de funcionário
                pessoaMap.put("perfis", pessoaMap.get("perfis") + ",FUNCIONARIO");
                headers.put("id", pessoaMap.get("id"));

                // Atualizar a pessoa
                producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                // Preparar e salvar o funcionário
                Map<String, Object> funcionarioMap = prepareFuncionarioMap(funcionarioDTO, pessoaMap);
                producerTemplate.sendBody("direct:salvarFuncionario", funcionarioMap);

                return new ResponseEntity<>("Funcionário adicionado com sucesso!", HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Erro ao adicionar funcionário: ", e);
            return new ResponseEntity<>("Ocorreu um erro interno ao tentar adicionar o funcionário. Por favor, tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Método para verificar se a pessoa é um funcionário
    private boolean isFuncionario(Map<String, Object> pessoaMap) {
        return pessoaMap.get("perfis").toString().contains("FUNCIONARIO");
    }

    // Método para preparar o objeto do funcionário para salvar
    private Map<String, Object> prepareFuncionarioMap(FuncionarioDTO funcionarioDTO, Map<String, Object> pessoaMap) {
        Map<String, Object> funcionarioMap = new HashMap<>();
        funcionarioMap.put("pessoaId", pessoaMap.get("id"));
        funcionarioMap.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
        funcionarioMap.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
        funcionarioMap.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());
        funcionarioMap.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao());
        funcionarioMap.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao());
        return funcionarioMap;
    }
}
