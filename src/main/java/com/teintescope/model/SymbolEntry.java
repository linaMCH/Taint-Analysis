package com.teintescope.model;

public record SymbolEntry(String name, String type, String scope, int line, TaintState state) {}