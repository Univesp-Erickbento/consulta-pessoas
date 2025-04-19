package com.mypet.mypet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dtos.clientedto.ClienteDTO;
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
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*", allowedHeaders = "*") // CORS para testes locais
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarCliente(@RequestBody ClienteDTO clienteDTO,
                                              @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String cpf = clienteDTO.getCpf();
            log.info("Recebido CPF: {}", cpf);
            log.info("Recebido Token: {}", authorizationHeader);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Token de autorização não fornecido."));
            }

            // Headers que serão reutilizados
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);
            headers.put("clienteReg", clienteDTO.getClienteReg());
            headers.put("clienteStatus", clienteDTO.getClienteStatus());

            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);
            log.info("Resposta da rota 'buscarPessoaPorCpf': {}", pessoaJson);

            if (pessoaJson == null || pessoaJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Pessoa não encontrada."));
            }

            Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

            Object perfisObj = pessoaMap.get("perfis");
            String perfis = (perfisObj != null) ? perfisObj.toString() : "";

            if (perfis.contains("CLIENTE")) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Essa pessoa já é um Cliente cadastrado."));
            }

            // Adiciona perfil de cliente
            pessoaMap.put("perfis", perfis + ",CLIENTE");

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

            // Prepara dados para salvar o cliente
            Map<String, Object> clienteMap = new HashMap<>();
            clienteMap.put("pessoaId", idObj.toString());
            clienteMap.put("clienteReg", clienteDTO.getClienteReg());
            clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());

            producerTemplate.sendBodyAndHeaders("direct:salvarCliente", clienteMap, headers);

            // ✅ Resposta JSON
            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Cliente adicionado com sucesso!");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Erro ao adicionar Cliente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro interno ao processar a solicitação."));
        }
    }
}
