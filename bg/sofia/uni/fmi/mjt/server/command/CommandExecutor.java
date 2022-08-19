package bg.sofia.uni.fmi.mjt.server.command;

import bg.sofia.uni.fmi.mjt.server.exceptions.BadInputParameterException;
import bg.sofia.uni.fmi.mjt.server.exceptions.FoodAnalyzerException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoResultsFoundException;
import bg.sofia.uni.fmi.mjt.server.food.Food;
import bg.sofia.uni.fmi.mjt.server.food.FoodByName;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class CommandExecutor {

    private static final String GET_FOOD = "get-food";
    private static final String GET_FOOD_REPORT = "get-food-report";
    private static final String GET_FOOD_BY_BARCODE = "get-food-by-barcode";
    private static final String DISCONNECT = "disconnect";
    private static final String UNKNOWN_COMMAND = "Unknown command";

    private static final String API_KEY = "PUT_YOUR_API_KEY_HERE";
    private static final String API_ENDPOINT_SCHEME = "https";
    private static final String API_ENDPOINT_HOST = "api.nal.usda.gov";
    private static final String API_ENDPOINT_SEARCH_PATH = "/fdc/v1/foods/search";
    private static final String API_ENDPOINT_ID_PATH = "/fdc/v1/food/";
    private static final String API_ENDPOINT_SEARCH_QUERY = "query=%s&api_key=%s";
    private static final String API_ENDPOINT_ID_QUERY = "requireAllWords=true&api_key=%s";

    private static final String UNEXPECTED_RESPONSE_CODE_ERROR_MESSAGE = "Unexpected response code from the service server. Try again later or contact administrator.";
    private static final String COULD_NOT_RETRIEVE_ANALYSIS_ERROR_MESSAGE = "Could not retrieve food analysis. Try again later or contact administrator.";

    private static final Gson GSON = new Gson();

    private final Path pathOfCacheFile;
    private final Path pathOfLogFile;

    private final HttpClient httpClient;

    private final Set<String> barcodes;

    public CommandExecutor(Path pathOfCacheFile, Path pathOfLogFile, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.pathOfCacheFile = pathOfCacheFile;
        this.pathOfLogFile = pathOfLogFile;
        barcodes = new HashSet<>();
    }

    public String execute(Command cmd) throws FoodAnalyzerException {
        return switch (cmd.command()) {
            case GET_FOOD            -> getFood(cmd.arguments());
            case GET_FOOD_REPORT     -> getFoodReport(cmd.arguments());
            case GET_FOOD_BY_BARCODE -> getFoodByBarcode(cmd.arguments());
            case DISCONNECT          -> disconnect();
            default                  -> UNKNOWN_COMMAND;
        };
    }

    private String getFood(String[] arguments) throws FoodAnalyzerException {
        HttpResponse<String> response;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_SEARCH_PATH, API_ENDPOINT_SEARCH_QUERY.formatted(getFoodFromInput(arguments), API_KEY), null);
            response = getResponse(uri);
        } catch (Exception e) {
            writeToLogFile(pathOfLogFile, COULD_NOT_RETRIEVE_ANALYSIS_ERROR_MESSAGE);
            throw new FoodAnalyzerException(COULD_NOT_RETRIEVE_ANALYSIS_ERROR_MESSAGE, e);
        }

        if (response.statusCode() == HttpsURLConnection.HTTP_OK) {
            String foodAnalysis = GSON.fromJson(response.body(), FoodByName.class).toString();
            writeAnalysisToFile(pathOfCacheFile, foodAnalysis);

            return foodAnalysis;
        }

        handleOccurredErrors(response);

        writeToLogFile(pathOfLogFile, UNEXPECTED_RESPONSE_CODE_ERROR_MESSAGE);
        throw new FoodAnalyzerException(UNEXPECTED_RESPONSE_CODE_ERROR_MESSAGE);
    }

    private String getFoodReport(String[] arguments) throws FoodAnalyzerException {
        HttpResponse<String> response;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_ID_PATH + arguments[0], API_ENDPOINT_ID_QUERY.formatted(API_KEY), null);
            response = getResponse(uri);
        } catch (Exception e) {
            writeToLogFile(pathOfLogFile, COULD_NOT_RETRIEVE_ANALYSIS_ERROR_MESSAGE);
            throw new FoodAnalyzerException(COULD_NOT_RETRIEVE_ANALYSIS_ERROR_MESSAGE, e);
        }

        if (response.statusCode() == HttpsURLConnection.HTTP_OK) {
            String foodAnalysis = GSON.fromJson(response.body(), Food.class).toString() + System.lineSeparator();
            writeAnalysisToFile(pathOfCacheFile, foodAnalysis);

            return foodAnalysis;
        }

        handleOccurredErrors(response);

        writeToLogFile(pathOfLogFile, UNEXPECTED_RESPONSE_CODE_ERROR_MESSAGE);
        throw new FoodAnalyzerException(UNEXPECTED_RESPONSE_CODE_ERROR_MESSAGE);
    }

    private String getFoodByBarcode(String[] arguments) {
        try (var reader = Files.newBufferedReader(pathOfCacheFile)) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (getBarcodeFromLine(line).equals(arguments[0])) {
                    return line + System.lineSeparator();
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a cache file", e);
        }

        return "There is no such barcode in the data set" + System.lineSeparator();
    }

    private String disconnect() {
        return "Disconnecting from the server";
    }

    private String getBarcodeFromLine(String line) {
        int index = line.indexOf("gtinUpc") + "gtinUpc".length() + 2;
        char[] chars = line.toCharArray();
        StringBuilder sb = new StringBuilder();

        for (int i = index; i < line.length() - index; i++) {
            if (chars[i] == '\'') {
                break;
            } else {
                sb.append(chars[i]);
            }
        }

        return sb.toString();
    }

    private void writeAnalysisToFile(Path file, String analysis) {
        String[] foods = analysis.split(System.lineSeparator());

        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (String food : foods) {
                String barcode = getBarcodeFromLine(food);
                if (!barcodes.contains(barcode)) {
                    writer.write(food + System.lineSeparator());
                    writer.flush();
                    barcodes.add(barcode);
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a cache file", e);
        }
    }

    private void writeToLogFile(Path file, String message) {
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            String messageToWrite = String.format("[%s] %s", LocalDateTime.now(), message);
            writer.write(messageToWrite);
            writer.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a log file", e);
        }
    }

    private String getFoodFromInput(String[] input) {
        StringBuilder sb = new StringBuilder();

        for (String str : input) {
            sb.append(str);
            sb.append(" ");
        }

        return sb.toString();
    }

    private void handleOccurredErrors(HttpResponse<String> response) throws FoodAnalyzerException {
        if (response.statusCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
            throw new BadInputParameterException("There is a bad input parameter");
        }

        if (response.statusCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
            throw new NoResultsFoundException("There are no results from this search");
        }
    }

    private HttpResponse<String> getResponse(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
