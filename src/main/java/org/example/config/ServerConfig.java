package org.example.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.MicroserviceConfig;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerConfig {

    private VertxConfig vertxConfig;
    // all those services needs to be visible to the gateway
    // name of the service and its configuration
    private Map<String, MicroserviceConfig> serviceConfigs;
}
