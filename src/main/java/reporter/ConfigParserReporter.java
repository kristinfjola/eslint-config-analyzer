package reporter;

import parser.ParserUtils;
import parser.RuleCategory;
import parser.ConfigFileParser;
import java.util.*;

public class ConfigParserReporter extends Reporter {

    private ConfigFileParser configParser;
    private int usedPreset;

    public ConfigParserReporter(ConfigFileParser cp) {
        this.configParser = cp;
        this.usedPreset = this.configParser.projectsWithPreset.size();
    }

    @Override
    public void report() {
        System.out.println("");
        System.out.println(" ---PARSING CONFIG FILES ---");
        System.out.println("Total configs analyzed: " + configParser.numAnalyzed);
        System.out.println("");
        reportPresets();
        reportPlugins();
        reportRules();
    }

    public void reportPresets() {
        System.out.println("- PRESETS -");
        System.out.println("Projects that used a preset: " + usedPreset + " - " + getPercentage(usedPreset, configParser.numAnalyzed));
        System.out.println("Don't use a preset: " + (configParser.numAnalyzed - usedPreset));
        System.out.println("All presets with count: ");
        int sum = 0;
        for(Map.Entry<String, Integer> entry : configParser.presets.entrySet()) {
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
            sum += entry.getValue();
        }
        System.out.println("Number of presets used in total: " + sum);
        System.out.println("");
        reportRulesAddedWithPreset();
    }

    public void reportRulesAddedWithPreset() {
        int countAddRule = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneRule.values()) {
            if(usesPreset) countAddRule++;
        }
        int countAddError = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneError.values()) {
            if(usesPreset) countAddError++;
        }
        int countAddWarning = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneWarning.values()) {
            if(usesPreset) countAddWarning++;
        }
        int countAddDisable = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneDisable.values()) {
            if(usesPreset) countAddDisable++;
        }

        System.out.println("Projects that use a preset and have at least one rule: " + countAddRule + " - "
                + getPercentage(countAddRule, usedPreset) + " of projects that use presets");
        int totalRulesWithPreset = configParser.totalErrorsWithPreset + configParser.totalWarningsWithPreset + configParser.totalDisabledWithPreset;
        System.out.println("\tTotal rules used by all projects: " + totalRulesWithPreset);
        System.out.println("\tAverage number of rules used per project: "
                + getAverage(totalRulesWithPreset, usedPreset));

        System.out.println("Projects that use a preset and have at least one rule as error: " + countAddError + " - "
                + getPercentage(countAddError, usedPreset) + " of projects that use presets");
        System.out.println("\tTotal errors used by all projects: " + configParser.totalErrorsWithPreset);
        System.out.println("\tAverage number of errors used per project: "
                + getAverage(configParser.totalErrorsWithPreset, usedPreset));

        System.out.println("Projects that use a preset and have at least one rule as warning: " + countAddWarning + " - "
                + getPercentage(countAddWarning, usedPreset) + " of projects that use presets");
        System.out.println("\tTotal warnings used by all projects: " + configParser.totalWarningsWithPreset);
        System.out.println("\tAverage number of warnings used per project: "
                + getAverage(configParser.totalWarningsWithPreset, usedPreset));

        System.out.println("Projects that use a preset and have at least one rule as off: " + countAddDisable + " - "
                + getPercentage(countAddDisable, usedPreset) + " of projects that use presets");
        System.out.println("\tTotal disables used by all projects: " + configParser.totalDisabledWithPreset);
        System.out.println("\tAverage number of disables used per project: "
                + getAverage(configParser.totalDisabledWithPreset, usedPreset));

        System.out.println("");
    }

    public void reportPlugins() {
        // plugins
        int usedPlugin = configParser.projectsWithPlugins.size();
        System.out.println("- PLUGINS -");
        System.out.println("Projects that used a plugin: " + usedPlugin + " - "
                + getPercentage(usedPlugin, configParser.numAnalyzed));
        System.out.println("Don't use a plugin: " + (configParser.numAnalyzed - usedPlugin));
        System.out.println("All plugins with count: ");
        int sum = 0;
        for(Map.Entry<String, Integer> entry : configParser.plugins.entrySet()) {
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
            sum += entry.getValue();
        }
        System.out.println("Number of plugins used in total: " + sum);
        System.out.println("");


        // plugins as presets
        System.out.println("- PLUGINS AS PRESETS -");
        int usedPluginAsPreset = configParser.projectsWithPluginsAsPresets.size();
        System.out.println("Projects that used a plugin as preset: " + usedPluginAsPreset + " - "
                + getPercentage(usedPluginAsPreset, configParser.numAnalyzed));
        System.out.println("Don't use a plugin as preset: " + (configParser.numAnalyzed - usedPluginAsPreset));
        System.out.println("All plugin as preset with count: ");
        sum = 0;
        for(Map.Entry<String, Integer> entry : configParser.pluginsAsPresets.entrySet()) {
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
            sum += entry.getValue();
        }
        System.out.println("Number of plugins as presets used in total: " + sum);
        System.out.println("");
    }

    public void reportRules() {
        System.out.println("- RULES - ");

        reportProjectsWithRuleSettings();
        reportMostCommonRulesPerCategory();
        System.out.println("-- STATS on each category in ENABLED rules WITHOUT using a preset --");
        reportStatsOnCategories(configParser.enabledCountPerCategoryWithoutPreset, configParser.projectsThatEnableOneRuleFromCategoryWithoutPreset, configParser.numAnalyzed - usedPreset);
        System.out.println("-- STATS on each category in ENABLED rules WITH using a preset --");
        reportStatsOnCategories(configParser.enabledCountPerCategoryWithPreset, configParser.projectsThatEnableOneRuleFromCategoryWithPreset, usedPreset);
        System.out.println("-- STATS on each category in DISABLED rules WITHOUT using a preset --");
        reportStatsOnCategories(configParser.disabledCountPerCategoryWithoutPreset, configParser.projectsThatDisableOneRuleFromCategoryWithoutPreset, configParser.numAnalyzed - usedPreset);
        System.out.println("-- STATS on each category in DISABLED rules WITH using a preset --");
        reportStatsOnCategories(configParser.disabledCountPerCategoryWithPreset, configParser.projectsThatDisableOneRuleFromCategoryWithPreset, usedPreset);

        reportRulesThatAreNeverEnabled();
        reportProjectsThatDontHaveRules();
        int withoutPreset = configParser.numAnalyzed - usedPreset;
        System.out.println("--- TOP ENABLED RULES WITHOUT PRESET ---");
        reportTopRules(configParser.enabledCountPerCategoryWithoutPreset, withoutPreset);
        System.out.println("--- TOP ENABLED RULES WITH PRESET ---");
        reportTopRules(configParser.enabledCountPerCategoryWithPreset, usedPreset);
        System.out.println("--- TOP DISABLED RULES WITHOUT PRESET ---");
        reportTopRules(configParser.disabledCountPerCategoryWithoutPreset, withoutPreset);
        System.out.println("--- TOP DISABLED RULES WITH PRESET ---");
        reportTopRules(configParser.disabledCountPerCategoryWithPreset, usedPreset);
        System.out.println("--- TOP WARNED RULES WITHOUT PRESET ---");
        reportTopRules(configParser.warningCountPerCategoryWithoutPreset, withoutPreset);
        System.out.println("--- TOP WARNED RULES WITH PRESET ---");
        reportTopRules(configParser.warningCountPerCategoryWithPreset, usedPreset);
    }

    public void reportProjectsWithRuleSettings() {
        // all projects
        System.out.println("All projects:");

        System.out.println("Projects that have at least one rule: " + configParser.projectsThatHaveOneRule.size() + " - "
                + getPercentage(configParser.projectsThatHaveOneRule.size(), configParser.numAnalyzed) + " of all projects");
        int totalRules = configParser.totalErrors + configParser.totalWarnings + configParser.totalDisabled;
        System.out.println("\tTotal rules used by all projects: " + totalRules);
        System.out.println("\tAverage number of rules used per project: " + getAverage(totalRules, configParser.numAnalyzed));

        System.out.println("Projects that have at least one rule as error: " + configParser.projectsThatHaveOneError.size() + " - "
                + getPercentage(configParser.projectsThatHaveOneError.size(), configParser.numAnalyzed) + " of all projects");
        System.out.println("\tTotal errors used by all projects: " + configParser.totalErrors);
        System.out.println("\tAverage number of errors used per project: " + getAverage(configParser.totalErrors, configParser.numAnalyzed));

        System.out.println("Projects that have at least one rule as warning: " + configParser.projectsThatHaveOneWarning.size() + " - "
                + getPercentage(configParser.projectsThatHaveOneWarning.size(), configParser.numAnalyzed) + " of all projects");
        System.out.println("\tTotal warnings used by all projects: " + configParser.totalWarnings);
        System.out.println("\tAverage number of warnings used per project: " + getAverage(configParser.totalWarnings, configParser.numAnalyzed));

        System.out.println("Projects that have at least one rule as off: " + configParser.projectsThatHaveOneDisable.size() + " - "
                + getPercentage(configParser.projectsThatHaveOneDisable.size(), configParser.numAnalyzed) + " of all projects");
        System.out.println("\tTotal disables used by all projects: " + configParser.totalDisabled);
        System.out.println("\tAverage number of disables used per project: " + getAverage(configParser.totalDisabled, configParser.numAnalyzed));
        System.out.println("");

        System.out.println("Only projects without using a preset: ");
        int countAddRule = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneRule.values()) {
            if(!usesPreset) countAddRule++;
        }
        int countAddError = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneError.values()) {
            if(!usesPreset) countAddError++;
        }
        int countAddWarning = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneWarning.values()) {
            if(!usesPreset) countAddWarning++;
        }
        int countAddDisable = 0;
        for(boolean usesPreset : configParser.projectsThatHaveOneDisable.values()) {
            if(!usesPreset) countAddDisable++;
        }

        int totalWithoutPreset = configParser.numAnalyzed - usedPreset;
        int totalErrorsUsed = configParser.totalErrors - configParser.totalErrorsWithPreset;
        int totalWarningsUsed = configParser.totalWarnings - configParser.totalWarningsWithPreset;
        int totalDisablesUsed = configParser.totalDisabled - configParser.totalDisabledWithPreset;
        int totalRulesWithoutPreset = totalErrorsUsed + totalWarningsUsed + totalDisablesUsed;

        System.out.println("Projects that have at least one rule: " + countAddRule + " - "
                + getPercentage(countAddRule, totalWithoutPreset) + " of projects that don't use presets");
        System.out.println("\tTotal rules used by all projects: " + totalRulesWithoutPreset);
        System.out.println("\tAverage number of rules used per project: " + getAverage(totalRulesWithoutPreset, totalWithoutPreset));

        System.out.println("Projects that have at least one rule as error: " + countAddError + " - "
                + getPercentage(countAddError, totalWithoutPreset) + " of projects that don't use presets");
        System.out.println("\tTotal errors used by all projects: " + totalErrorsUsed);
        System.out.println("\tAverage number of errors used per project: " + getAverage(totalErrorsUsed, totalWithoutPreset));


        System.out.println("Projects that have at least one rule as warning: " + countAddWarning + " - "
                + getPercentage(countAddWarning, totalWithoutPreset) + " of projects that don't use presets");
        System.out.println("\tTotal warnings used by all projects: " + totalWarningsUsed);
        System.out.println("\tAverage number of warnings used per project: " + getAverage(totalWarningsUsed, totalWithoutPreset));

        System.out.println("Projects that have at least one rule as off: " + countAddDisable + " - "
                + getPercentage(countAddDisable, totalWithoutPreset) + " of projects that don't use presets");
        System.out.println("\tTotal disables used by all projects: " + totalDisablesUsed);
        System.out.println("\tAverage number of disables used per project: " + getAverage(totalDisablesUsed, totalWithoutPreset));
        System.out.println("");
    }

    public void reportMostCommonRulesPerCategory() {
        System.out.println("- Most common rules per project -");
        System.out.println("");

        int withoutPreset = configParser.numAnalyzed - usedPreset;
        System.out.println("ENABLED rules WITHOUT preset");
        reportMostCommonRuleForCategory(configParser.enabledCountPerCategoryWithoutPreset, withoutPreset);
        System.out.println("ENABLED rules WITH preset");
        reportMostCommonRuleForCategory(configParser.enabledCountPerCategoryWithPreset, usedPreset);
        System.out.println("DISABLED rules WITHOUT preset");
        reportMostCommonRuleForCategory(configParser.disabledCountPerCategoryWithoutPreset, withoutPreset);
        System.out.println("DISABLED rules WITH preset");
        reportMostCommonRuleForCategory(configParser.disabledCountPerCategoryWithPreset, usedPreset);
        System.out.println("WARNED rules WITHOUT preset");
        reportMostCommonRuleForCategory(configParser.warningCountPerCategoryWithoutPreset, withoutPreset);
        System.out.println("WARNED rules WITH preset");
        reportMostCommonRuleForCategory(configParser.warningCountPerCategoryWithPreset, usedPreset);
    }

    public void reportMostCommonRuleForCategory(Map<RuleCategory, Map<String, Integer>> ruleCount, int totalProjects) {
        for(Map.Entry<RuleCategory, Map<String, Integer>> categoryEntry : ruleCount.entrySet()) {
            System.out.println(categoryEntry.getKey().getName());
            int count = 0;
            for(Map.Entry<String, Integer> ruleEntry : categoryEntry.getValue().entrySet()) {
                if(count >= 10) break;
                System.out.println(ruleEntry.getValue() + "\t" + ruleEntry.getKey());
                count++;
            }
            System.out.println("");
        }
    }

    public void reportStatsOnCategories(Map<RuleCategory, Map<String, Integer>> countPerCategory, Map<RuleCategory, Set<String>> projectsWithOne, int totalProjects) {
        System.out.println("");

        for(Map.Entry<RuleCategory, Map<String, Integer>> entry : countPerCategory.entrySet()) {
            int numberOfRulesUsed = entry.getValue().size();
            int totalRuleInstances = entry.getValue().values().stream().mapToInt(Number::intValue).sum();
            RuleCategory ruleCategory = entry.getKey();

            System.out.println("Category: " + ruleCategory.getName());
            System.out.println("Available rules: " + ruleCategory.getNumberOfRules());
            System.out.println("Nr of different rules used: " + numberOfRulesUsed + " - "
                    + getPercentage(numberOfRulesUsed, entry.getKey().getNumberOfRules())  + " of available rules");
            System.out.println("Total instances enabled in all projects: " + totalRuleInstances + " - normalized: " + totalRuleInstances/ruleCategory.getNumberOfRules());
            double averageAmountUsed = getAverage(totalRuleInstances, totalProjects);
            System.out.println("Average amount of rules used per project: " + averageAmountUsed + " - "
                    + getPercentage(averageAmountUsed, ruleCategory.getNumberOfRules()) + " of available rules");
            int numProjectsThatHaveOneRule = projectsWithOne.get(ruleCategory).size();
            System.out.println("Number of projects that have at least one rule: " + numProjectsThatHaveOneRule + " - "
                    + getPercentage(numProjectsThatHaveOneRule, totalProjects) + " out of projects with a preset");

            System.out.println("");
        }
    }

    private void reportRulesThatAreNeverEnabled() {
        System.out.println("Rules that are never enabled");
        Map<String, RuleCategory> allRules = ParserUtils.writeAllRulesToMapWithCategories();
        for(Map.Entry<String, RuleCategory> ruleEntry : allRules.entrySet()) {
            String rule = ruleEntry.getKey();
            boolean containsRule = false;
            boolean onlyOnce = false;
            for(Map<String, Integer> categoryRule : configParser.enabledCountPerCategoryWithoutPreset.values()) {
                if(categoryRule.containsKey(rule)) {
                    if(categoryRule.get(rule) == 1) onlyOnce = true;
                    containsRule = true;
                    break;
                }
            }
            if(!containsRule) {
                System.out.println("Rule is never enabled without preset: " + rule + " - " + allRules.get(rule).getName());
                for(Map<String, Integer> category : configParser.enabledCountPerCategoryWithPreset.values()) {
                    if(category.containsKey(rule)) {
                        if(onlyOnce && category.get(rule) > 1) onlyOnce = false;
                        else if (!containsRule && category.get(rule) == 1) onlyOnce = true;
                        containsRule = true;
                        break;
                    }
                }
            }
            if(!containsRule) System.out.println("Rule is never enabled: " + rule + " - " + allRules.get(rule).getName());
            if(onlyOnce) System.out.println("Rule is only used once: " + rule + " - " + allRules.get(rule).getName());
        }
        System.out.println("");
    }

    private void reportProjectsThatDontHaveRules() {
        System.out.println("--- PROJECTS THAT DONT HAVE RULES AND NO PRESET ---");
        for(String project : configParser.configURLs) {
            if(!configParser.projectsThatHaveOneRule.keySet().contains(project)
                    && !configParser.projectsWithPreset.contains(project)) {
                System.out.println(project);
            }
        }
        System.out.println("");
    }

    private void reportTopRules(Map<RuleCategory, Map<String, Integer>> ruleCounts, int allProjects) {
        List<EnabledRule> ruleCount = new ArrayList<>();
        for(Map.Entry<RuleCategory, Map<String, Integer>> entry : ruleCounts.entrySet()) {
            RuleCategory category = entry.getKey();
            for(Map.Entry<String, Integer> rule : entry.getValue().entrySet()) {
                EnabledRule enabledRule = new EnabledRule(rule.getKey(), rule.getValue(), category);
                ruleCount.add(enabledRule);
            }
        }

        // print rules
        Collections.sort(ruleCount);
        Collections.reverse(ruleCount);
        int count = 0;
        for(EnabledRule enabledRule : ruleCount) {
            if(count >= 20) break;
            String tab = enabledRule.rule.length() < 10 ? "\t\t\t\t\t" : (enabledRule.rule.length() < 20 ? "\t\t\t" : "\t");
            System.out.println(enabledRule.count + "\t" + enabledRule.rule + tab + enabledRule.category.getName());
            count++;
        }
        System.out.println("");
    }

    public static class EnabledRule implements Comparable {
        public String rule;
        public int count;
        public RuleCategory category;

        public EnabledRule(String r, int c, RuleCategory cat) {
            this.rule = r;
            this.count = c;
            this.category = cat;
        }

        @Override
        public int compareTo(Object o) {
            EnabledRule other = (EnabledRule) o;
            if(count < other.count) return -1;
            if(count > other.count) return 1;
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EnabledRule that = (EnabledRule) o;

            if (count != that.count) return false;
            if (rule != null ? !rule.equals(that.rule) : that.rule != null) return false;
            return category == that.category;

        }

        @Override
        public int hashCode() {
            int result = rule != null ? rule.hashCode() : 0;
            result = 31 * result + count;
            result = 31 * result + (category != null ? category.hashCode() : 0);
            return result;
        }
    }
}
