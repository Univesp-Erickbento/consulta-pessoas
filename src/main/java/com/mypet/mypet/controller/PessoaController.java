package com.mypet.mypet.controller;

import com.mypet.mypet.domain.dto.PessoaDTO;
import com.mypet.mypet.service.PessoaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    @Autowired
    private PessoaServiceImpl pessoaService;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarPessoa(@RequestBody PessoaDTO pessoaDTO) {
        return pessoaService.adicionarPessoa(pessoaDTO);
    }
}
