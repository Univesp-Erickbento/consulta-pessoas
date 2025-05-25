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

import java.util.*;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔍 Buscar funcionário por pessoaId
    @GetMapping("/pessoa/{pessoaId}")

    public ResponseEntity<?> buscarFuncionarioPorPessoaId(
            @PathVariable("pessoaId") Long pessoaId,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            log.info("🔎 Buscando funcionário com pessoaId: {}", pessoaId);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Token de autorização não fornecido."));
            }

            Map<String, Object> headers = new HashMap<>();
            headers.put("Authorization", authorizationHeader);
            headers.put("pessoaId", pessoaId);

            List<?> funcionarios = producerTemplate.requestBodyAndHeaders(
                    "direct:buscarFuncionarioPorPessoaId", null, headers, List.class);

            if (funcionarios == null || funcionarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensagem", "Funcionário não encontrado."));
            }

            // Assume-se que só há um funcionário por pessoaId. Pega o primeiro da lista.
            return ResponseEntity.ok(funcionarios.get(0));

        } catch (Exception e) {
            log.error("❌ Erro ao buscar funcionário por pessoaId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao buscar funcionário."));
        }
    }

    // ➕ Adicionar funcionário
    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(
            @RequestBody FuncionarioDTO funcionarioDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String cpf = funcionarioDTO.getCpf();
            log.info("📥 Recebido CPF: {}", cpf);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Token de autorização não fornecido."));
            }

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

            // Busca pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);
            log.info("📨 Resposta da rota buscarPessoaPorCpf: {}", pessoaJson);

            if (pessoaJson == null || pessoaJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Pessoa não encontrada."));
            }

            Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

            // Verifica perfil
            Object perfisObj = pessoaMap.get("perfis");
            String perfis = (perfisObj != null) ? perfisObj.toString() : "";

            if (perfis.contains("FUNCIONARIO")) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Essa pessoa já é um funcionário cadastrado."));
            }

            // Atualiza perfis
            pessoaMap.put("perfis", perfis + ",FUNCIONARIO");

            Object idObj = pessoaMap.get("id");
            if (idObj == null) {
                log.error("⚠️ Campo 'id' não encontrado no JSON da pessoa. JSON recebido: {}", pessoaMap);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", "Campo 'id' não encontrado na resposta da pessoa."));
            }

            headers.put("id", idObj.toString());

            // Atualiza dados da pessoa
            producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

            // Salva funcionário
            producerTemplate.sendBodyAndHeaders("direct:salvarFuncionario", null, headers);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Funcionário adicionado com sucesso!"));

        } catch (Exception e) {
            log.error("❌ Erro ao adicionar funcionário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro interno ao processar a solicitação."));
        }
    }
}
