package com.mypet.mypet.service;


import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String getToken() {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZXJ2acOnby1kZS1jYWRhc3Ryby1CTVQiLCJzdWIiOiJFcmlja0JlbnRvIiwiZXhwIjoxNzM4NDIwNDM3LCJpYXQiOjE3Mzg0MjAxMzd9.l0Z-iOXtuWPTAcBqAIGMeFE5rY3TUsDOqi_Vq680WEuCPjp4BteeMFrBTlVPvLTNvXo2LLjWVLKkfqZv4fGjH9n7XsmyL0p459dRavrbntWVFbLsIJOaq9L07dFsv0K8ku24w_Z4wvien71WRv89MgkNU6OaN4wnPjNVdheGVLJAyZMI1yxe9PP3U8NBwWWqgacqpyApm0BQgvyMCPfZQA3UmFjAAhh5lUxbPXg08d8fGCKPfRS9g9G1iNVgOA0i-MUJJGydMghn_PlO9bxC04DWxf_ncMiNLrZLCxH5J1P9vzJJrSfgfpsTh2kx0Gy5mCCgxyiAMz_fK9tvvzaWbA"; // Substitua pelo token real
        System.out.println("Token gerado: " + token);  // Log para confirmar se o token está correto
        return token;
    }

}
