package com.example.openapi;

import com.example.traits.ErrorExampleTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.HttpBinding;
import software.amazon.smithy.model.knowledge.HttpBindingIndex;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.traits.ErrorTrait;
import software.amazon.smithy.model.traits.HttpErrorTrait;
import software.amazon.smithy.openapi.fromsmithy.Context;
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper;
import software.amazon.smithy.openapi.model.OpenApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * OpenAPI mapper that generates /components/examples entries from @errorExample traits
 * and references them in error responses.
 */
public final class ErrorExampleMapper implements OpenApiMapper {

    private static final Logger LOGGER = Logger.getLogger(ErrorExampleMapper.class.getName());

    @Override
    public byte getOrder() {
        // Run before RemoveUnusedComponents (order 64)
        return 60;
    }

    @Override
    public ObjectNode updateNode(Context<? extends software.amazon.smithy.model.traits.Trait> context,
                                 OpenApi openapi,
                                 ObjectNode node) {
        Model model = context.getModel();
        ServiceShape service = context.getService();

        // Collect all error shapes with @errorExample trait
        Map<ShapeId, ErrorExampleTrait> errorExamples = new HashMap<>();
        for (StructureShape shape : model.getStructureShapes()) {
            if (shape.hasTrait(ErrorTrait.class) && shape.hasTrait(ErrorExampleTrait.class)) {
                errorExamples.put(shape.getId(), shape.expectTrait(ErrorExampleTrait.class));
            }
        }

        if (errorExamples.isEmpty()) {
            return node;
        }

        LOGGER.fine("Found " + errorExamples.size() + " error shapes with @errorExample trait");

        // Build the examples to add to components/examples
        ObjectNode.Builder examplesBuilder = ObjectNode.builder();
        for (Map.Entry<ShapeId, ErrorExampleTrait> entry : errorExamples.entrySet()) {
            String errorName = entry.getKey().getName();
            ErrorExampleTrait trait = entry.getValue();

            for (int i = 0; i < trait.getExamples().size(); i++) {
                ErrorExampleTrait.ErrorExampleEntry example = trait.getExamples().get(i);
                String exampleName = errorName + "Example" + (trait.getExamples().size() > 1 ? (i + 1) : "");

                ObjectNode.Builder exampleNode = ObjectNode.builder()
                        .withMember("summary", example.getTitle())
                        .withMember("value", example.getContent());

                example.getDocumentation().ifPresent(doc ->
                        exampleNode.withMember("description", doc));

                examplesBuilder.withMember(exampleName, exampleNode.build());
            }
        }

        // Get existing components or create new one
        ObjectNode components = node.getObjectMember("components").orElse(ObjectNode.builder().build());

        // Merge existing examples with new ones
        ObjectNode existingExamples = components.getObjectMember("examples").orElse(ObjectNode.builder().build());
        ObjectNode newExamples = examplesBuilder.build();
        ObjectNode mergedExamples = existingExamples.merge(newExamples);

        // Update components with merged examples
        components = components.withMember("examples", mergedExamples);
        node = node.withMember("components", components);

        // Now update paths to reference the examples
        node = updatePathsWithExampleReferences(context, model, service, node, errorExamples);

        return node;
    }

    private ObjectNode updatePathsWithExampleReferences(
            Context<? extends software.amazon.smithy.model.traits.Trait> context,
            Model model,
            ServiceShape service,
            ObjectNode node,
            Map<ShapeId, ErrorExampleTrait> errorExamples) {

        ObjectNode paths = node.getObjectMember("paths").orElse(ObjectNode.builder().build());
        ObjectNode.Builder updatedPaths = ObjectNode.builder();

        for (Map.Entry<String, Node> pathEntry : paths.getStringMap().entrySet()) {
            String path = pathEntry.getKey();
            ObjectNode pathItem = pathEntry.getValue().expectObjectNode();
            ObjectNode.Builder updatedPathItem = ObjectNode.builder();

            for (Map.Entry<String, Node> methodEntry : pathItem.getStringMap().entrySet()) {
                String method = methodEntry.getKey();
                ObjectNode operation = methodEntry.getValue().expectObjectNode();

                // Find which Smithy operation this corresponds to
                Optional<OperationShape> smithyOperation = findOperationForPath(context, model, service, path, method);

                if (smithyOperation.isPresent()) {
                    operation = updateOperationResponses(model, operation, smithyOperation.get(), errorExamples);
                }

                updatedPathItem.withMember(method, operation);
            }

            updatedPaths.withMember(path, updatedPathItem.build());
        }

        return node.withMember("paths", updatedPaths.build());
    }

    private Optional<OperationShape> findOperationForPath(
            Context<? extends software.amazon.smithy.model.traits.Trait> context,
            Model model,
            ServiceShape service,
            String path,
            String method) {

        // Get all operations from the service
        for (ShapeId operationId : service.getAllOperations()) {
            Optional<OperationShape> opShape = model.getShape(operationId)
                    .flatMap(Shape::asOperationShape);

            if (opShape.isPresent()) {
                OperationShape operation = opShape.get();

                // Check if this operation matches the path and method
                Optional<software.amazon.smithy.model.traits.HttpTrait> httpTrait =
                        operation.getTrait(software.amazon.smithy.model.traits.HttpTrait.class);

                if (httpTrait.isPresent()) {
                    String opMethod = httpTrait.get().getMethod().toLowerCase();
                    String opUri = httpTrait.get().getUri().toString();

                    // Convert Smithy URI pattern to OpenAPI path format for comparison
                    String normalizedOpUri = normalizeUri(opUri);
                    String normalizedPath = normalizePath(path);

                    if (opMethod.equals(method.toLowerCase()) && normalizedOpUri.equals(normalizedPath)) {
                        return Optional.of(operation);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private String normalizeUri(String uri) {
        // Convert Smithy URI pattern {param} to OpenAPI {param} (they're the same)
        // Remove query string parameters for comparison
        int queryIndex = uri.indexOf('?');
        if (queryIndex >= 0) {
            uri = uri.substring(0, queryIndex);
        }
        return uri;
    }

    private String normalizePath(String path) {
        return path;
    }

    private ObjectNode updateOperationResponses(
            Model model,
            ObjectNode operation,
            OperationShape smithyOperation,
            Map<ShapeId, ErrorExampleTrait> errorExamples) {

        // Get operation errors
        Set<ShapeId> operationErrors = new HashSet<>(smithyOperation.getErrors());

        if (operationErrors.isEmpty()) {
            return operation;
        }

        ObjectNode responses = operation.getObjectMember("responses").orElse(ObjectNode.builder().build());
        ObjectNode.Builder updatedResponses = ObjectNode.builder();

        // Group errors by status code
        // Note: When onErrorStatusConflict=oneOf, Smithy creates synthetic wrapper shapes
        // We need to find the actual original errors
        Map<Integer, Set<ShapeId>> errorsByStatusCode = new HashMap<>();
        for (ShapeId errorId : operationErrors) {
            Optional<StructureShape> errorShape = model.getShape(errorId).flatMap(Shape::asStructureShape);
            if (errorShape.isPresent()) {
                int statusCode = errorShape.get().getTrait(HttpErrorTrait.class)
                        .map(HttpErrorTrait::getCode)
                        .orElse(getDefaultStatusCode(errorShape.get()));

                errorsByStatusCode.computeIfAbsent(statusCode, k -> new HashSet<>()).add(errorId);
            }
        }

        // Also add the original error shapes that have @errorExample and match the status code
        for (Map.Entry<ShapeId, ErrorExampleTrait> entry : errorExamples.entrySet()) {
            ShapeId errorId = entry.getKey();
            Optional<StructureShape> errorShape = model.getShape(errorId).flatMap(Shape::asStructureShape);
            if (errorShape.isPresent()) {
                int statusCode = errorShape.get().getTrait(HttpErrorTrait.class)
                        .map(HttpErrorTrait::getCode)
                        .orElse(getDefaultStatusCode(errorShape.get()));

                // Check if this error is used by this operation (directly or via a synthetic wrapper)
                // by checking if the operation's list of errors contains an error with the same status code
                if (errorsByStatusCode.containsKey(statusCode)) {
                    errorsByStatusCode.get(statusCode).add(errorId);
                }
            }
        }

        for (Map.Entry<String, Node> responseEntry : responses.getStringMap().entrySet()) {
            String statusCode = responseEntry.getKey();
            ObjectNode response = responseEntry.getValue().expectObjectNode();

            // Check if this is an error response code
            int code;
            try {
                code = Integer.parseInt(statusCode);
            } catch (NumberFormatException e) {
                updatedResponses.withMember(statusCode, response);
                continue;
            }

            // Find errors that match this status code and have examples
            Set<ShapeId> errorsForCode = errorsByStatusCode.getOrDefault(code, new HashSet<>());
            ObjectNode.Builder examplesForResponse = ObjectNode.builder();

            for (ShapeId errorId : errorsForCode) {
                if (errorExamples.containsKey(errorId)) {
                    ErrorExampleTrait trait = errorExamples.get(errorId);
                    String errorName = errorId.getName();

                    for (int i = 0; i < trait.getExamples().size(); i++) {
                        String exampleName = errorName + "Example" + (trait.getExamples().size() > 1 ? (i + 1) : "");
                        String refKey = errorName + (trait.getExamples().size() > 1 ? String.valueOf(i + 1) : "");

                        ObjectNode ref = ObjectNode.builder()
                                .withMember("$ref", "#/components/examples/" + exampleName)
                                .build();

                        examplesForResponse.withMember(refKey, ref);
                    }
                }
            }

            ObjectNode examplesNode = examplesForResponse.build();
            if (!examplesNode.isEmpty()) {
                // Update the response's content to include examples
                response = updateResponseContent(response, examplesNode);
            }

            updatedResponses.withMember(statusCode, response);
        }

        return operation.withMember("responses", updatedResponses.build());
    }

    private int getDefaultStatusCode(StructureShape errorShape) {
        // Default status codes based on error type
        Optional<ErrorTrait> errorTrait = errorShape.getTrait(ErrorTrait.class);
        if (errorTrait.isPresent()) {
            if (errorTrait.get().isClientError()) {
                return 400;
            } else if (errorTrait.get().isServerError()) {
                return 500;
            }
        }
        return 500;
    }

    private ObjectNode updateResponseContent(ObjectNode response, ObjectNode examples) {
        ObjectNode content = response.getObjectMember("content").orElse(ObjectNode.builder().build());
        ObjectNode.Builder updatedContent = ObjectNode.builder();

        for (Map.Entry<String, Node> contentEntry : content.getStringMap().entrySet()) {
            String mediaType = contentEntry.getKey();
            ObjectNode mediaTypeObject = contentEntry.getValue().expectObjectNode();

            // Add or merge examples into the media type object
            ObjectNode existingExamples = mediaTypeObject.getObjectMember("examples")
                    .orElse(ObjectNode.builder().build());
            ObjectNode mergedExamples = existingExamples.merge(examples);

            mediaTypeObject = mediaTypeObject.withMember("examples", mergedExamples);
            updatedContent.withMember(mediaType, mediaTypeObject);
        }

        return response.withMember("content", updatedContent.build());
    }
}
