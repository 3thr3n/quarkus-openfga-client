package io.quarkiverse.openfga.deployment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.openfga.client.api.API;
import io.quarkiverse.openfga.client.model.Store;
import io.quarkiverse.openfga.client.model.dto.CreateStoreRequest;
import io.quarkiverse.openfga.client.model.dto.CreateStoreResponse;
import io.quarkiverse.openfga.client.model.dto.ListStoresResponse;
import io.quarkiverse.openfga.client.model.dto.WriteAuthorizationModelResponse;

public class DevServicesStoreInitializer {

    private static final ObjectMapper objectMapper = API.createObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final URI instanceURL;

    public DevServicesStoreInitializer(String instanceURL) {
        this.instanceURL = URI.create(instanceURL);
    }

    public String createStore(String name) throws Exception {

        var requestBody = new CreateStoreRequest(name);

        var url = instanceURL.resolve("/stores");

        var request = HttpRequest.newBuilder(url)
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        var httpResponse = httpClient.send(request, BodyHandlers.ofString());
        if (httpResponse.statusCode() != 201) {
            throw new IOException("Failed to create store for DevServices");
        }

        var response = objectMapper.readValue(httpResponse.body(), CreateStoreResponse.class);

        return response.getId();
    }

    public Optional<String> findStoreId(String name) throws Exception {

        var url = instanceURL.resolve("/stores");

        var request = HttpRequest.newBuilder(url)
                .GET()
                .header("Accept", "application/json")
                .build();

        var httpResponse = httpClient.send(request, BodyHandlers.ofString());
        if (httpResponse.statusCode() != 200) {
            throw new IOException("Failed to list stores for DevServices configuration");
        }

        var response = objectMapper.readValue(httpResponse.body(), ListStoresResponse.class);

        return response.getStores().stream().filter(store -> store.getName().equals(name))
                .findFirst()
                .map(Store::getId);
    }

    public String createAuthorizationModel(String storeId, String modelJSON) throws Exception {

        var url = instanceURL.resolve("/stores/" + storeId + "/authorization-models");

        var request = HttpRequest.newBuilder(url)
                .POST(BodyPublishers.ofString(modelJSON))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        var httpResponse = httpClient.send(request, BodyHandlers.ofString());
        if (httpResponse.statusCode() != 201) {
            throw new IOException("Failed to create authorization model for devservices");
        }

        var response = objectMapper.readValue(httpResponse.body(), WriteAuthorizationModelResponse.class);

        return response.getAuthorizationModelId();
    }

}
