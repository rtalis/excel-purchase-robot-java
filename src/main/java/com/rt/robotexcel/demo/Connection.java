package com.rt.robotexcel.demo;


import java.net.*;
import java.io.*;
import io.github.cdimascio.dotenv.Dotenv;

public class Connection {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("TOKEN");
        String baseUrl = dotenv.get("BASE_URL");
        
        // Use the token in a request to protected endpoint
        if (token != null) {
            URL protectedUrl = new URL(baseUrl + "/auth/protected");
            HttpURLConnection protectedConn = (HttpURLConnection) protectedUrl.openConnection();
            protectedConn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(protectedConn.getInputStream()));
            String inputLine;
            System.out.println("Response from protected endpoint:");
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        } else {
            System.out.println("No token found in .env file.");
        }
    }
}