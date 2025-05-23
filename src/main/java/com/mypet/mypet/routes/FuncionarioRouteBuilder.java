package com.mypet.mypet.routes;

import com.mypet.mypet.config.PessoaProperties;
import com.mypet.mypet.domain.dtos.FuncionarioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioRouteBuilder extends RouteBuilder {

    @Autowired
    private PessoaProperties pessoaProperties;

    @Override
    public void configure() throws Exception {
        // Tratamento de exceções
        onException(Exception.class)
                .log("❌ Erro ao processar a rota de funcionário: ${exception.message}")
                .handled(true);

        // 🔹 Rota para salvar funcionário
        from("direct:salvarFuncionario")
                .log("➡️ Salvando funcionário: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    if (authorizationHeader == null || authorizationHeader.isEmpty())
                        throw new IllegalArgumentException("Token de autorização não fornecido.");

                    Long pessoaId = Long.parseLong(exchange.getIn().getHeader("id", String.class));
                    String funcionarioReg = exchange.getIn().getHeader("funcionarioReg", String.class);
                    String funcionarioStatus = exchange.getIn().getHeader("funcionarioStatus", String.class);
                    String funcionarioTipo = exchange.getIn().getHeader("funcionarioTipo", String.class);
                    String dataDeAdmissao = exchange.getIn().getHeader("dataDeAdmissao", String.class);
                    String dataDeDemissao = exchange.getIn().getHeader("dataDeDemissao", String.class);

                    if (funcionarioReg == null || funcionarioReg.isEmpty())
                        throw new IllegalArgumentException("funcionarioReg não fornecido.");
                    if (funcionarioStatus == null || funcionarioStatus.isEmpty())
                        throw new IllegalArgumentException("funcionarioStatus não fornecido.");
                    if (funcionarioTipo == null || funcionarioTipo.isEmpty())
                        throw new IllegalArgumentException("funcionarioTipo não fornecido.");

                    FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                    funcionarioDTO.setPessoaId(pessoaId);
                    funcionarioDTO.setFuncionarioReg(funcionarioReg);
                    funcionarioDTO.setFuncionarioStatus(funcionarioStatus.toUpperCase());
                    funcionarioDTO.setFuncionarioTipo(funcionarioTipo);
                    funcionarioDTO.setDataDeAdmissao(dataDeAdmissao);
                    funcionarioDTO.setDataDeDemissao(dataDeDemissao);

                    exchange.getIn().setBody(funcionarioDTO);
                })
                .log("📤 Payload enviado: ${body}")
                .marshal().json(JsonLibrary.Jackson)
                .toD("${bean:pessoaProperties.getPessoaFuncionarioBaseUrl()}?bridgeEndpoint=true")
                .log("✅ Funcionário salvo com sucesso.");

        // 🔹 Rota para buscar funcionário por pessoaId
        from("direct:buscarFuncionarioPorPessoaId")
                .log("🔍 Buscando funcionário pelo pessoaId: ${header.pessoaId}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Authorization", simple("${header.Authorization}"))
                .toD("${bean:pessoaProperties.getPessoaFuncionarioBaseUrl()}/pessoa/${header.pessoaId}?bridgeEndpoint=true")
                .log("📦 Corpo bruto da resposta: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, FuncionarioDTO.class)
                .log("✅ Funcionario encontrado: ${body}");
    }
}
