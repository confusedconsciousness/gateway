package org.example.model.endpoint;

public enum EndpointType {
    STATIC, // this only requires, host and port for e.g., vhost:8080
    // we can support other as well such as service dns based discovery
    // ...
    DNS
}
