package com.mypet.mypet.routes;

import com.mypet.mypet.domain.dtos.FuncionarioDTO;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Tratar exceções dentro das rotas
        onException(Exception.class)
                .log("Erro ao processar a rota: ${exception.message}")
                .handled(true);

        // 📌 Rota para salvar o funcionário (padrão igual ao cliente)
        from("direct:salvarFuncionario")
                .log("Salvando funcionário: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    String funcionarioReg = exchange.getIn().getHeader("funcionarioReg", String.class);
                    String funcionarioStatus = exchange.getIn().getHeader("funcionarioStatus", String.class);
                    String funcionarioTipo = exchange.getIn().getHeader("funcionarioTipo", String.class);
                    String dataDeAdmissao = exchange.getIn().getHeader("dataDeAdmissao", String.class);
                    String dataDeDemissao = exchange.getIn().getHeader("dataDeDemissao", String.class);
                    String idHeader = exchange.getIn().getHeader("id", String.class);

                    // Validações
                    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }
                    if (funcionarioReg == null || funcionarioReg.isEmpty()) {
                        throw new IllegalArgumentException("funcionarioReg não fornecido.");
                    }
                    if (funcionarioStatus == null || funcionarioStatus.isEmpty()) {
                        throw new IllegalArgumentException("funcionarioStatus não fornecido.");
                    }
                    if (funcionarioTipo == null || funcionarioTipo.isEmpty()) {
                        throw new IllegalArgumentException("funcionarioTipo não fornecido.");
                    }
                    if (idHeader == null || idHeader.isEmpty()) {
                        throw new IllegalArgumentException("ID não fornecido.");
                    }

                    long pessoaId = Long.parseLong(idHeader);

                    // Criar o DTO
                    FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                    funcionarioDTO.setPessoaId(pessoaId);
                    funcionarioDTO.setFuncionarioReg(funcionarioReg);
                    funcionarioDTO.setFuncionarioStatus(funcionarioStatus.toUpperCase());
                    funcionarioDTO.setFuncionarioTipo(funcionarioTipo);
                    funcionarioDTO.setDataDeAdmissao(dataDeAdmissao);
                    funcionarioDTO.setDataDeDemissao(dataDeDemissao);

                    exchange.getIn().setBody(funcionarioDTO);

                    System.out.println("FuncionarioDTO a ser enviado: " + funcionarioDTO);
                })
                .log("Payload enviado: ${body}")
                .marshal().json()
                .to("http://192.168.15.115:9090/api/funcionarios")
                .log("Funcionário salvo com sucesso.");

        // 📌 Rota para buscar funcionário por pessoaId
        from("direct:buscarFuncionarioPorPessoaId")
                .setHeader("CamelHttpMethod", constant("GET"))
                .process(exchange -> {
                    String pessoaId = exchange.getIn().getHeader("pessoaId", String.class);
                    String token = exchange.getIn().getHeader("Authorization", String.class);

                    if (pessoaId == null || pessoaId.isEmpty()) {
                        throw new IllegalArgumentException("PessoaId não fornecido.");
                    }
                    if (token == null || token.isEmpty()) {
                        throw new IllegalArgumentException("Token de autorização não fornecido.");
                    }

                    // 📌 Aqui é o endpoint da API externa (mesmo que o cliente)
                    String url = "http://192.168.15.115:9090/api/funcionarios/pessoa/" + pessoaId;
                    exchange.getIn().setHeader("CamelHttpUri", url);
                    exchange.getIn().setHeader("Authorization", token);
                })
                .toD("${header.CamelHttpUri}?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log("Resposta da busca por funcionário: ${body}");
    }
}
