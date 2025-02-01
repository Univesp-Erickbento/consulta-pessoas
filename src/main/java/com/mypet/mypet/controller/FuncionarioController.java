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

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(
            @RequestBody FuncionarioDTO funcionarioDTO,
            @RequestHeader("Authorization") String authorizationHeader) {  // Captura o cabeçalho Authorization
        try {
            // Passo 1: Obter o CPF do FuncionarioDTO
            String cpf = funcionarioDTO.getCpf();

            // Passo 2: Criar um Map de cabeçalhos para enviar para a rota Camel
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);  // Adicionando o cabeçalho Authorization

            // Passo 3: Chamar a rota Camel para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Verificação se a resposta não está vazia
            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                // Passo 4: Converter o JSON da resposta para um Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Passo 5: Verificar se a pessoa já possui o perfil de funcionário
                if (pessoaMap.get("perfis").toString().contains("FUNCIONARIO")) {
                    return new ResponseEntity<>("Essa pessoa já é um funcionário cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    // Passo 6: Adicionar o perfil de funcionário à pessoa existente
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",FUNCIONARIO");

                    // Adicionar o ID da pessoa aos cabeçalhos para atualização
                    headers.put("id", pessoaMap.get("id"));

                    // Enviar a pessoa atualizada para a rota de atualização
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    // Passo 7: Criar e preparar o objeto funcionário para salvar, sem enviar o CPF
                    Map<String, Object> funcionarioMap = new HashMap<>();
                    funcionarioMap.put("pessoaId", pessoaMap.get("id"));
                    funcionarioMap.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
                    funcionarioMap.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
                    funcionarioMap.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());
                    funcionarioMap.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao());
                    funcionarioMap.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao());

                    // Enviar o objeto funcionário para a rota de salvar funcionário
                    producerTemplate.sendBody("direct:salvarFuncionario", funcionarioMap);

                    return new ResponseEntity<>("Funcionário adicionado com sucesso!", HttpStatus.CREATED);
                }
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Log de erro detalhado
            System.err.println("Erro ao adicionar funcionário: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
