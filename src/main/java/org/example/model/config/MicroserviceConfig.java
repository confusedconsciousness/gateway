package org.example.model.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.endpoint.Endpoint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MicroserviceConfig {
    private Endpoint endpoint; // how does this service wants to be discovered (static or service discovery)
}
