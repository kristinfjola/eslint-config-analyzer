package parser;

public enum RuleCategory {
    POSSIBLE_ERRORS(31, "Possible Errors"),
    BEST_PRACTICES(69, "Best Practices"),
    STRICT(1, "Strict Mode"),
    VARIABLES(12, "Variables"),
    NODE_COMMON(10, "Node.js and CommonJS"),
    STYLISTIC(81, "Stylistic Issues"),
    ES6(32, "ES6");

    private int numberOfRules;
    private String name;

    RuleCategory(int numRules, String ruleName) {
        this.numberOfRules = numRules;
        this.name = ruleName;
    }

    public int getNumberOfRules() {
        return numberOfRules;
    }

    public String getName() {
        return name;
    }
}
