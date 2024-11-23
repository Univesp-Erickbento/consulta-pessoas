package com.mypet.mypet.domain.dto;

import lombok.Data;

@Data
public class ClienteDTO {
    private String cpf;
    private long pessoaId;
    private String clienteReg;
    private String clienteStatus;
}
