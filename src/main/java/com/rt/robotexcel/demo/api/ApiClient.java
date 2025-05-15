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
            HttpRequest protectedRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/protected"))
                .header("Cookie", sessionCookie)
                .GET()
                .build();
            HttpResponse<String> protectedResponse = client.send(protectedRequest, HttpResponse.BodyHandlers.ofString());
            if (protectedResponse.statusCode() != 200) {
                return false;
            } else {
                System.out.println("Login successful!");
                return true;
            }
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

    public String searchPurchaseByInvoice(String numeroNF) {
        try {
            String url = String.format("%s/api/purchase_by_nf?num_nf=%s", baseUrl, numeroNF);
            
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

    public boolean testConnection() {
        try {            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}