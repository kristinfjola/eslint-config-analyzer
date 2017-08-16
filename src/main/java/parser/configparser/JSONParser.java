package parser.configparser;

import parser.ConfigFileParser;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Parses rules and presets for JSON and JS format ESLint configuration files.
 */

public class JSONParser extends ConfigParser {
    public JSONParser(ConfigFileParser configParser) {
        super(configParser);
    }

    @Override
    public void parseRules(BufferedReader in, String line, boolean usesPreset, String path) throws IOException {
        int curlyCounter = 1;   // to know when rules section ends

        while((line = in.readLine()) != null) {
            line = line.trim();
            line = clearComment(line);

            if(line.length() == 0) continue;
            if(line.contains("{")) {
                curlyCounter++;
            }
            if(line.contains("}")) {
                curlyCounter--;
                if(curlyCounter == 0) break;
            }

            if(line.contains(":")) {
                // format: rule : [
                //              val,
                if(line.charAt(line.length() - 1) == '[') {
                    String old = line;
                    line = in.readLine();   // retrieve value that is always in next line
                    if(line != null) {
                        String combined = old + line.trim();
                        combined = clearComment(combined);
                        parseRule(combined, usesPreset, path);
                    }
                // format: rule: val
                } else {
                    parseRule(line, usesPreset, path);
                }
            }
        }
    }

    @Override
    public boolean parsePresets(BufferedReader in, String line, String configPath, boolean isPreset) throws IOException {
        boolean usesPreset = false;
        // array of presets
        if(line.charAt(line.length() - 1) == '[') {
            while((line = in.readLine()) != null && !line.contains("]")) {
                boolean usePreset = parsePresetOrPlugin(line.trim(), configPath, isPreset);
                if(usePreset) usesPreset = true;
            }
        // format: extends: preset
        } else {
            usesPreset = parsePresetOrPlugin(line, configPath, isPreset);
        }
        return usesPreset;
    }

    @Override
    public String clearComment(String line) {
        if(line.length() > 0) {
            if(line.contains("//")) {
                line = line.substring(0, line.indexOf("//"));
            } else if(line.contains("/*") && line.contains("*/")) {
                line = line.substring(0, line.indexOf("/*")) + line.substring(line.indexOf("*/") + 2);
            } else if(line.contains("/*")) {
                line = line.substring(0, line.indexOf("/*"));
            } else if(line.contains("*/")) {
                line = line.substring(line.indexOf("*/") + 2);
            }
        }
        return line.trim();
    }
}
