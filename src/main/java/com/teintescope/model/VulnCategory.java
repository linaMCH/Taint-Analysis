package com.teintescope.model;

/** Les 7 familles de vulnérabilités gérées par le moteur commun de propagation de teinte. */
public enum VulnCategory {
    SQLI("SQL Injection", "SQL", VulnFamily.SQL, Maturity.AVAILABLE),
    NOSQLI("NoSQL Injection", "NoSQL", VulnFamily.NON_SQL, Maturity.PLANNED),
    XSS("Cross-Site Scripting", "XSS", VulnFamily.XSS, Maturity.IN_PROGRESS),
    CMDI("OS Command Injection", "CMD", VulnFamily.COMMAND, Maturity.PLANNED),
    LDAPI("LDAP Injection", "LDAP", VulnFamily.NON_SQL, Maturity.PLANNED),
    XPATHI("XPath Injection", "XPath", VulnFamily.NON_SQL, Maturity.PLANNED),
    PATH("Path Traversal", "Path", VulnFamily.OTHER, Maturity.PLANNED);

    private final String label, shortName;
    private final VulnFamily family;
    private final Maturity maturity;

    VulnCategory(String label, String shortName, VulnFamily family, Maturity maturity) {
        this.label = label; this.shortName = shortName; this.family = family; this.maturity = maturity;
    }
    public String label() { return label; }
    public String shortName() { return shortName; }
    public VulnFamily family() { return family; }
    public Maturity maturity() { return maturity; }
}