package org.example.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MicroserviceConfig {
    private Endpoint endpoint; // how does this service wants to be discovered (static or service discovery)
}
