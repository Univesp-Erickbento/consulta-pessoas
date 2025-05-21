package com.mypet.mypet.controller;

import com.mypet.mypet.domain.dtos.clientedto.ClienteDTO;
import com.mypet.mypet.domain.dtos.clientedto.ClienteEnvioDTO;
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
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarCliente(@RequestBody ClienteDTO clienteDTO,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String cpf = clienteDTO.getCpf();  // Agora está correto
            log.info("Recebido CPF: {}", cpf);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Token de autorização não fornecido."));
            }

            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);

            // 🔍 Buscar pessoa pelo CPF
            Map<String, Object> pessoaMap = producerTemplate.requestBodyAndHeaders(
                    "direct:buscarClientePorCpf", null, headers, Map.class);
            log.info("Pessoa encontrada: {}", pessoaMap);

            if (pessoaMap == null || pessoaMap.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Pessoa não encontrada."));
            }

            Object perfisObj = pessoaMap.get("perfis");
            String perfis = (perfisObj != null) ? perfisObj.toString() : "";

            if (perfis.contains("CLIENTE")) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Essa pessoa já é um Cliente cadastrado."));
            }

            pessoaMap.put("perfis", perfis + ",CLIENTE");

            Object idObj = pessoaMap.get("id");
            if (idObj == null) {
                log.error("Campo 'id' não encontrado na resposta da pessoa: {}", pessoaMap);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", "Campo 'id' não encontrado."));
            }

            long pessoaId = Long.parseLong(idObj.toString());
            headers.put("id", String.valueOf(pessoaId));

            // Atualiza pessoa
            producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

            // Monta ClienteEnvioDTO com os dados corretos
            ClienteEnvioDTO clienteEnvioDTO = new ClienteEnvioDTO(
                    pessoaId,
                    clienteDTO.getClienteReg(),
                    clienteDTO.getClienteStatus()
            );

            // Salva cliente
            producerTemplate.sendBodyAndHeaders("direct:salvarCliente", clienteEnvioDTO, headers);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Cliente adicionado com sucesso!"));

        } catch (Exception e) {
            log.error("Erro ao adicionar Cliente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro interno ao processar a solicitação."));
        }
    }

    @GetMapping("/pessoa/{pessoaId}")
    public ResponseEntity<?> buscarClientePorPessoaId(@PathVariable Long pessoaId,
                                                      @RequestHeader("Authorization") String authorizationHeader) {
        try {
            log.info("Buscando cliente com pessoaId: {}", pessoaId);

            Map<String, Object> headers = new HashMap<>();
            headers.put("pessoaId", pessoaId);
            headers.put("Authorization", authorizationHeader);

            ClienteDTO clienteDTO = producerTemplate.requestBodyAndHeaders(
                    "direct:buscarClientePorPessoaId", null, headers, ClienteDTO.class);

            if (clienteDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensagem", "Cliente não encontrado para o pessoaId informado."));
            }

            return ResponseEntity.ok(clienteDTO);

        } catch (Exception e) {
            log.error("Erro ao buscar cliente por pessoaId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensagem", "Erro ao buscar cliente."));
        }
    }
}
