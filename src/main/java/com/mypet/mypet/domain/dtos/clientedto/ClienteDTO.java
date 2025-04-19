package com.mypet.mypet.domain.dtos.clientedto;

import lombok.Data;

@Data
public class ClienteDTO {
    private String cpf;
    private long pessoaId;
    private String clienteReg;
    private String clienteStatus;
}
