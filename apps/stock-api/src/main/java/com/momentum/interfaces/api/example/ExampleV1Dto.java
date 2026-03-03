package com.momentum.interfaces.api.example;

import com.momentum.application.example.ExampleInfo;

public class ExampleV1Dto {
    public record ExampleResponse(Long id, String name, String description) {
        public static ExampleResponse from(ExampleInfo info) {
            return new ExampleResponse(
                info.id(),
                info.name(),
                info.description()
            );
        }
    }
}
