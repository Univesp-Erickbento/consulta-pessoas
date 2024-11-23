package com.mypet.mypet.domain.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PessoaDTO {
    private String nome;
    private String cpf;
    private String sobrenome;
    private String rg;
    private String genero;
    private String perfis;
    private String email;
    private String contato;
    private LocalDate dataNascimento;
    private LocalDate dataCadastro;
}
