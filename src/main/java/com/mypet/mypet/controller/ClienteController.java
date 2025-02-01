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
    private ProducerTemplate producerTemplate;  // Certifique-se de que esta variável está corretamente injetada

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarCliente(@RequestBody ClienteDTO clienteDTO, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Passo 1: Obter o CPF do ClienteDTO
            String cpf = clienteDTO.getCpf();

            // Passo 2: Criar um Map de cabeçalhos para enviar para a rota Camel
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);  // Adicionando o cabeçalho Authorization recebido no Request

            // Passo 3: Chamar a rota Camel para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Verificação se a resposta não está vazia
            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                // Passo 4: Converter o JSON da resposta para um Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Passo 5: Verificar se a pessoa já possui o perfil de cliente
                if (pessoaMap.get("perfis").toString().contains("CLIENTE")) {
                    return new ResponseEntity<>("Essa pessoa já é um cliente cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    // Passo 6: Adicionar o perfil de cliente à pessoa existente
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",CLIENTE");

                    // Adicionar o ID da pessoa aos cabeçalhos para atualização
                    headers.put("id", pessoaMap.get("id"));

                    // Enviar a pessoa atualizada para a rota de atualização
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    // Passo 7: Criar e preparar o objeto cliente para salvar, sem enviar o CPF
                    Map<String, Object> clienteMap = new HashMap<>();
                    clienteMap.put("pessoaId", pessoaMap.get("id"));
                    clienteMap.put("clienteReg", clienteDTO.getClienteReg());
                    clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());

                    // Enviar o objeto cliente para a rota de salvar cliente
                    producerTemplate.sendBody("direct:salvarCliente", clienteMap);

                    return new ResponseEntity<>("Cliente adicionado com sucesso!", HttpStatus.CREATED);
                }
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Log de erro detalhado
            System.err.println("Erro ao adicionar cliente: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
