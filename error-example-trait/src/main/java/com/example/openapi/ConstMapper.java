package com.example.openapi;

import com.example.traits.ConstTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.openapi.fromsmithy.Context;
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper;
import software.amazon.smithy.openapi.model.OpenApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * OpenAPI mapper that transforms "default" to "const" for members annotated with @const trait.
 *
 * When a structure member has both a default value and the @const trait, this mapper
 * replaces the OpenAPI "default" property with "const" in the generated schema.
 */
public final class ConstMapper implements OpenApiMapper {

    private static final Logger LOGGER = Logger.getLogger(ConstMapper.class.getName());

    @Override
    public byte getOrder() {
        // Run early to transform default -> const before other mappers process the schema
        return 50;
    }

    @Override
    public ObjectNode updateNode(Context<? extends Trait> context,
                                 OpenApi openapi,
                                 ObjectNode node) {
        Model model = context.getModel();

        // Collect all member shapes with @const trait
        Set<String> constMembers = new HashSet<>();
        for (StructureShape structure : model.getStructureShapes()) {
            for (MemberShape member : structure.getAllMembers().values()) {
                if (member.hasTrait(ConstTrait.class)) {
                    // Store the member name as it appears in the parent structure
                    constMembers.add(structure.getId().getName() + "." + member.getMemberName());
                    LOGGER.fine("Found @const member: " + structure.getId().getName() + "." + member.getMemberName());
                }
            }
        }

        if (constMembers.isEmpty()) {
            return node;
        }

        // Process components/schemas to transform default -> const for @const members
        ObjectNode components = node.getObjectMember("components").orElse(ObjectNode.builder().build());
        ObjectNode schemas = components.getObjectMember("schemas").orElse(ObjectNode.builder().build());

        ObjectNode.Builder updatedSchemas = ObjectNode.builder();
        for (Map.Entry<String, Node> schemaEntry : schemas.getStringMap().entrySet()) {
            String schemaName = schemaEntry.getKey();
            ObjectNode schema = schemaEntry.getValue().expectObjectNode();

            // Process properties of this schema
            schema = processSchemaProperties(schemaName, schema, constMembers);

            updatedSchemas.withMember(schemaName, schema);
        }

        components = components.withMember("schemas", updatedSchemas.build());
        return node.withMember("components", components);
    }

    private ObjectNode processSchemaProperties(String schemaName, ObjectNode schema, Set<String> constMembers) {
        ObjectNode properties = schema.getObjectMember("properties").orElse(null);
        if (properties == null) {
            return schema;
        }

        // Get the base schema name (strip common suffixes like ResponseContent)
        String baseSchemaName = getBaseSchemaName(schemaName);

        ObjectNode.Builder updatedProperties = ObjectNode.builder();
        boolean hasChanges = false;

        for (Map.Entry<String, Node> propEntry : properties.getStringMap().entrySet()) {
            String propName = propEntry.getKey();
            ObjectNode propSchema = propEntry.getValue().expectObjectNode();

            // Try both the exact schema name and the base schema name
            String memberKey = schemaName + "." + propName;
            String baseMemberKey = baseSchemaName + "." + propName;

            boolean shouldConvert = (constMembers.contains(memberKey) || constMembers.contains(baseMemberKey))
                    && propSchema.getMember("default").isPresent();

            if (shouldConvert) {
                // Transform default to const
                Node defaultValue = propSchema.getMember("default").get();
                ObjectNode.Builder updatedProp = ObjectNode.builder();

                for (Map.Entry<String, Node> field : propSchema.getStringMap().entrySet()) {
                    if (!"default".equals(field.getKey())) {
                        updatedProp.withMember(field.getKey(), field.getValue());
                    }
                }
                updatedProp.withMember("const", defaultValue);

                LOGGER.fine("Transformed default to const for " + memberKey + ": " + defaultValue);
                updatedProperties.withMember(propName, updatedProp.build());
                hasChanges = true;
            } else {
                updatedProperties.withMember(propName, propSchema);
            }
        }

        if (hasChanges) {
            return schema.withMember("properties", updatedProperties.build());
        }
        return schema;
    }

    /**
     * Strips common suffixes from schema names to find the base Smithy shape name.
     * Smithy's OpenAPI conversion creates synthetic schemas with suffixes like
     * "ResponseContent" for response bodies.
     */
    private String getBaseSchemaName(String schemaName) {
        // Common suffixes added by Smithy's OpenAPI conversion
        String[] suffixes = {"ResponseContent", "RequestContent"};

        for (String suffix : suffixes) {
            if (schemaName.endsWith(suffix)) {
                return schemaName.substring(0, schemaName.length() - suffix.length());
            }
        }
        return schemaName;
    }
}
