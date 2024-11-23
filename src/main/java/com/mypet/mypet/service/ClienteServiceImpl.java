package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.ClienteDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClienteServiceImpl {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Transactional
    public ResponseEntity<?> adicionarCliente(ClienteDTO clienteDTO) {
        try {
            String cpf = clienteDTO.getCpf();
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);

            // Chama a rota Camel para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:clienteBuscarPessoaPorCpf", null, headers, String.class);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                // Converte JSON para Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                if (pessoaMap.get("perfis").toString().contains("CLIENTE")) {
                    return new ResponseEntity<>("Essa pessoa já é um cliente cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    // Adiciona perfil de cliente à pessoa existente
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",CLIENTE");
                    headers.put("id", pessoaMap.get("id"));
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    // Cria e salva o cliente
                    Map<String, Object> clienteMap = new HashMap<>();
                    clienteMap.put("pessoaId", pessoaMap.get("id"));
                    clienteMap.put("clienteReg", clienteDTO.getClienteReg());
                    clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());

                    producerTemplate.sendBody("direct:salvarCliente", clienteMap);

                    return new ResponseEntity<>("Cliente adicionado com sucesso!", HttpStatus.CREATED);
                }
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Erro ao adicionar cliente: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
