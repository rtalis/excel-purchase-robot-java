package com.rt.robotexcel.demo;


import java.net.*;
import java.util.List;
import java.util.Map;
import java.io.*;
import io.github.cdimascio.dotenv.Dotenv;

public class Connection {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String email = dotenv.get("EMAIL");
        String password = dotenv.get("PASSWORD");
        String baseUrl = dotenv.get("BASE_URL");
        
        String jsonInput = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        

        URL url = new URL(baseUrl + "/auth/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

       
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes());
        }

        String sessionCookie = null;
        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> cookies = headers.get("Set-Cookie");
        
        if (cookies != null) {
            for (String header : cookies) {
                if (header.startsWith("session")) {
                    sessionCookie = header.split(";")[0];
                    break;
                }
            }
        }

        if (sessionCookie != null) {
            System.out.println("Session cookie: " + sessionCookie + "\n\n\n");
        }
        else {
            System.out.println("No session cookie found.");
        }

        // Use the cookie in a subsequent request
        if (sessionCookie != null) {
            URL protectedUrl = new URL(baseUrl + "/auth/protected");
            HttpURLConnection protectedConn = (HttpURLConnection) protectedUrl.openConnection();
            protectedConn.setRequestProperty("Cookie", sessionCookie);

            BufferedReader in = new BufferedReader(new InputStreamReader(protectedConn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        }
    }
}