package com.mypet.mypet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration
@ConfigurationProperties(prefix = "pessoas")
public class PessoaProperties {

    private String clientesBaseUrl;
    private String funcionariosBaseUrl;
    private String fornecedoresBaseUrl;
    private String apiBaseUrl; // ✅ novo campo
    private String pessoaClientesBaseUrl;
    private String pessoaFuncionarioBaseUrl;


    // Getters e setters

    public String getPessoaClientesBaseUrl() {
        return pessoaClientesBaseUrl;
    }

    public void setPessoaClientesBaseUrl(String pessoaClientesBaseUrl) {
        this.pessoaClientesBaseUrl = pessoaClientesBaseUrl;}

    public String getPessoaFuncionarioBaseUrl() {
        return pessoaFuncionarioBaseUrl;
    }

    public void setPessoaFuncionarioBaseUrl(String pessoaFuncionarioBaseUrl) {
        this.pessoaFuncionarioBaseUrl = pessoaFuncionarioBaseUrl;
    }

    public String getClientesBaseUrl() {
        return clientesBaseUrl;
    }

    public void setClientesBaseUrl(String clientesBaseUrl) {
        this.clientesBaseUrl = clientesBaseUrl;
    }

    public String getFuncionariosBaseUrl() {
        return funcionariosBaseUrl;
    }

    public void setFuncionariosBaseUrl(String funcionariosBaseUrl) {
        this.funcionariosBaseUrl = funcionariosBaseUrl;
    }

    public String getFornecedoresBaseUrl() {
        return fornecedoresBaseUrl;
    }

    public void setFornecedoresBaseUrl(String fornecedoresBaseUrl) {
        this.fornecedoresBaseUrl = fornecedoresBaseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
}
