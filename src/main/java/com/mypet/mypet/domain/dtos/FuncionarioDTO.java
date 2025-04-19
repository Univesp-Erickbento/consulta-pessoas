package com.mypet.mypet.domain.dtos;

import lombok.Data;

@Data
public class FuncionarioDTO {
    private long pessoaId;
    private String cpf;
    private String funcionarioTipo;
    private String funcionarioReg;
    private String funcionarioStatus;
    private String dataDeAdmissao;
    private String dataDeDemissao;
}

