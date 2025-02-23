package com.mypet.mypet.domain.dto;

import lombok.Data;
import java.time.LocalDate;

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

