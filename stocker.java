///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS com.fasterxml.jackson.core:jackson-databind:2.13.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

@Command(name = "stocker", mixinStandardHelpOptions = true, version = "stock retriever 0.1",
        description = "ESTC stock value retriever")
class stocker implements Callable<Integer> {

    @Option(names = {"-A", "--accesskey"}, description = "Access key retrived from marketstack.com", required = false)
    private String accesskey;

    @Parameters(description = "vesting date (yyyy-mm-dd)")
    private String dateTo;

    public static void main(String... args) {
        int exitCode = new CommandLine(new stocker()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        if (accesskey == null) {
            accesskey = System.getenv().get("MARKET_STOCK_API_KEY");
            if (accesskey == null) {
                System.out.println("No market access key found");
                System.exit(1);
            }
        }

        URI uri = new URI("http://api.marketstack.com/v1/eod?access_key=" + accesskey + "&symbols=ESTC&date_to=" + dateTo + "&limit=31");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .build();
        
        HttpResponse<String> response = HttpClient
            .newBuilder()
            .build()
            .send(request, BodyHandlers.ofString());

        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.body());
        ArrayNode data = (ArrayNode)jsonNode.get("data");

        if (data == null) {
            System.out.println("No data available");
            System.exit(2);
        }
        
        System.out.println("date, close, open, high, low");
        for (JsonNode tickAtDay : data) {
            final String tickDate = tickAtDay.get("date").asText();
            double closingValue = tickAtDay.get("close").asDouble();
            double openingValue = tickAtDay.get("open").asDouble();
            double highValue = tickAtDay.get("high").asDouble();
            double lowValue = tickAtDay.get("low").asDouble();
            String csvRow = String.join(", ", tickDate, Double.toString(closingValue), Double.toString(openingValue), 
                Double.toString(highValue), Double.toString(lowValue));
            System.out.println(csvRow);
        }
        return 0;
    }
}
