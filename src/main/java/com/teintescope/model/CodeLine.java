package com.teintescope.model;

import java.util.List;

public record CodeLine(int number, LineKind kind, List<CodeSegment> segments) {}
