package org.example.model.endpoint;

import lombok.*;

@Data
@Builder
public class StaticEndpoint extends Endpoint {

    private String host;
    private int port;

    public StaticEndpoint(String host, int port) {
        super(EndpointType.STATIC);
        this.host = host;
        this.port = port;
    }
}
