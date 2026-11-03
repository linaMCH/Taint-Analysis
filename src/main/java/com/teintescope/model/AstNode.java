package com.teintescope.model;

import java.util.List;

public record AstNode(String kind, String label, List<AstNode> children) {
    public static AstNode of(String kind, String label, AstNode... children) {
        return new AstNode(kind, label, List.of(children));
    }
}