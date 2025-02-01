package com.mypet.mypet.service;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String getToken(Exchange exchange) {
        // Captura o cabeçalho Authorization da requisição
        String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);

        // Verifica se o cabeçalho de autorização está presente e começa com "Bearer "
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
        }

        // Extrai o token do cabeçalho (remove o prefixo "Bearer ")
        String token = authorizationHeader.substring(7); // "Bearer <token>" -> "<token>"

        // Log para confirmar se o token está correto
        System.out.println("Token recebido: " + token);

        return token;
    }
}
