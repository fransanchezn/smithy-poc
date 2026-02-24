package com.example.traits;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.Trait;

/**
 * Trait that indicates a member has a constant value that cannot change.
 * The value provided in the annotation will be used as the OpenAPI "const" value.
 */
public final class ConstTrait extends AbstractTrait {

    public static final ShapeId ID = ShapeId.from("com.example#const");

    private final Node value;

    public ConstTrait(Node value) {
        super(ID, value.getSourceLocation());
        this.value = value;
    }

    /**
     * Gets the constant value.
     */
    public Node getValue() {
        return value;
    }

    @Override
    protected Node createNode() {
        return value;
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            return new ConstTrait(value);
        }
    }
}
