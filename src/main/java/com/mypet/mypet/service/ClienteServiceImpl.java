package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.ClienteDTO;
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

            // Validação de autorização

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {

                log.error("Token de autorização não fornecido.");

                return new ResponseEntity<>("Token de autorização não fornecido.", HttpStatus.BAD_REQUEST);

            }

            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);
            headers.put("clienteReg", clienteDTO.getClienteReg());
            headers.put("clienteStatus", clienteDTO.getClienteStatus());

            log.info("Recebendo token no serviço: {}", authorizationHeader);

            log.info("Recebendo CPF no serviço: {}", cpf);

            // Enviar a solicitação para verificar a pessoa pelo CPF (sem a parte de perfis)
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Log da resposta da API

            log.info("Resposta da API para buscar pessoa no serviço: {}", pessoaJson);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Verifica se a pessoa já é um cliente

                if (pessoaMap.containsKey("perfis") && pessoaMap.get("perfis").toString().contains("CLIENTE")) {

                    log.warn("Essa pessoa já é um cliente cadastrado.");

                    return new ResponseEntity<>("Essa pessoa já é um cliente cadastrado.", HttpStatus.BAD_REQUEST);

                } else {

                    // Adicionando "CLIENTE" ao perfil da pessoa

                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",CLIENTE");


                    // Atualiza os dados da pessoa

                    headers.put("id", pessoaMap.get("id").toString());

                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);


                    // Criando e preparando os dados do cliente

                    Map<String, Object> clienteMap = new HashMap<>();

                    clienteMap.put("pessoaId", pessoaMap.get("id"));

                    clienteMap.put("clienteReg", clienteDTO.getClienteReg());

                    clienteMap.put("clienteStatus", clienteDTO.getClienteStatus());


                    // Log dos dados do Cliente antes de enviar para salvar

                    log.info("Dados do Cliente antes de enviar para salvar: {}", clienteMap);


                    // Enviando os dados do cliente para salvar

                    String respostaCliente = producerTemplate.requestBodyAndHeaders("direct:salvarCliente", clienteMap, headers, String.class);

                    log.info("Resposta ao salvar cliente: {}", respostaCliente);


                    // Verifica a resposta da API de salvar

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

            // Melhorando o tratamento de exceções com mais detalhes

            log.error("Erro ao adicionar funcionário: {}", e.getMessage(), e);

            return new ResponseEntity<>("Erro interno ao processar a solicitação.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

}

