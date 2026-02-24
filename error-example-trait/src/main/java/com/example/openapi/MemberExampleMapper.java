package com.example.openapi;

import com.example.traits.ConstTrait;
import com.example.traits.MemberExampleTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.shapes.UnionShape;
import software.amazon.smithy.model.traits.ErrorTrait;
import software.amazon.smithy.model.traits.HttpErrorTrait;
import software.amazon.smithy.model.traits.HttpTrait;
import software.amazon.smithy.model.traits.Trait;
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
 * OpenAPI mapper that generates /components/examples entries from @memberExample and @const traits
 * on error structure members, and references them in error responses.
 */
public final class MemberExampleMapper implements OpenApiMapper {

    private static final Logger LOGGER = Logger.getLogger(MemberExampleMapper.class.getName());

    @Override
    public byte getOrder() {
        // Run after ConstMapper (50) and before RemoveUnusedComponents (64)
        return 61;
    }

    @Override
    public ObjectNode updateNode(Context<? extends Trait> context,
                                 OpenApi openapi,
                                 ObjectNode node) {
        Model model = context.getModel();
        ServiceShape service = context.getService();

        // Collect all error shapes and build examples from their members
        Map<ShapeId, ObjectNode> errorExamples = new HashMap<>();
        for (StructureShape shape : model.getStructureShapes()) {
            if (shape.hasTrait(ErrorTrait.class)) {
                Optional<Node> example = buildExampleFromMembers(model, shape);
                if (example.isPresent()) {
                    errorExamples.put(shape.getId(), example.get().expectObjectNode());
                }
            }
        }

        if (errorExamples.isEmpty()) {
            return node;
        }

        LOGGER.fine("Found " + errorExamples.size() + " error shapes with member examples");

        // Build the examples to add to components/examples
        ObjectNode.Builder examplesBuilder = ObjectNode.builder();
        for (Map.Entry<ShapeId, ObjectNode> entry : errorExamples.entrySet()) {
            String errorName = entry.getKey().getName();
            ObjectNode exampleNode = ObjectNode.builder()
                    .withMember("summary", errorName)
                    .withMember("value", entry.getValue())
                    .build();

            examplesBuilder.withMember(errorName, exampleNode);
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

    /**
     * Recursively builds an example object from @const and @memberExample traits on structure members.
     */
    private Optional<Node> buildExampleFromMembers(Model model, StructureShape shape) {
        ObjectNode.Builder builder = ObjectNode.builder();
        boolean hasAnyExample = false;

        for (MemberShape member : shape.getAllMembers().values()) {
            String memberName = member.getMemberName();
            Shape targetShape = model.expectShape(member.getTarget());

            // Priority: @const > @memberExample > recurse for structures/lists
            if (member.hasTrait(ConstTrait.class)) {
                builder.withMember(memberName, member.expectTrait(ConstTrait.class).getValue());
                hasAnyExample = true;
            } else if (member.hasTrait(MemberExampleTrait.class)) {
                builder.withMember(memberName, member.expectTrait(MemberExampleTrait.class).getValue());
                hasAnyExample = true;
            } else if (targetShape.isStructureShape()) {
                // Recurse into nested structure
                Optional<Node> nested = buildExampleFromMembers(model, targetShape.asStructureShape().get());
                if (nested.isPresent()) {
                    builder.withMember(memberName, nested.get());
                    hasAnyExample = true;
                }
            } else if (targetShape.isListShape()) {
                // Handle lists - build example for member type
                Optional<Node> listExample = buildListExample(model, targetShape.asListShape().get());
                if (listExample.isPresent()) {
                    builder.withMember(memberName, listExample.get());
                    hasAnyExample = true;
                }
            }
        }

        return hasAnyExample ? Optional.of(builder.build()) : Optional.empty();
    }

    /**
     * Builds an example array for a list shape by creating an example of its member type.
     */
    private Optional<Node> buildListExample(Model model, ListShape listShape) {
        Shape memberTarget = model.expectShape(listShape.getMember().getTarget());
        if (memberTarget.isStructureShape()) {
            Optional<Node> itemExample = buildExampleFromMembers(model, memberTarget.asStructureShape().get());
            if (itemExample.isPresent()) {
                return Optional.of(ArrayNode.fromNodes(itemExample.get()));
            }
        }
        return Optional.empty();
    }

    private ObjectNode updatePathsWithExampleReferences(
            Context<? extends Trait> context,
            Model model,
            ServiceShape service,
            ObjectNode node,
            Map<ShapeId, ObjectNode> errorExamples) {

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
            Context<? extends Trait> context,
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
                Optional<HttpTrait> httpTrait = operation.getTrait(HttpTrait.class);

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
            Map<ShapeId, ObjectNode> errorExamples) {

        // Get operation errors
        Set<ShapeId> operationErrors = new HashSet<>(smithyOperation.getErrors());

        LOGGER.fine("Processing operation: " + smithyOperation.getId().getName() + " with errors: " + operationErrors);

        if (operationErrors.isEmpty()) {
            return operation;
        }

        ObjectNode responses = operation.getObjectMember("responses").orElse(ObjectNode.builder().build());
        ObjectNode.Builder updatedResponses = ObjectNode.builder();

        // Group errors by status code (only errors declared on this operation)
        // Handle synthesized union errors by resolving their members
        Map<Integer, Set<ShapeId>> errorsByStatusCode = new HashMap<>();
        for (ShapeId errorId : operationErrors) {
            Shape shape = model.expectShape(errorId);
            LOGGER.fine("  Error shape type for " + errorId.getName() + ": " + shape.getType());

            if (shape.isStructureShape()) {
                StructureShape errorShape = shape.asStructureShape().get();

                // Check if this is a synthesized error union wrapper (has single "errorUnion" member)
                Optional<MemberShape> errorUnionMember = errorShape.getMember("errorUnion");
                if (errorUnionMember.isPresent()) {
                    // This is a synthesized error wrapper - resolve the union members
                    Shape unionTarget = model.expectShape(errorUnionMember.get().getTarget());
                    if (unionTarget.isUnionShape()) {
                        int statusCode = errorShape.getTrait(HttpErrorTrait.class)
                                .map(HttpErrorTrait::getCode)
                                .orElse(getDefaultStatusCode(errorShape));
                        LOGGER.fine("  Found synthesized error wrapper: " + errorId.getName() + " with status " + statusCode);

                        for (MemberShape member : unionTarget.asUnionShape().get().getAllMembers().values()) {
                            ShapeId targetId = member.getTarget();
                            LOGGER.fine("    Union member: " + targetId.getName());
                            // Add the original error shape, which should have examples
                            errorsByStatusCode.computeIfAbsent(statusCode, k -> new HashSet<>()).add(targetId);
                        }
                    }
                } else {
                    // Regular error structure
                    int statusCode = errorShape.getTrait(HttpErrorTrait.class)
                            .map(HttpErrorTrait::getCode)
                            .orElse(getDefaultStatusCode(errorShape));

                    LOGGER.fine("  Error " + errorId.getName() + " has status code " + statusCode);
                    errorsByStatusCode.computeIfAbsent(statusCode, k -> new HashSet<>()).add(errorId);
                }
            } else if (shape.isUnionShape()) {
                // Synthesized union error - resolve its members
                LOGGER.fine("  Found union error: " + errorId.getName());
                for (MemberShape member : shape.asUnionShape().get().getAllMembers().values()) {
                    ShapeId targetId = member.getTarget();
                    Optional<StructureShape> targetShape = model.getShape(targetId).flatMap(Shape::asStructureShape);
                    if (targetShape.isPresent() && targetShape.get().hasTrait(ErrorTrait.class)) {
                        int statusCode = targetShape.get().getTrait(HttpErrorTrait.class)
                                .map(HttpErrorTrait::getCode)
                                .orElse(getDefaultStatusCode(targetShape.get()));
                        LOGGER.fine("    Union member " + targetId.getName() + " has status code " + statusCode);
                        errorsByStatusCode.computeIfAbsent(statusCode, k -> new HashSet<>()).add(targetId);
                    }
                }
            }
        }

        LOGGER.fine("  errorsByStatusCode: " + errorsByStatusCode);
        LOGGER.fine("  errorExamples keys: " + errorExamples.keySet());

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
            LOGGER.fine("  For status " + code + ", errorsForCode: " + errorsForCode);
            ObjectNode.Builder examplesForResponse = ObjectNode.builder();

            for (ShapeId errorId : errorsForCode) {
                LOGGER.fine("    Checking errorId: " + errorId + ", containsKey: " + errorExamples.containsKey(errorId));
                if (errorExamples.containsKey(errorId)) {
                    String errorName = errorId.getName();

                    ObjectNode ref = ObjectNode.builder()
                            .withMember("$ref", "#/components/examples/" + errorName)
                            .build();

                    examplesForResponse.withMember(errorName, ref);
                }
            }

            ObjectNode examplesNode = examplesForResponse.build();
            LOGGER.fine("  examplesNode for status " + code + ": " + examplesNode);
            // Always update if there are examples, regardless of oneOf schema
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
