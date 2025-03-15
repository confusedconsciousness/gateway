package org.example.model;

import lombok.Data;


@Data
public abstract class Endpoint {
    private final EndpointType type;

    public Endpoint(EndpointType type) {
        this.type = type;
    }
}
