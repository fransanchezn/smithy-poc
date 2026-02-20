package com.example.traits;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.utils.SmithyBuilder;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Marker trait that indicates a member has a constant value that cannot change.
 * When applied to a member with a default value, generates OpenAPI "const" instead of "default".
 */
public final class ConstTrait extends AbstractTrait implements ToSmithyBuilder<ConstTrait> {

    public static final ShapeId ID = ShapeId.from("com.example#const");

    private ConstTrait(Builder builder) {
        super(ID, builder.getSourceLocation());
    }

    @Override
    protected Node createNode() {
        return ObjectNode.builder().build();
    }

    @Override
    public SmithyBuilder<ConstTrait> toBuilder() {
        return builder().sourceLocation(getSourceLocation());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractTraitBuilder<ConstTrait, Builder> {
        private Builder() {}

        @Override
        public ConstTrait build() {
            return new ConstTrait(this);
        }
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            return builder().sourceLocation(value).build();
        }
    }
}
