package com.example.traits;

import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.utils.BuilderRef;
import software.amazon.smithy.utils.SmithyBuilder;
import software.amazon.smithy.utils.ToSmithyBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Trait that defines examples for error structures that will be automatically
 * included in OpenAPI components/examples and referenced in error responses.
 */
public final class ErrorExampleTrait extends AbstractTrait implements ToSmithyBuilder<ErrorExampleTrait> {

    public static final ShapeId ID = ShapeId.from("com.example#errorExample");

    private final List<ErrorExampleEntry> examples;

    private ErrorExampleTrait(Builder builder) {
        super(ID, builder.getSourceLocation());
        this.examples = builder.examples.copy();
    }

    public List<ErrorExampleEntry> getExamples() {
        return examples;
    }

    @Override
    protected Node createNode() {
        ArrayNode.Builder arrayBuilder = ArrayNode.builder();
        for (ErrorExampleEntry entry : examples) {
            ObjectNode.Builder entryBuilder = ObjectNode.builder()
                    .withMember("title", entry.getTitle())
                    .withMember("content", entry.getContent());
            entry.getDocumentation().ifPresent(doc -> entryBuilder.withMember("documentation", doc));
            arrayBuilder.withValue(entryBuilder.build());
        }
        return arrayBuilder.build();
    }

    @Override
    public SmithyBuilder<ErrorExampleTrait> toBuilder() {
        Builder builder = builder().sourceLocation(getSourceLocation());
        examples.forEach(builder::addExample);
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractTraitBuilder<ErrorExampleTrait, Builder> {
        private final BuilderRef<List<ErrorExampleEntry>> examples = BuilderRef.forList();

        private Builder() {}

        public Builder addExample(ErrorExampleEntry example) {
            examples.get().add(example);
            return this;
        }

        public Builder examples(List<ErrorExampleEntry> examples) {
            this.examples.clear();
            this.examples.get().addAll(examples);
            return this;
        }

        @Override
        public ErrorExampleTrait build() {
            return new ErrorExampleTrait(this);
        }
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            Builder builder = builder().sourceLocation(value);
            ArrayNode arrayNode = value.expectArrayNode();

            for (Node node : arrayNode.getElements()) {
                ObjectNode entryNode = node.expectObjectNode();
                String title = entryNode.expectStringMember("title").getValue();
                ObjectNode content = entryNode.expectObjectMember("content");
                Optional<String> documentation = entryNode.getStringMember("documentation")
                        .map(n -> n.getValue());

                builder.addExample(new ErrorExampleEntry(title, documentation.orElse(null), content));
            }

            return builder.build();
        }
    }

    /**
     * Represents a single error example entry.
     */
    public static final class ErrorExampleEntry {
        private final String title;
        private final String documentation;
        private final ObjectNode content;

        public ErrorExampleEntry(String title, String documentation, ObjectNode content) {
            this.title = Objects.requireNonNull(title, "title must not be null");
            this.documentation = documentation;
            this.content = Objects.requireNonNull(content, "content must not be null");
        }

        public String getTitle() {
            return title;
        }

        public Optional<String> getDocumentation() {
            return Optional.ofNullable(documentation);
        }

        public ObjectNode getContent() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ErrorExampleEntry that = (ErrorExampleEntry) o;
            return title.equals(that.title) &&
                    Objects.equals(documentation, that.documentation) &&
                    content.equals(that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, documentation, content);
        }
    }
}
