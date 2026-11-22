import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpWrapper {
    public enum RequestType 
        {GET, POST, PUT}   

    private final HttpClient client = HttpClient.newHttpClient();
    private final HttpRequest request;
    private HttpResponse<String> response = null;
    
    public HttpWrapper(String requestedURI, String jsonPayload, RequestType type)
    {
        switch (type)
        {
            case POST:
                request = HttpRequest.newBuilder()
                .uri(URI.create(requestedURI))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
                break;
            case PUT:
                request = HttpRequest.newBuilder()
                .uri(URI.create(requestedURI))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
                break;
            default: // we'll treat everything else as a GET
                request = HttpRequest.newBuilder()
                .uri(URI.create(requestedURI))
                .build();
                break;
        }
    }

    // this just packages everything into a lightweight method call.
    public String go()
    {
        makeRoundTrip();
        return getResponse();
    }
    
    public void makeRoundTrip()
    {
        try 
        {
            response = client.send(request, BodyHandlers.ofString());
        } 
        catch (Exception e) 
        {
            System.out.printf("HttpClient.send can return multiple exceptions, but our wrapper only catches Exception due to time constraints.\nThe error message is:\n%s\n--Anything below this point is a new exception caught elsewhere.--\n", e.getMessage());
        }
        client.close();
    }

    public String getResponse()
    {
        return response.body();
    }
}