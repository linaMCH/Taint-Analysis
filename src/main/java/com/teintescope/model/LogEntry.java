package com.teintescope.model;

/** level : INFO, WARN ou ERROR. phase : LEXICAL, SYNTAXE, SÉMANTIQUE… */
public record LogEntry(String time, String phase, String level, String message) {}