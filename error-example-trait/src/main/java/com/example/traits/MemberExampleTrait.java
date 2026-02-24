package com.example.traits;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.Trait;

/**
 * Trait that defines an example value for a structure member.
 * Used to generate OpenAPI examples at the component level.
 * If the member also has @const, the const value takes precedence.
 */
public final class MemberExampleTrait extends AbstractTrait {

    public static final ShapeId ID = ShapeId.from("com.example#memberExample");

    private final Node value;

    public MemberExampleTrait(Node value) {
        super(ID, value.getSourceLocation());
        this.value = value;
    }

    /**
     * Gets the example value.
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
            return new MemberExampleTrait(value);
        }
    }
}
