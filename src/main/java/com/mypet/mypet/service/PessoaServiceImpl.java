package com.mypet.mypet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.mypet.domain.dto.PessoaDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PessoaServiceImpl {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<?> adicionarPessoa(PessoaDTO pessoaDTO) {
        try {
            // Montando o corpo da solicitação com os dados da pessoa
            Map<String, Object> pessoaMap = new HashMap<>();
            pessoaMap.put("nome", pessoaDTO.getNome());
            pessoaMap.put("cpf", pessoaDTO.getCpf());
            pessoaMap.put("sobrenome", pessoaDTO.getSobrenome());
            pessoaMap.put("rg", pessoaDTO.getRg());
            pessoaMap.put("genero", pessoaDTO.getGenero());
            pessoaMap.put("perfis", pessoaDTO.getPerfis());
            pessoaMap.put("email", pessoaDTO.getEmail());
            pessoaMap.put("contato", pessoaDTO.getContato());
            pessoaMap.put("dataNascimento", pessoaDTO.getDataNascimento());
            pessoaMap.put("dataCadastro", pessoaDTO.getDataCadastro());

            // Enviando a solicitação para a rota Camel
            producerTemplate.sendBody("direct:adicionarPessoa", pessoaMap);
            return new ResponseEntity<>("Pessoa adicionada com sucesso!", HttpStatus.CREATED);
        } catch (Exception e) {
            // Log de erro detalhado
            System.err.println("Erro ao adicionar pessoa: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
