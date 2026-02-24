package com.example.openapi;

import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper;
import software.amazon.smithy.openapi.fromsmithy.Smithy2OpenApiExtension;

import java.util.List;

/**
 * Extension that registers OpenAPI mappers for custom traits.
 */
public final class ErrorExampleExtension implements Smithy2OpenApiExtension {

    @Override
    public List<OpenApiMapper> getOpenApiMappers() {
        return List.of(new ConstMapper(), new MemberExampleMapper());
    }
}
