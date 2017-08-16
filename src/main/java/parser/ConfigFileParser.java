package parser;

import parser.configparser.ConfigParser;
import parser.configparser.JSONParser;
import parser.configparser.YamlParser;


import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Parses the complete config file and looks for presets and rules.
 * Saves all results to report.
 * Delegates tasks to parse 1) YAML format or 2) JSON, JS format. Does not rely on file endings to identify format
 * because files can be named '.eslintrc' without including another file ending.
 */

public class ConfigFileParser {

    public Map<String, Integer> presets = new HashMap<>();
    public Set<String> projectsWithPreset = new HashSet<>();
    public Map<String, Integer> plugins = new HashMap<>();
    public Set<String> projectsWithPlugins = new HashSet<>();
    public Map<String, Integer> pluginsAsPresets = new HashMap<>();
    public Set<String> projectsWithPluginsAsPresets = new HashSet<>();
    public Set<String> configURLs = new HashSet<>();
    public int numAnalyzed = 0;

    // rule categories
    public Map<RuleCategory, Map<String, Integer>> enabledCountPerCategoryWithoutPreset = new HashMap<>();  // <ruleCategory, <rule, count>>
    public Map<RuleCategory, Map<String, Integer>> enabledCountPerCategoryWithPreset = new HashMap<>();
    public Map<RuleCategory, Map<String, Integer>> disabledCountPerCategoryWithoutPreset = new HashMap<>();
    public Map<RuleCategory, Map<String, Integer>> disabledCountPerCategoryWithPreset = new HashMap<>();
    public Map<RuleCategory, Map<String, Integer>> warningCountPerCategoryWithoutPreset = new HashMap<>();
    public Map<RuleCategory, Map<String, Integer>> warningCountPerCategoryWithPreset = new HashMap<>();
    public Map<RuleCategory, Set<String>> projectsThatEnableOneRuleFromCategoryWithoutPreset = new HashMap<>();    // <ruleCategory, Set<projectPath>>
    public Map<RuleCategory, Set<String>> projectsThatEnableOneRuleFromCategoryWithPreset = new HashMap<>();
    public Map<RuleCategory, Set<String>> projectsThatDisableOneRuleFromCategoryWithoutPreset = new HashMap<>();
    public Map<RuleCategory, Set<String>> projectsThatDisableOneRuleFromCategoryWithPreset = new HashMap<>();

    // rule settings
    public Map<String, Boolean> projectsThatHaveOneRule = new HashMap<>(); // <projectPath, usesPreset>
    public Map<String, Boolean> projectsThatHaveOneError = new HashMap<>(); //
    public Map<String, Boolean> projectsThatHaveOneWarning = new HashMap<>();
    public Map<String, Boolean> projectsThatHaveOneDisable = new HashMap<>();
    public int totalErrors = 0;
    public int totalErrorsWithPreset = 0;
    public int totalWarnings = 0;
    public int totalWarningsWithPreset = 0;
    public int totalDisabled = 0;
    public int totalDisabledWithPreset = 0;

    protected String PACKAGE_FILE_NAME = "package.json";
    protected String ESLINT_CONFIG_PROPERTY = "eslintConfig";

    private ConfigParser yamlParser;
    private ConfigParser jsonParser;

    public ConfigFileParser(Set<String> configURLs) {
        this.configURLs = configURLs;
        this.yamlParser = new YamlParser(this);
        this.jsonParser = new JSONParser(this);

        initializeFields();
    }

    private void initializeFields() {
        for(RuleCategory category : RuleCategory.values()) {
            enabledCountPerCategoryWithoutPreset.put(category, new HashMap<>());
            enabledCountPerCategoryWithPreset.put(category, new HashMap<>());
            disabledCountPerCategoryWithoutPreset.put(category, new HashMap<>());
            disabledCountPerCategoryWithPreset.put(category, new HashMap<>());
            warningCountPerCategoryWithoutPreset.put(category, new HashMap<>());
            warningCountPerCategoryWithPreset.put(category, new HashMap<>());
            projectsThatEnableOneRuleFromCategoryWithoutPreset.put(category, new HashSet<>());
            projectsThatEnableOneRuleFromCategoryWithPreset.put(category, new HashSet<>());
            projectsThatDisableOneRuleFromCategoryWithoutPreset.put(category, new HashSet<>());
            projectsThatDisableOneRuleFromCategoryWithPreset.put(category, new HashSet<>());
        }
    }

    public void parseConfigFiles() {
        long startTime = System.nanoTime();

        for(String configPath : configURLs) {
            if(numAnalyzed % 100 == 0) System.out.println("Parsed: " + numAnalyzed);
            try {
                readURLContent(configPath);
                numAnalyzed++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sortResults();

        long totalTimeSeconds = (long) ((System.nanoTime() - startTime) / 1000000000.0);
        System.out.println("");
        System.out.println("Total time to parse configuration files: " + totalTimeSeconds);
    }

    protected void readURLContent(String path) throws IOException {
        URL url = new URL(path);
        BufferedReader inPresets = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        BufferedReader inPlugins = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        BufferedReader inRules = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

        // check if it's a 'package.json' file
        String possiblePackageEnding = path.substring(path.length() - PACKAGE_FILE_NAME.length());
        if(possiblePackageEnding.equals(PACKAGE_FILE_NAME)) {
            String line;
            while ((line = inPresets.readLine()) != null) {
                if(line.contains(ESLINT_CONFIG_PROPERTY)) {
                    boolean usesPreset = parseConfigurationFileForPresets(inPresets, path, true);
                    line = inPlugins.readLine();
                    while(line != null && !line.contains(ESLINT_CONFIG_PROPERTY)) line = inPlugins.readLine();
                    parseConfigurationFileForPlugins(inPlugins, path, true);
                    line = inRules.readLine();
                    while(line != null && !line.contains(ESLINT_CONFIG_PROPERTY)) line = inRules.readLine();
                    parseConfigurationFileForRules(inRules, path, true, usesPreset);
                    inPresets.close(); // we don't need to check rest of package.json
                    inPlugins.close();
                    inRules.close();
                    return;
                }
            }
        // normal '.eslintrc.*' config file
        } else {
            boolean usesPreset = parseConfigurationFileForPresets(inPresets, path, false);
            parseConfigurationFileForPlugins(inPlugins, path, false);
            parseConfigurationFileForRules(inRules, path, false, usesPreset);
        }

        inPresets.close();
        inPlugins.close();
        inRules.close();
    }

    protected boolean parseConfigurationFileForPresets(BufferedReader in, String path, boolean isPackageFile) throws IOException {
        boolean usesPreset = false;
        int curlyCounter = 0;
        String line;
        while ((line = in.readLine()) != null) {
            String originalLine = line;
            line = line.trim();
            line = line.replace("\"", "");
            line = line.replace("\'", "");

            // check if we have finished parsing 'eslintConfig' section of package
            if(isPackageFile && curlyCounter <= 0 && (line.equals("}") || line.equals("},"))) return usesPreset;
            if(line.contains("{")) curlyCounter++;
            if(line.contains("}")) curlyCounter--;

            // presets
            if(line.contains("extends")) {
                usesPreset = parsePresets(in, originalLine, line, path);
            }
        }
        return usesPreset;
    }

    protected void parseConfigurationFileForPlugins(BufferedReader in, String path, boolean isPackageFile) throws IOException {
        int curlyCounter = 0;
        String line;
        while ((line = in.readLine()) != null) {
            String originalLine = line;
            line = line.trim();
            line = line.replace("\"", "");
            line = line.replace("\'", "");

            // check if we have finished parsing 'eslintConfig' section of package
            if(isPackageFile && curlyCounter <= 0 && (line.equals("}") || line.equals("},"))) return;
            if(line.contains("{")) curlyCounter++;
            if(line.contains("}")) curlyCounter--;

            // plugins
            if(line != null && line.contains("plugins")) {
                parsePlugins(in, originalLine, line, path);
            }
        }
    }

    protected void parseConfigurationFileForRules(BufferedReader in, String path, boolean isPackageFile, boolean usesPreset) throws IOException {
        int curlyCounter = 0;
        String line;
        while ((line = in.readLine()) != null) {
            String originalLine = line;
            line = line.trim();
            line = line.replace("\"", "");
            line = line.replace("\'", "");

            // check if we have finished parsing 'eslintConfig' section of package
            if(isPackageFile && curlyCounter <= 0 && (line.equals("}") || line.equals("},"))) return;
            if(line.contains("{")) curlyCounter++;
            if(line.contains("}")) curlyCounter--;

            // rules
            if(line != null && line.contains("rules")) {
                parseRules(in, originalLine, line, usesPreset, path);
                if(line.contains("{")) curlyCounter--;  // we don't parse closing curly bracket
            }
        }
    }

    private boolean parsePresets(BufferedReader in, String originalLine, String line, String configPath) throws IOException {
        line = clearComment(line);
        if(line.length() > 0 && line.contains(":")) {
            String trimmed = line.substring(0, line.indexOf(':')).trim();
            if(trimmed.equals("extends")) {
                if(line.equals("extends:")) {
                    return yamlParser.parsePresets(in, originalLine, configPath, true);
                } else {
                    return jsonParser.parsePresets(in, line, configPath, true);
                }
            }
        }

        return false;
    }

    private void parsePlugins(BufferedReader in, String originalLine, String line, String configPath) throws IOException {
        line = clearComment(line);
        if(line.length() > 0 && line.contains(":")) {
            String trimmed = line.substring(0, line.indexOf(':')).trim();
            if(trimmed.equals("plugins")) {
                String fileEnding = getFileEnding(configPath);
                if(line.equals("plugins:") && !fileEnding.equals("js") && !fileEnding.equals("json")) {
                    yamlParser.parsePresets(in, originalLine, configPath, false);
                } else {
                    jsonParser.parsePresets(in, line, configPath, false);
                }
            }
        }
    }

    private void parseRules(BufferedReader in, String originalLine, String line, boolean usesPreset, String path) throws IOException {
        String rules = line.replace("\"", "");
        rules = rules.replace("\'", "");
        rules = rules.replace(" ", "");
        rules = clearComment(rules);
        rules = rules.trim();

        // yml: "rules:"
        if(rules.length() == 6 && rules.charAt(5) == ':') {
            yamlParser.parseRules(in, originalLine, usesPreset, path);
        // other formats: "rules: {"
        } else if(rules.length() == 7 && rules.charAt(6) == '{') {
            jsonParser.parseRules(in, line, usesPreset, path);
        }
    }

    private void sortResults() {
        presets = ParserUtils.sortMapByValue(presets);
        plugins = ParserUtils.sortMapByValue(plugins);
        pluginsAsPresets = ParserUtils.sortMapByValue(pluginsAsPresets);
        sortRuleCount(enabledCountPerCategoryWithoutPreset);
        sortRuleCount(enabledCountPerCategoryWithPreset);
        sortRuleCount(disabledCountPerCategoryWithoutPreset);
        sortRuleCount(disabledCountPerCategoryWithPreset);
        sortRuleCount(warningCountPerCategoryWithoutPreset);
        sortRuleCount(warningCountPerCategoryWithPreset);
    }

    private void sortRuleCount(Map<RuleCategory, Map<String, Integer>> ruleCount) {
        for(Map.Entry<RuleCategory, Map<String, Integer>> entry : ruleCount.entrySet()) {
            Map<String, Integer> sortedRuleCount = ParserUtils.sortMapByValue(entry.getValue());
            ruleCount.put(entry.getKey(), sortedRuleCount);
        }
    }

    public String clearComment(String line) {
        line = yamlParser.clearComment(line);
        line = jsonParser.clearComment(line);
        return line;
    }

    private String getFileEnding(String path) {
        String fileName = ".eslintrc.";
        String packageName = "package.";
        String ending = "";
        if(fileName.contains(fileName)) {
            ending = path.substring(path.indexOf(fileName) + fileName.length());
        } else if(fileName.contains(packageName)) {
            ending = path.substring(path.indexOf(packageName) + packageName.length());
        }
        return ending;
    }
}
