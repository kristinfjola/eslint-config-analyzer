package parser.configparser;

import parser.ConfigFileParser;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Parses rules and presets for YAML format ESLint configuration files.
 */

public class YamlParser extends ConfigParser {

    public YamlParser(ConfigFileParser configParser) {
        super(configParser);
    }

    @Override
    public void parseRules(BufferedReader in, String line, boolean usesPreset, String path) throws IOException {
        int attributeIndentCount = line.indexOf(line.trim());
        while((line = in.readLine()) != null) {
            line = clearComment(line);
            if(line.trim().length() == 0) continue;
            int ruleIndentCount = line.indexOf(line.trim());
            if(ruleIndentCount <= attributeIndentCount) break;    // stop when it's no longer indented

            line = line.trim();
            if(line.length() == 0) continue;

            // format: rule:
            //          - val
            if(line.charAt(line.length() - 1) == ':') {
                String old = line;
                line = in.readLine();   // retrieve value that is always in next line
                if(line != null) {
                    String combined = old + line.replace("-", "").trim();
                    combined = clearComment(combined);
                    parseRule(combined, usesPreset, path);
                }
            // format: rule: val
            } else if(line.contains(":") && line.charAt(0) != '-') {
                parseRule(line.trim(), usesPreset, path);
            }
        }
    }

    @Override
    public boolean parsePresets(BufferedReader in, String line, String configPath, boolean isPreset) throws IOException {
        boolean usesPreset = false;
        int attributeIndentCount = line.indexOf(line.trim());
        while((line = in.readLine()) != null) {
            int pluginIndentCount = line.indexOf(line.trim());
            if(line.length() == 0 || pluginIndentCount <= attributeIndentCount) break;    // stop when it's no longer indented

            line = line.trim();
            line = clearComment(line);
            if(line.length() == 0) break;

            boolean usePreset = parsePresetOrPlugin(line, configPath, isPreset);
            if(usePreset) usesPreset = true;
        }
        return usesPreset;
    }

    @Override
    public String clearComment(String line) {
        if(line.length() > 0 && line.contains("#")) {
            line = line.substring(0, line.indexOf('#'));
        }
        return line;
    }
}
