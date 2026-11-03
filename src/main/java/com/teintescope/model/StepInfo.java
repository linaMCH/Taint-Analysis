package com.teintescope.model;

/** Une étape du pipeline (barre sous l'en-tête). */
public record StepInfo(String title, String info, StepStatus status) {}