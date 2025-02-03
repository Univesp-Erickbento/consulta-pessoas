package com.mypet.mypet.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FuncionarioRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(createObjectMapper(), Object.class);

        // Rota para salvar o funcionário e atualizar o perfil da pessoa
        from("direct:salvarFuncionario")
                // Passo 1: Buscar a pessoa associada ao CPF do funcionário
                .process(exchange -> {
                    // Acessando diretamente a chave cpf no corpo da mensagem (que é um HashMap)
                    Map<String, Object> body = exchange.getIn().getBody(Map.class);
                    String cpf = (String) body.get("cpf");
                    exchange.getIn().setHeader("cpf", cpf); // Passando o CPF como cabeçalho para a próxima etapa
                })
                .to("direct:funcionarioBuscarPessoaPorCpf") // Busca a pessoa pelo CPF

                // Passo 2: Adicionar o perfil "FUNCIONARIO" à pessoa (caso ainda não tenha esse perfil)
                .process(exchange -> {
                    String pessoaJson = exchange.getIn().getBody(String.class);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, Map.class);

                    List<String> perfis = (List<String>) pessoaMap.get("perfis");
                    if (!perfis.contains("FUNCIONARIO")) {
                        perfis.add("FUNCIONARIO");
                        pessoaMap.put("perfis", perfis);
                    }
                    exchange.getIn().setBody(pessoaMap); // Atualiza o corpo com os dados da pessoa modificados
                })

                // Passo 3: Atualizar a pessoa com o novo perfil
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${header.Authorization}")) // Reutilizando o token da primeira requisição
                .to("http://localhost:9090/api/pessoas/${header.id}") // Atualiza a pessoa com o novo perfil
                .log("Pessoa atualizada com sucesso com o novo perfil.")

                // Passo 4: Salvar o funcionário
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${header.Authorization}")) // Reutilizando o token da primeira requisição
                .marshal(jacksonDataFormat)
                .to("http://localhost:9090/api/funcionarios")
                .log("Funcionário salvo com sucesso.");

        // Rota para deletar o funcionário
        from("direct:deletarFuncionario")
                .setHeader("CamelHttpMethod", constant("DELETE"))
                .to("http://localhost:9090/api/funcionarios/${header.id}");

        // Reutilizando a rota de busca de pessoa por CPF
        from("direct:funcionarioBuscarPessoaPorCpf")
                .to("direct:buscarPessoaPorCpf");
    }


    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
}
