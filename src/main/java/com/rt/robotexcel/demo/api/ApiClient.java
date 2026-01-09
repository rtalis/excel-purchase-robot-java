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

    public String searchPurchaseOrder(String codEmp1, String codPedc) {
        try {
            // Build query with both cod_emp1 and cod_pedc
            String query = codPedc;
            if (codEmp1 != null && !codEmp1.trim().isEmpty()) {
                query = codPedc.trim();
            }

            String url = String.format("%s/api/search_advanced?query=%s&page=1&per_page=200&score_cutoff=100&fields=cod_pedc&selectedFuncName=todos&selectedCodEmp1=%s&ignoreDiacritics=true&exactSearch=true&hideCancelled=false", 
                baseUrl, query, codEmp1 != null && !codEmp1.trim().isEmpty() ? codEmp1.trim() : "todos");
            
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