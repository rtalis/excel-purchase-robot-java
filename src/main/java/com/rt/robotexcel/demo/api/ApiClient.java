package com.rt.robotexcel.demo.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private final HttpClient client;
    private String sessionCookie;
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public boolean login(String email, String password) {
        try {
            String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            this.sessionCookie = response.headers().firstValue("Set-Cookie")
                .orElse(null);
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String searchPurchaseOrder(String pedido) {
        try {
            String url = String.format("%s/api/search_combined?query=%s&page=1&per_page=200&score_cutoff=100&searchByCodPedc=true&searchByFornecedor=false&searchByObservacao=true&searchByItemId=false&searchByDescricao=false&selectedFuncName=todos", 
                baseUrl, pedido);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Cookie", sessionCookie)
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}