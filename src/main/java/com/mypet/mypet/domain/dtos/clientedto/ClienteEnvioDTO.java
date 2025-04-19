package com.mypet.mypet.domain.dtos.clientedto;


public record ClienteEnvioDTO(
        long pessoaId,
        String clienteReg,
        String clienteStatus
) {}
