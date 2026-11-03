package com.teintescope.model;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Toutes les statistiques sont calculées à partir des données : rien n'est figé dans l'interface. */
public record ReportData(String project, LocalDateTime date, double seconds,
                         int files, int lines, int taintedFlows, int safeFlows,
                         List<Vulnerability> vulns) {

    public Map<Severity, Integer> bySeverity() {
        Map<Severity, Integer> m = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) m.put(s, 0);
        vulns.forEach(v -> m.merge(v.severity(), 1, Integer::sum));
        return m;
    }

    public Map<VulnCategory, Integer> byCategory() {
        Map<VulnCategory, Integer> m = new EnumMap<>(VulnCategory.class);
        for (VulnCategory c : VulnCategory.values()) m.put(c, 0);
        vulns.forEach(v -> m.merge(v.category(), 1, Integer::sum));
        return m;
    }
}