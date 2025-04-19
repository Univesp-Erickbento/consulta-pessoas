package com.mypet.mypet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dtos.FuncionarioDTO;
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
@CrossOrigin(origins = "*", allowedHeaders = "*") // CORS para testes locais
public class FuncionarioController {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(
            @RequestBody FuncionarioDTO funcionarioDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String cpf = funcionarioDTO.getCpf();
            log.info("Recebido CPF: {}", cpf);
            log.info("Recebido Token: {}", authorizationHeader);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Token de autorização não fornecido."));
            }

            // Headers que serão reutilizados
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);
            headers.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
            headers.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
            headers.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());

            if (funcionarioDTO.getDataDeAdmissao() != null) {
                headers.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao().toString());
            }

            if (funcionarioDTO.getDataDeDemissao() != null) {
                headers.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao().toString());
            }

            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);
            log.info("Resposta da rota 'buscarPessoaPorCpf': {}", pessoaJson);

            if (pessoaJson == null || pessoaJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Pessoa não encontrada."));
            }

            Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

            Object perfisObj = pessoaMap.get("perfis");
            String perfis = (perfisObj != null) ? perfisObj.toString() : "";

            if (perfis.contains("FUNCIONARIO")) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Essa pessoa já é um funcionário cadastrado."));
            }

            // Adiciona perfil de funcionário
            pessoaMap.put("perfis", perfis + ",FUNCIONARIO");

            // Pega o ID com verificação de null
            Object idObj = pessoaMap.get("id");
            if (idObj == null) {
                log.error("Campo 'id' não encontrado no JSON da pessoa. JSON recebido: {}", pessoaMap);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", "Campo 'id' não encontrado na resposta da pessoa."));
            }

            headers.put("id", idObj.toString());

            // Atualiza dados da pessoa
            producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

            // Prepara dados para salvar o funcionário
            Map<String, Object> funcionarioMap = new HashMap<>();
            funcionarioMap.put("pessoaId", idObj.toString());
            funcionarioMap.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
            funcionarioMap.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
            funcionarioMap.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());

            if (funcionarioDTO.getDataDeAdmissao() != null) {
                funcionarioMap.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao().toString());
            }

            if (funcionarioDTO.getDataDeDemissao() != null) {
                funcionarioMap.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao().toString());
            }

            producerTemplate.sendBodyAndHeaders("direct:salvarFuncionario", funcionarioMap, headers);

            // ✅ Resposta JSON ao invés de texto simples
            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Funcionário adicionado com sucesso!");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Erro ao adicionar funcionário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro interno ao processar a solicitação."));
        }
    }
}
