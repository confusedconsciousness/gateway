package org.example.verticle.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.example.config.ServerConfig;
import org.example.model.MicroserviceConfig;
import org.example.model.StaticEndpoint;

public class UserService extends AbstractVerticle {
    private final ServerConfig serverConfig;

    public UserService(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        MicroserviceConfig userServiceConfig = serverConfig.getServiceConfigs().get("user-service");

        Router router = Router.router(vertx);
        // we'll be exposing yet another service called user service that will manage our users such as creating, signing up etc
        router.route("/create").handler(routingContext -> {
            routingContext.json(new JsonObject().put("message", "Successfully Registered an User"));
        });


        vertx.createHttpServer()
                .requestHandler(router)
                .listen(((StaticEndpoint) userServiceConfig.getEndpoint()).getPort());

    }
}
