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

            pessoaMap.put("perfis", perfis + ",CLIENTE");

            Object idObj = pessoaMap.get("id");
            if (idObj == null) {
                log.error("Campo 'id' não encontrado no JSON da pessoa. JSON recebido: {}", pessoaMap);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", "Campo 'id' não encontrado na resposta da pessoa."));
            }

            headers.put("id", idObj.toString());

            producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

            Map<String, Object> clienteMap = new HashMap<>();
            clienteMap.put("pessoaId", idObj.toString());
            clienteMap.put("clienteReg", clienteDTO.getClienteReg());
            clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());

            producerTemplate.sendBodyAndHeaders("direct:salvarCliente", clienteMap, headers);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Cliente adicionado com sucesso!"));

        } catch (Exception e) {
            log.error("Erro ao adicionar Cliente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro interno ao processar a solicitação."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id,
                                                @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("id", id);
            headers.put("Authorization", token);

            String clienteJson = producerTemplate.requestBodyAndHeaders("direct:buscarClientePorId", null, headers, String.class);
            log.info("Resposta da rota 'buscarClientePorId': {}", clienteJson);

            if (clienteJson == null || clienteJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Cliente não encontrado."));
            }

            return ResponseEntity.ok(objectMapper.readValue(clienteJson, Map.class));
        } catch (Exception e) {
            log.error("Erro ao buscar Cliente por ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao buscar Cliente."));
        }
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<?> buscarClientePorCpf(@PathVariable String cpf,
                                                 @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", token);

            String clienteJson = producerTemplate.requestBodyAndHeaders("direct:buscarClientePorCpf", null, headers, String.class);
            log.info("Resposta da rota 'buscarClientePorCpf': {}", clienteJson);

            if (clienteJson == null || clienteJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Cliente não encontrado."));
            }

            return ResponseEntity.ok(objectMapper.readValue(clienteJson, Map.class));
        } catch (Exception e) {
            log.error("Erro ao buscar Cliente por CPF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao buscar Cliente."));
        }
    }


    @GetMapping("/pessoa/{pessoaId}")
    public ResponseEntity<?> buscarClientePorPessoaId(@PathVariable Long pessoaId,
                                                      @RequestHeader("Authorization") String token) {
        try {
            // Definindo o header com o pessoaId e o token de autorização
            Map<String, Object> headers = new HashMap<>();
            headers.put("pessoaId", pessoaId);
            headers.put("Authorization", token);

            // Chamada para buscar o cliente pelo pessoaId
            String clienteJson = producerTemplate.requestBodyAndHeaders("direct:buscarClientePorPessoaId", null, headers, String.class);
            log.info("Resposta da rota 'buscarClientePorPessoaId': {}", clienteJson);

            // Verificando se o cliente foi encontrado
            if (clienteJson == null || clienteJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Cliente não encontrado."));
            }

            // Retornando o cliente encontrado
            return ResponseEntity.ok(objectMapper.readValue(clienteJson, Map.class));
        } catch (Exception e) {
            log.error("Erro ao buscar Cliente por pessoaId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao buscar Cliente."));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id,
                                              @RequestBody ClienteDTO clienteDTO,
                                              @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("id", id);
            headers.put("Authorization", token);
            headers.put("clienteReg", clienteDTO.getClienteReg());
            headers.put("clienteStatus", clienteDTO.getClienteStatus());

            Map<String, Object> clienteMap = new HashMap<>();
            clienteMap.put("clienteReg", clienteDTO.getClienteReg());
            clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());

            producerTemplate.sendBodyAndHeaders("direct:atualizarCliente", clienteMap, headers);

            return ResponseEntity.ok(Map.of("mensagem", "Cliente atualizado com sucesso!"));
        } catch (Exception e) {
            log.error("Erro ao atualizar Cliente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao atualizar Cliente."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id,
                                            @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("id", id);
            headers.put("Authorization", token);

            producerTemplate.sendBodyAndHeaders("direct:deletarCliente", null, headers);

            return ResponseEntity.ok(Map.of("mensagem", "Cliente deletado com sucesso!"));
        } catch (Exception e) {
            log.error("Erro ao deletar Cliente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao deletar Cliente."));
        }
    }
}
