package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dtos.FuncionarioDTO;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class FuncionarioServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioServiceImpl.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<?> adicionarFuncionario(FuncionarioDTO funcionarioDTO, String authorizationHeader) {
        try {
            String cpf = funcionarioDTO.getCpf();

            // Validação de autorização
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                log.error("Token de autorização não fornecido.");
                return new ResponseEntity<>("Token de autorização não fornecido.", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", authorizationHeader);
            headers.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());  // Adicionando funcionarioTipo
            headers.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());    // Adicionando funcionarioReg
            headers.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus()); // Adicionando funcionarioStatus
            headers.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao() != null ? funcionarioDTO.getDataDeAdmissao().toString() : null); // Adicionando dataDeAdmissao
            headers.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao() != null ? funcionarioDTO.getDataDeDemissao().toString() : null); // Adicionando dataDeDemissao

            log.info("Recebendo token no serviço: {}", authorizationHeader);
            log.info("Recebendo CPF no serviço: {}", cpf);

            // Enviar requisição para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Log da resposta da API
            log.info("Resposta da API para buscar pessoa no serviço: {}", pessoaJson);

            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Verifica se a pessoa já é um funcionário
                if (pessoaMap.containsKey("perfis") && pessoaMap.get("perfis").toString().contains("FUNCIONARIO")) {
                    log.warn("Essa pessoa já é um funcionário cadastrado.");
                    return new ResponseEntity<>("Essa pessoa já é um funcionário cadastrado.", HttpStatus.BAD_REQUEST);
                } else {
                    // Adicionando "FUNCIONARIO" ao perfil da pessoa
                    pessoaMap.put("perfis", pessoaMap.get("perfis") + ",FUNCIONARIO");

                    // Atualiza os dados da pessoa
                    headers.put("id", pessoaMap.get("id").toString());
                    producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                    // Criando e preparando os dados do funcionário
                    Map<String, Object> funcionarioMap = new HashMap<>();
                    funcionarioMap.put("pessoaId", pessoaMap.get("id"));
                    funcionarioMap.put("funcionarioTipo", funcionarioDTO.getFuncionarioTipo());
                    funcionarioMap.put("funcionarioReg", funcionarioDTO.getFuncionarioReg());
                    funcionarioMap.put("funcionarioStatus", funcionarioDTO.getFuncionarioStatus());

                    // Adicionando as datas, se existirem
                    if (funcionarioDTO.getDataDeAdmissao() != null) {
                        funcionarioMap.put("dataDeAdmissao", funcionarioDTO.getDataDeAdmissao().toString());
                    }

                    if (funcionarioDTO.getDataDeDemissao() != null) {
                        funcionarioMap.put("dataDeDemissao", funcionarioDTO.getDataDeDemissao().toString());
                    }

                    // Log dos dados do funcionário antes de enviar para salvar
                    log.info("Dados do funcionário antes de enviar para salvar: {}", funcionarioMap);

               // Enviando os dados do funcionário para salvar
                    String respostaFuncionario = producerTemplate.requestBodyAndHeaders("direct:salvarFuncionario", funcionarioMap, headers, String.class);
                    log.info("Resposta ao salvar funcionário: {}", respostaFuncionario);

                    // Verifica a resposta da API de salvar
                    if (respostaFuncionario != null && !respostaFuncionario.isEmpty()) {
                        return new ResponseEntity<>("Funcionário adicionado com sucesso!", HttpStatus.CREATED);
                    } else {
                        log.error("Erro ao salvar o funcionário: Resposta vazia ou mal formatada.");
                        return new ResponseEntity<>("Erro ao salvar o funcionário.", HttpStatus.INTERNAL_SERVER_ERROR);
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
