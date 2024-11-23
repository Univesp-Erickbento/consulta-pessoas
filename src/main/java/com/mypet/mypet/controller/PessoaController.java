package com.mypet.mypet.controller;

import com.mypet.mypet.domain.dto.PessoaDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarPessoa(@RequestBody PessoaDTO pessoaDTO) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("nome", pessoaDTO.getNome());
            headers.put("cpf", pessoaDTO.getCpf());
            headers.put("sobrenome", pessoaDTO.getSobrenome());
            headers.put("rg", pessoaDTO.getRg());
            headers.put("genero", pessoaDTO.getGenero());
            headers.put("perfis", pessoaDTO.getPerfis());
            headers.put("email", pessoaDTO.getEmail());
            headers.put("contato", pessoaDTO.getContato());
            headers.put("dataNascimento", pessoaDTO.getDataNascimento());
            headers.put("dataCadastro", pessoaDTO.getDataCadastro());

            producerTemplate.sendBodyAndHeaders("direct:adicionarPessoa", null, headers);
            return new ResponseEntity<>("Pessoa adicionada com sucesso!", HttpStatus.CREATED);
        } catch (Exception e) {
            // Log de erro detalhado
            System.err.println("Erro ao adicionar pessoa: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
