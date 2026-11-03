package com.teintescope.model;

/** Une étape du chemin de teinte. line == 0 signifie « non rencontrée » (ex. aucun assainisseur). */
public record TraceStep(StepKind kind, int line, String code, String explanation, TaintState state) {}