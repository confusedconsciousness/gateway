package org.example.verticle.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ServerConfig;
import org.example.model.MicroserviceConfig;
import org.example.model.StaticEndpoint;

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
        // one to post the post and other to view the post
        // please note we are not getting into the nitty gritty of the whole service we are just mocking them
        router.route("/post").handler(context -> {
            context.json(new JsonObject().put("message", "Successfully Created a Post"));
        });

        router.route("/view").handler(context -> {
            context.json(new JsonObject().put("posts", new JsonArray(List.of("P21, P45, P68"))));
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(((StaticEndpoint) postgramConfig.getEndpoint()).getPort());

    }
}
