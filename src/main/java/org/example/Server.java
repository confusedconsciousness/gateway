package org.example;

import io.vertx.core.Vertx;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.config.ServerConfig;
import org.example.model.config.VertxConfig;
import org.example.model.config.MicroserviceConfig;
import org.example.model.endpoint.StaticEndpoint;
import org.example.verticle.GatewayVerticle;
import org.example.microservice.Photogram;
import org.example.microservice.UserService;

import java.util.Map;

@Slf4j
@NoArgsConstructor
public class Server {

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        log.info("Starting server");
        // starting point of our server, this method will initialise all the components
        Vertx vertx = Vertx.vertx();
        // for simplicity let's add the config manually, ideally this should come from some config store
        // we are onboarding only two services named photogram (another internal service running on localhost:8921),
        // and user-service that is running on 8945
        ServerConfig config = ServerConfig.builder()
                .vertxConfig(VertxConfig.builder().defaultPort(8080).build())
                .serviceConfigs(Map.of("photogram", MicroserviceConfig.builder()
                        .endpoint(StaticEndpoint.builder()
                                .host("localhost")
                                .port(8921)
                                .build())
                        .build(), "user-service", MicroserviceConfig.builder()
                        .endpoint(StaticEndpoint.builder()
                                .host("localhost")
                                .port(8945)
                                .build())
                        .build()))
                .build();

        // deploy our units
        deployGatewayVerticle(vertx, config);
        deployMicroservices(vertx, config);
    }

    private void deployGatewayVerticle(Vertx vertx, ServerConfig config) {
        vertx.deployVerticle(new GatewayVerticle(config), res -> {
            if (res.succeeded()) {
                log.info("Gateway Verticle deployed");
            } else {
                log.error("Gateway Verticle deploy failed", res.cause());
            }
        });
    }

    private void deployMicroservices(Vertx vertx, ServerConfig config) {
        // deploy the microservices as well in the same project for simplicity
        // our photogram service is responsible for handling posts
        vertx.deployVerticle(new Photogram(config), res -> {
            if (res.succeeded()) {
                log.info("Photogram Service deployed");
            } else {
                log.error("Photogram Service deploy failed", res.cause());
            }
        });
        // our user service is responsible for managing users
        vertx.deployVerticle(new UserService(config), res -> {
            if (res.succeeded()) {
                log.info("User Service deployed");
            } else {
                log.error("User Service deploy failed", res.cause());
            }
        });
    }
}
