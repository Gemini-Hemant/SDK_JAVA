package com.amazonaws.kinesisvideo.demoapp;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientExample{

    public static void main(String[] args) throws Exception {
//        start_recording();
//        Thread.sleep(10000);
//        stop_recording();
//        download_video("100MEDIA/VID00003.MP4");
    }
    public static void start_recording() throws IOException, InterruptedException {
        String url = "http://10.50.0.7/cgi-bin/foream_remote_control?list_files=/tmp/SD0/DCIM"; // Replace with your API endpoint
        String method = "GET"; // Adjust for POST, PUT, etc.

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.noBody()) // Adjust for POST/PUT with data
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        System.out.println("Sending '" + method + "' request to URL : " + url);
        System.out.println("Status Code: " + statusCode);

        if (statusCode == 200) { // Handle successful responses
            String body = response.body();
            System.out.println("Response body: " + body);
        } else { // Handle errors
            System.out.println("Error: Server returned HTTP response code: " + statusCode);
        }
    }
    public static void stop_recording() throws IOException, InterruptedException {
        String url = "http://10.50.0.7/cgi-bin/foream_remote_control?stop_record"; // Replace with your API endpoint
        String method = "GET"; // Adjust for POST, PUT, etc.

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.noBody()) // Adjust for POST/PUT with data
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        System.out.println("Sending '" + method + "' request to URL : " + url);
        System.out.println("Status Code: " + statusCode);

        if (statusCode == 200) { // Handle successful responses
            String body = response.body();
            System.out.println("Response body: " + body);
        } else { // Handle errors
            System.out.println("Error: Server returned HTTP response code: " + statusCode);
        }
    }
    public static void download_video(String path) throws IOException {


        // Make the API request (refer to previous responses for request logic)
        // ... (Replace with your API request code)

        // Assuming the API response contains a "download_url" field
        String downloadUrl = "http://10.50.0.7/DCIM/"+path/* Extract download URL from API response */;

        if (downloadUrl == null || downloadUrl.isEmpty()) {
            throw new RuntimeException("Download URL not found in API response");
        }

        String fileName = "downloaded_video.mkv"; // Adjust filename as needed

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET"); // Adjust if API requires a different method

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(fileName)) {

                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(data)) != -1) {
                    out.write(data, 0, bytesRead);
                }
                System.out.println("Video downloaded successfully: " + fileName);
            }
        } else {
            System.out.println("Error downloading video: HTTP response code " + responseCode);
        }

        connection.disconnect();
    }





}
