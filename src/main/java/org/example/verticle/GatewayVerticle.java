package org.example.verticle;

import com.google.common.base.Strings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.example.model.config.ServerConfig;
import org.example.model.endpoint.EndpointType;
import org.example.model.config.MicroserviceConfig;
import org.example.model.endpoint.StaticEndpoint;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class GatewayVerticle extends AbstractVerticle {
    private final ServerConfig serverConfig;
    public static String DEFAULT_MOUNT_PATH = "/services/:service/*";

    private WebClient webClient;

    public GatewayVerticle(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // create a router
        Router router = Router.router(vertx);
        this.webClient = WebClient.create(vertx);
        // mount this router for the requests that begins with /services (because that is convention we are going to follow)
        // if somebody comes at some other path say /random/* we will fail it

        // when someone will come to /services/name-of-service/anything,
        // our router will come into the picture, and it will go through all these handlers one by one
        // handlers are meant for a lot of thing, we have a validator handler that will check whether the request is correct
        // end handler simply just marks the end of the request
        router.route(DEFAULT_MOUNT_PATH)
                .handler(this::validator)
                .handler(this::forwarder)
                .handler(this::endRouter);

        // the above was just a router, but we want to create an HTTP server
        // that will listen and when the request will land, our router will get triggered
        HttpServerOptions defaultHttpServerOptions = new HttpServerOptions();
        HttpServer server = vertx.createHttpServer(defaultHttpServerOptions);

        server.requestHandler(router)
                .listen(serverConfig.getVertxConfig().getDefaultPort());
    }


    public void validator(RoutingContext routingContext) {
        // let's understand few things here
        // /services/:service/:path
        String service = routingContext.pathParam("service");
        String path = routingContext.pathParam("*");

        String requestId = routingContext.get("requestId");
        if (Strings.isNullOrEmpty(requestId)) {
            requestId = UUID.randomUUID().toString();
            routingContext.put("requestId", requestId);
        }

        // let's log the request for which service this request was targeted for and for what endpoint
        log.info("Received request on service: {}, for path: {}, with requestId: {}", service, path, requestId);
        // check if the service is even onboarded or visible to gateway?
        if (serverConfig.getServiceConfigs().containsKey(service)) {
            // we are just printing the info here
            // you can forward the request to the upstream by fetching the endpoint type and all
            // putting this information so that the upcoming handler have this information
            routingContext.put("service", service);
            routingContext.put("path", path);
            // move to the next handler
            routingContext.next();
        } else {
            // fail the request by saying the service is not present
            routingContext.response()
                    .end(new JsonObject()
                            .put("status", 404)
                            .put("error", String.format("Service '%s' not found", service))
                            .encode());
        }

    }

    public void forwarder(RoutingContext routingContext) {
        // this handler forwards the request to the upstream
        String requestUri = buildRequestUri(routingContext);

        log.info("Upstream Request URI: {}", requestUri);
        HttpRequest<Buffer> httpRequest = getHttpRequest(routingContext, requestUri);
        httpRequest.send(clientResponse -> handleResponse(routingContext, clientResponse));
    }

    private void handleResponse(RoutingContext routingContext, AsyncResult<HttpResponse<Buffer>> clientResponse) {
        // this method will be called when we receive a response from the client
        if (clientResponse.succeeded()) {
            HttpResponse<Buffer> httpResponse = clientResponse.result();
            routingContext.response().setStatusCode(200);
            routingContext.response().end(httpResponse.bodyAsString());
        } else {
            routingContext.response().setStatusCode(500);
            log.error("Error while handling response", clientResponse.cause());
        }
        routingContext.next();
    }

    public void endRouter(RoutingContext routingContext) {
        log.info("Ending Request");
    }


    public MicroserviceConfig getServiceConfig(RoutingContext routingContext) {
        String service = routingContext.pathParam("service");
        if (Strings.isNullOrEmpty(service)) {
            throw new IllegalArgumentException("Missing required parameter 'service'");
        }
        return serverConfig.getServiceConfigs().get(service);
    }

    public String buildRequestUri(RoutingContext routingContext) {

        MicroserviceConfig microserviceConfig = getServiceConfig(routingContext);
        // find the host and port for the service where the request needs to be forwarded
        // we are only covering the static endpoint part
        // in order to build up the complete request, we need to first get the host and port of the microservice and
        // then append the path to it
        // host:port/path
        if (Objects.requireNonNull(microserviceConfig.getEndpoint().getType()) == EndpointType.STATIC) {
            StaticEndpoint endpoint = (StaticEndpoint) microserviceConfig.getEndpoint();
            // http scheme + host + port + path
            return String.format("%s://%s:%s/%s", "http", endpoint.getHost(), endpoint.getPort(), routingContext.get("path"));
        }
        return "";
    }

    public HttpRequest<Buffer> getHttpRequest(RoutingContext routingContext, String requestUri) {
        HttpMethod method = routingContext.request().method();
        switch (String.valueOf(method)) {
            case "GET" -> {
                return webClient.getAbs(requestUri);
            }
            case "POST" -> {
                return webClient.postAbs(requestUri);
            }
            default -> throw new IllegalStateException("Unexpected value: " + method);
        }
    }


}
