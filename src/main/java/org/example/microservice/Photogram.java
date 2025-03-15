package org.example.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.example.model.config.ServerConfig;
import org.example.model.config.MicroserviceConfig;
import org.example.model.endpoint.StaticEndpoint;

import java.util.List;

@Slf4j
public class Photogram extends AbstractVerticle {
    // this verticle will act like a microservice (postgram)
    private final ServerConfig serverConfig;

    public Photogram(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        MicroserviceConfig postgramConfig = serverConfig.getServiceConfigs().get("photogram");
        Router router = Router.router(vertx);

        // our photogram service will expose two apis
        // 1. one to post the post and other to view the post
        // 2. please note we are not getting into the nitty-gritty of the whole service we are just mocking them
        router.route("/post").handler(context -> {
            log.info("Creating a new Post on Photogram");
            context.json(new JsonObject().put("message", "Successfully Created a Post"));
        });

        router.route("/view").handler(context -> {
            log.info("Received a view request on Photogram");
            context.json(new JsonObject().put("posts", new JsonArray(List.of("P21, P45, P68"))));
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(((StaticEndpoint) postgramConfig.getEndpoint()).getPort());

    }
}
