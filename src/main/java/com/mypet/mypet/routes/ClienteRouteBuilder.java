package com.mypet.mypet.routes;

import com.mypet.mypet.config.PessoaProperties;
import com.mypet.mypet.domain.dtos.clientedto.ClienteDTO;
import com.mypet.mypet.domain.dtos.clientedto.ClienteEnvioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClienteRouteBuilder extends RouteBuilder {

    @Autowired
    private PessoaProperties pessoaProperties;

    @Override
    public void configure() throws Exception {
        // Lidar com exceções de forma geral
        onException(Exception.class)
                .log("❌ Erro ao processar a rota de cliente: ${exception.message}")
                .handled(true);

        from("direct:salvarCliente")
                .log("➡️ Salvando cliente: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }

                    ClienteEnvioDTO dto = exchange.getIn().getBody(ClienteEnvioDTO.class);

                    if (dto.clienteReg() == null || dto.clienteReg().isEmpty())
                        throw new IllegalArgumentException("clienteReg não fornecido.");
                    if (dto.clienteStatus() == null || dto.clienteStatus().isEmpty())
                        throw new IllegalArgumentException("clienteStatus não fornecido.");

                    // Ajusta o status para uppercase
                    ClienteEnvioDTO dtoCorrigido = new ClienteEnvioDTO(
                            dto.pessoaId(),
                            dto.clienteReg(),
                            dto.clienteStatus().toUpperCase()
                    );

                    exchange.getIn().setBody(dtoCorrigido);
                })
                .log("📤 Payload enviado: ${body}")
                .marshal().json()
                .toD("${bean:pessoaProperties.getPessoaClientesBaseUrl}?bridgeEndpoint=true")
// ✅ Aqui está a correção!
                .log("✅ Cliente salvo com sucesso.");

        // Buscar cliente pelo pessoaId
        from("direct:buscarClientePorPessoaId")
                .log("🔍 Buscando cliente pelo pessoaId: ${header.pessoaId}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Authorization", simple("${header.Authorization}"))
                .toD("${bean:pessoaProperties?method=getPessoaClientesBaseUrl()}/pessoa/${header.pessoaId}?bridgeEndpoint=true")

                .log("✅ Cliente encontrado: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, ClienteDTO.class);

        // ✅ Buscar pessoa pelo CPF (corrigido para usar API de pessoas)
        from("direct:buscarClientePorCpf")
                .log("🔍 Buscando pessoa pelo CPF: ${header.cpf}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Authorization", simple("${header.Authorization}"))
                .toD("${bean:pessoaProperties?method=getApiBaseUrl()}/cpf/${header.cpf}?bridgeEndpoint=true")
                .log("✅ Pessoa encontrada: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, Map.class);
    }
}
