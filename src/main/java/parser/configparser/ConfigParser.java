package parser.configparser;

import parser.ParserUtils;
import parser.RuleCategory;
import parser.RuleSetting;
import parser.ConfigFileParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parser for individual rules and presets where the format doesn't matter anymore.
 * Writes results to 'ConfigFileParser' because we want all results at the same place, no matter the file format.
 */
public abstract class ConfigParser {

    private ConfigFileParser cp;
    private Map<String, RuleCategory> allRules = new HashMap<>();   // <rule, ruleCategory>
    private Map<String, RuleSetting> ruleSettings = new HashMap<>();    // <rule, ruleSetting>

    public ConfigParser(ConfigFileParser configParser) {
        this.cp = configParser;
        initializeFields();
    }

    private void initializeFields() {
        ruleSettings.put("0", RuleSetting.OFF);
        ruleSettings.put("1", RuleSetting.WARNING);
        ruleSettings.put("2", RuleSetting.ERROR);
        ruleSettings.put("OFF", RuleSetting.OFF);
        ruleSettings.put("WARN", RuleSetting.WARNING);
        ruleSettings.put("ERROR", RuleSetting.ERROR);

        allRules = ParserUtils.writeAllRulesToMapWithCategories();
    }

    public abstract void parseRules(BufferedReader in, String line, boolean usesPreset, String path) throws IOException;

    public abstract boolean parsePresets(BufferedReader in, String line, String path, boolean isPreset) throws IOException;

    public abstract String clearComment(String line);

    protected void parseRule(String line, boolean usesPreset, String configPath) {
        if(line.length() == 0) return;
        line = line.replace("\"", "");
        line = line.replace("\'", "");

        String rule = line.substring(0, line.indexOf(':')).trim();
        String val = line.substring(line.indexOf(':') + 1).trim();

        if(!allRules.containsKey(rule)) return;    // filter out extra settings to a rule (e.g. message), or custom rules

        String actualVal = parseRuleValue(val);
        if(!ruleSettings.containsKey(actualVal.toUpperCase())) return;  // not valid setting

        RuleSetting ruleSetting = ruleSettings.get(actualVal.toUpperCase());
        RuleCategory ruleCategory = allRules.get(rule);

        // write results
        if(!cp.projectsThatHaveOneRule.containsKey(configPath)) {
            cp.projectsThatHaveOneRule.put(configPath, usesPreset);
        }
        if(ruleSetting == RuleSetting.ERROR) {
            cp.totalErrors++;
            if(usesPreset) {
                cp.totalErrorsWithPreset++;
                countRule(rule, cp.enabledCountPerCategoryWithPreset);
                if(!cp.projectsThatEnableOneRuleFromCategoryWithPreset.get(ruleCategory).contains(configPath)) {
                    cp.projectsThatEnableOneRuleFromCategoryWithPreset.get(ruleCategory).add(configPath);
                }
            } else {
                countRule(rule, cp.enabledCountPerCategoryWithoutPreset);
                if(!cp.projectsThatEnableOneRuleFromCategoryWithoutPreset.get(ruleCategory).contains(configPath)) {
                    cp.projectsThatEnableOneRuleFromCategoryWithoutPreset.get(ruleCategory).add(configPath);
                }
            }
            if(!cp.projectsThatHaveOneError.containsKey(configPath)) {
                cp.projectsThatHaveOneError.put(configPath, usesPreset);
            }
        } else if (ruleSetting == RuleSetting.OFF) {
            cp.totalDisabled++;
            if(usesPreset) {
                cp.totalDisabledWithPreset++;
                countRule(rule, cp.disabledCountPerCategoryWithPreset);
                if(!cp.projectsThatDisableOneRuleFromCategoryWithPreset.get(ruleCategory).contains(configPath)) {
                    cp.projectsThatDisableOneRuleFromCategoryWithPreset.get(ruleCategory).add(configPath);
                }
            } else {
                countRule(rule, cp.disabledCountPerCategoryWithoutPreset);
                if(!cp.projectsThatDisableOneRuleFromCategoryWithoutPreset.get(ruleCategory).contains(configPath)) {
                    cp.projectsThatDisableOneRuleFromCategoryWithoutPreset.get(ruleCategory).add(configPath);
                }
            }
            if(!cp.projectsThatHaveOneDisable.containsKey(configPath)) {
                cp.projectsThatHaveOneDisable.put(configPath, usesPreset);
            }
        } else if (ruleSetting == RuleSetting.WARNING) {
            cp.totalWarnings++;
            if(usesPreset) {
                cp.totalWarningsWithPreset++;
                countRule(rule, cp.warningCountPerCategoryWithPreset);
            } else {
                countRule(rule, cp.warningCountPerCategoryWithoutPreset);
            }
            if(!cp.projectsThatHaveOneWarning.containsKey(configPath)) {
                cp.projectsThatHaveOneWarning.put(configPath, usesPreset);
            }
        }
    }

    private String parseRuleValue(String val) {
        val = val.replace("[", "");
        val = val.replace("]", "");
        String[] splits = val.split(",");
        String actualVal = splits[0].trim();
        actualVal = actualVal.length() > 5 ? actualVal.substring(0, 5).trim() : actualVal.trim(); // longest setting is 'error'

        return actualVal;
    }

    private void countRule(String rule, Map<RuleCategory, Map<String, Integer>> ruleCount) {
        RuleCategory ruleCategory = allRules.get(rule);
        if(ruleCount.get(ruleCategory).containsKey(rule)) {
            int previousCount = ruleCount.get(ruleCategory).get(rule);
            ruleCount.get(ruleCategory).put(rule, previousCount + 1);
        } else {
            ruleCount.get(ruleCategory).put(rule, 1);
        }
    }

    protected boolean parsePreset(String p, String configPath){
        // plugin as preset
        if(p.length() > 7 && p.substring(0, 7).equals("plugin:")) {
            countPresetOrPlugin(p, configPath, cp.pluginsAsPresets, cp.projectsWithPluginsAsPresets);
            return false;
        // preset
        } else {
            if (p.contains("eslint-config-")) p = p.replace("eslint-config-", "");    // can omit
            countPresetOrPlugin(p, configPath, cp.presets, cp.projectsWithPreset);
            return true;
        }
    }

    protected void parsePlugin(String p, String configPath){
        if (p.contains("eslint-plugin-")) p = p.replace("eslint-plugin-", "");    // can omit
        countPresetOrPlugin(p, configPath, cp.plugins, cp.projectsWithPlugins);
    }

    protected boolean parsePresetOrPlugin(String line, String configPath, boolean isPreset){
        if(line.length() == 0) return false;
        boolean usesPreset = false;
        String trimmed = trimPresetOrPlugin(line);
        String[] splits = trimmed.split(",");
        for(String p : splits) {
            p = cp.clearComment(p);
            p = p.trim();
            if(p.length() == 0) continue;

            if(isPreset) {
                boolean usedPreset = parsePreset(p, configPath);
                if(usedPreset) usesPreset = true;
            } else {
                parsePlugin(p, configPath);
            }
        }
        return usesPreset;
    }

    private String trimPresetOrPlugin(String line) {
        String rest = trimPreset(line);
        rest = trimPlugin(rest);
        rest = clearComment(rest);
        if(line.charAt(0) == '-') rest = rest.substring(1); // yml
        rest = rest.replace("[", "");
        rest = rest.replace("]", "");
        rest = rest.replace("\"", "");
        rest = rest.replace("\'", "");
        rest = rest.trim();

        return rest;
    }

    private String trimPreset(String line) {
        if(line.contains("extends")) line = line.substring(line.indexOf(":") + 1);
        return line;
    }

    private String trimPlugin(String line) {
        if(line.contains("plugins")) line = line.substring(line.indexOf(":") + 1);
        return line;
    }

    private void countPresetOrPlugin(String presetOrPlugin, String configPath, Map<String, Integer> counts, Set<String> projects) {
        if(counts.containsKey(presetOrPlugin)) {
            counts.put(presetOrPlugin, counts.get(presetOrPlugin) + 1);
        } else {
            counts.put(presetOrPlugin, 1);
        }
        if(!projects.contains(configPath)) {
            projects.add(configPath);
        }
    }
}
