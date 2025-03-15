package org.example;

import io.vertx.core.Vertx;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ServerConfig;
import org.example.config.VertxConfig;
import org.example.model.MicroserviceConfig;
import org.example.model.StaticEndpoint;
import org.example.verticle.ApiVerticle;
import org.example.verticle.microservice.Photogram;
import org.example.verticle.microservice.UserService;

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


        vertx.deployVerticle(new ApiVerticle(config), res -> {
            if (res.succeeded()) {
                log.info("Verticle deployed");
            } else {
                log.error("Verticle deploy failed", res.cause());
            }
        });

        // deploy the microservices as well in the same project
        vertx.deployVerticle(new Photogram(config), res -> {
            if (res.succeeded()) {
                log.info("Photogram service deployed");
            } else {
                log.error("Photogram service deploy failed", res.cause());
            }
        });
        vertx.deployVerticle(new UserService(config), res -> {
            if (res.succeeded()) {
                log.info("User service deployed");
            } else {
                log.error("User service deploy failed", res.cause());
            }
        });

    }
}
