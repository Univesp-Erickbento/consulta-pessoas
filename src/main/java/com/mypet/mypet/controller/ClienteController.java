package com.mypet.mypet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.ClienteDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarCliente(@RequestBody ClienteDTO clienteDTO, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String cpf = clienteDTO.getCpf();

            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);

            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                if (pessoaMap.get("perfis").toString().contains("CLIENTE")) {
                    return new ResponseEntity<>("Essa pessoa já é um cliente cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",CLIENTE");

                    headers.put("id", pessoaMap.get("id"));
                    headers.put("cpf", cpf);
                    headers.put("Authorization", authorizationHeader);
                    headers.put("clienteReg", clienteDTO.getClienteReg());  // Passando clienteReg nos cabeçalhos
                    headers.put("clienteStatus", clienteDTO.getClienteStatus());  // Passando clienteStatus nos cabeçalhos

                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    producerTemplate.sendBodyAndHeaders("direct:salvarCliente", null, headers);  // Passando cabeçalhos com clienteReg e clienteStatus

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
