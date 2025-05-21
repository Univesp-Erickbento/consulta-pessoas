package com.mypet.mypet.domain.dtos.clientedto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteDTO {
    private String cpf;
    private long pessoaId;
    private String clienteReg;
    private String clienteStatus;
}
