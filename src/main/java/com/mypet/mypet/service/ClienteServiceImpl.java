package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dtos.clientedto.ClienteDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClienteServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(ClienteServiceImpl.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<?> adicionarCliente(ClienteDTO clienteDTO, String authorizationHeader) {
        try {
            String cpf = clienteDTO.getCpf();

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                log.error("Token de autorização não fornecido.");
                return new ResponseEntity<>("Token de autorização não fornecido.", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);
            headers.put("clienteReg", clienteDTO.getClienteReg());
            headers.put("clienteStatus", clienteDTO.getClienteStatus());

            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);
            log.info("Resposta da API para buscar pessoa no serviço: {}", pessoaJson);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                if (pessoaMap.containsKey("perfis") && pessoaMap.get("perfis").toString().contains("CLIENTE")) {
                    log.warn("Essa pessoa já é um cliente cadastrado.");
                    return new ResponseEntity<>("Essa pessoa já é um cliente cadastrada.", HttpStatus.BAD_REQUEST);
                } else {
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",CLIENTE");
                    headers.put("id", pessoaMap.get("id").toString());

                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    Map<String, Object> clienteMap = new HashMap<>();
                    clienteMap.put("pessoaId", pessoaMap.get("id"));
                    clienteMap.put("clienteReg", clienteDTO.getClienteReg());
                    clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());

                    String respostaCliente = producerTemplate.requestBodyAndHeaders("direct:salvarCliente", clienteMap, headers, String.class);
                    log.info("Resposta ao salvar cliente: {}", respostaCliente);

                    if (respostaCliente != null && !respostaCliente.isEmpty()) {
                        return new ResponseEntity<>("Cliente adicionado com sucesso!", HttpStatus.CREATED);
                    } else {
                        log.error("Erro ao salvar o cliente: Resposta vazia ou mal formatada.");
                        return new ResponseEntity<>("Erro ao salvar o cliente.", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                log.warn("Pessoa não encontrada com CPF: {}", cpf);
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error("Erro ao adicionar cliente: {}", e.getMessage(), e);
            return new ResponseEntity<>("Erro interno ao processar a solicitação.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> buscarClientePorId(Long id, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("id", id);
            headers.put("Authorization", token);

            String clienteJson = producerTemplate.requestBodyAndHeaders("direct:buscarClientePorId", null, headers, String.class);
            log.info("Resposta buscarClientePorId: {}", clienteJson);

            if (clienteJson == null || clienteJson.isEmpty()) {
                return new ResponseEntity<>("Cliente não encontrado.", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> cliente = objectMapper.readValue(clienteJson, Map.class);
            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            log.error("Erro ao buscar cliente por ID: {}", e.getMessage(), e);
            return new ResponseEntity<>("Erro ao buscar cliente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> buscarClientePorCpf(String cpf, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", token);

            String clienteJson = producerTemplate.requestBodyAndHeaders("direct:buscarClientePorCpf", null, headers, String.class);
            log.info("Resposta buscarClientePorCpf: {}", clienteJson);

            if (clienteJson == null || clienteJson.isEmpty()) {
                return new ResponseEntity<>("Cliente não encontrado.", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> cliente = objectMapper.readValue(clienteJson, Map.class);
            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            log.error("Erro ao buscar cliente por CPF: {}", e.getMessage(), e);
            return new ResponseEntity<>("Erro ao buscar cliente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> buscarClientePorPessoaId(Long pessoaId, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("pessoaId", pessoaId);
            headers.put("Authorization", token);

            String clienteJson = producerTemplate.requestBodyAndHeaders("direct:buscarClientePorPessoaId", null, headers, String.class);
            log.info("Resposta buscarClientePorPessoaId: {}", clienteJson);

            if (clienteJson == null || clienteJson.isEmpty()) {
                return new ResponseEntity<>("Cliente não encontrado.", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> cliente = objectMapper.readValue(clienteJson, Map.class);
            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            log.error("Erro ao buscar cliente por pessoaId: {}", e.getMessage(), e);
            return new ResponseEntity<>("Erro ao buscar cliente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> atualizarCliente(Long id, ClienteDTO clienteDTO, String token) {
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

            return new ResponseEntity<>("Cliente atualizado com sucesso!", HttpStatus.OK);

        } catch (Exception e) {
            log.error("Erro ao atualizar cliente: {}", e.getMessage(), e);
            return new ResponseEntity<>("Erro ao atualizar cliente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> deletarCliente(Long id, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("id", id);
            headers.put("Authorization", token);

            producerTemplate.sendBodyAndHeaders("direct:deletarCliente", null, headers);

            return new ResponseEntity<>("Cliente deletado com sucesso!", HttpStatus.OK);

        } catch (Exception e) {
            log.error("Erro ao deletar cliente: {}", e.getMessage(), e);
            return new ResponseEntity<>("Erro ao deletar cliente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
