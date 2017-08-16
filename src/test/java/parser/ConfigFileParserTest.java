package parser;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Set;

import static org.junit.Assert.*;



public class ConfigFileParserTest {

    private ConfigFileParser configFileParser;

    @Before
    public void setUp() throws Exception {
        configFileParser = new ConfigFileParser(null);
    }

    /**
     * JSON & preset
     *
     * Tests parsing a single configuration file that uses a preset and has several rules.
     * Tests all aspects of parsing a json file (also other methods and classes).
     * @throws Exception
     */

    @Test
    public void testParseJSONConfigFileWithPresetAndMultiplePlugins() throws Exception {
        String filePath = "src/test/resources/test-config-file-1.eslintrc.json";
        BufferedReader brPresets = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedReader brPlugins = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedReader brRules = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        configFileParser.parseConfigurationFileForPresets(brPresets, filePath, false);
        configFileParser.parseConfigurationFileForPlugins(brPlugins, filePath, false);
        configFileParser.parseConfigurationFileForRules(brRules, filePath, false, true);

        // presets
        assertEquals(1, configFileParser.projectsWithPreset.size());
        assertTrue(configFileParser.presets.containsKey("eslint:recommended"));

        // plugins
        assertEquals(1, configFileParser.projectsWithPlugins.size());
        assertEquals(3, configFileParser.plugins.size());
        assertTrue(configFileParser.plugins.containsKey("node"));
        assertTrue(configFileParser.plugins.containsKey("react"));
        assertTrue(configFileParser.plugins.containsKey("import"));

        // rules quantity
        assertEquals(2, configFileParser.totalErrors);
        assertEquals(2, configFileParser.totalErrorsWithPreset);
        assertEquals(2, configFileParser.totalWarnings);
        assertEquals(2, configFileParser.totalWarningsWithPreset);
        assertEquals(3, configFileParser.totalDisabled);
        assertEquals(3, configFileParser.totalDisabledWithPreset);

        // rule settings
        assertTrue(configFileParser.projectsThatHaveOneError.containsKey(filePath));
        assertTrue(configFileParser.projectsThatHaveOneWarning.containsKey(filePath));
        assertTrue(configFileParser.projectsThatHaveOneDisable.containsKey(filePath));
        assertFalse(configFileParser.projectsThatEnableOneRuleFromCategoryWithoutPreset.containsKey(filePath));

        // rule categories
        assertEquals(1, configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).size());
        assertTrue(configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).containsKey("no-cond-assign"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).get("no-cond-assign"));
        assertFalse(configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).containsKey("no-console"));
        assertTrue(configFileParser.disabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).containsKey("no-console"));
        assertFalse(configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).containsKey("use-isnan"));
        assertTrue(configFileParser.disabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).containsKey("use-isnan"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.VARIABLES).get("no-undef"));
    }

    @Test
    public void testParseYAMLFileWithMultiplePresetsAndPlugins() throws Exception {
        String filePath = "src/test/resources/test-config-file-2.eslintrc.yaml";
        BufferedReader brPresets = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedReader brPlugins = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedReader brRules = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

        configFileParser.parseConfigurationFileForPresets(brPresets, filePath, false);
        configFileParser.parseConfigurationFileForPlugins(brPlugins, filePath, false);
        configFileParser.parseConfigurationFileForRules(brRules, filePath, false, true);

        // presets
        assertEquals(1, configFileParser.projectsWithPreset.size());
        assertEquals(2, configFileParser.presets.size());
        assertTrue(configFileParser.presets.containsKey("preset1"));
        assertTrue(configFileParser.presets.containsKey("preset2"));

        // plugins
        assertEquals(1, configFileParser.projectsWithPlugins.size());
        assertEquals(1, configFileParser.projectsWithPluginsAsPresets.size());
        assertTrue(configFileParser.plugins.containsKey("node"));
        assertTrue(configFileParser.pluginsAsPresets.containsKey("plugin:node/recommended"));

        // rules quantity
        assertEquals(3, configFileParser.totalErrors);
        assertEquals(3, configFileParser.totalErrorsWithPreset);
        assertEquals(2, configFileParser.totalWarnings);
        assertEquals(2, configFileParser.totalWarningsWithPreset);
        assertEquals(2, configFileParser.totalDisabled);
        assertEquals(2, configFileParser.totalDisabledWithPreset);

        // rule settings
        assertTrue(configFileParser.projectsThatHaveOneError.containsKey(filePath));
        assertTrue(configFileParser.projectsThatHaveOneWarning.containsKey(filePath));
        assertTrue(configFileParser.projectsThatHaveOneDisable.containsKey(filePath));
        assertFalse(configFileParser.projectsThatEnableOneRuleFromCategoryWithoutPreset.containsKey(filePath));

        // rule categories
        assertEquals(0, configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).size());
        assertEquals(1, configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.NODE_COMMON).size());
        assertEquals(2, configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).size());
        assertEquals(1, configFileParser.disabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).size());
        assertEquals(1, configFileParser.disabledCountPerCategoryWithPreset.get(RuleCategory.NODE_COMMON).size());
        assertEquals(1, configFileParser.warningCountPerCategoryWithPreset.get(RuleCategory.NODE_COMMON).size());
        assertEquals(1, configFileParser.warningCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).size());

        assertEquals(1, (int) configFileParser.disabledCountPerCategoryWithPreset.get(RuleCategory.POSSIBLE_ERRORS).get("no-control-regex"));
        assertEquals(1, (int) configFileParser.warningCountPerCategoryWithPreset.get(RuleCategory.NODE_COMMON).get("no-mixed-requires"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.NODE_COMMON).get("no-path-concat"));
        assertEquals(1, (int) configFileParser.disabledCountPerCategoryWithPreset.get(RuleCategory.NODE_COMMON).get("no-restricted-modules"));
        assertEquals(1, (int) configFileParser.warningCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).get("block-spacing"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).get("brace-style"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).get("comma-dangle"));
    }

    @Test
    public void testParseYAMLFileWithMoreIndent() throws Exception {
        String filePath = "src/test/resources/test-config-file-4.eslintrc.yml";
        BufferedReader brPresets = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedReader brPlugins = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedReader brRules = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

        configFileParser.parseConfigurationFileForPresets(brPresets, filePath, false);
        configFileParser.parseConfigurationFileForPlugins(brPlugins, filePath, false);
        configFileParser.parseConfigurationFileForRules(brRules, filePath, false, true);

        // presets
        assertEquals(1, configFileParser.projectsWithPreset.size());
        assertEquals(1, configFileParser.presets.size());
        assertTrue(configFileParser.presets.containsKey("airbnb"));


        // plugins
        assertEquals(1, configFileParser.projectsWithPlugins.size());
        assertEquals(2, configFileParser.plugins.size());
        assertTrue(configFileParser.plugins.containsKey("node"));
        assertTrue(configFileParser.plugins.containsKey("react"));

        // rules
        assertEquals(2, configFileParser.totalErrors);
        assertEquals(2, configFileParser.totalErrorsWithPreset);
        assertTrue(configFileParser.projectsThatHaveOneError.containsKey(filePath));
        assertEquals(2, configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).size());
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).get("array-bracket-spacing"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithPreset.get(RuleCategory.STYLISTIC).get("block-spacing"));
    }

    @Test
    public void testParsePackageFileWithoutPresetAndPlugins() throws Exception {
        configFileParser = new ConfigFileParserMock(null);
        String filePath = "src/test/resources/test-config-file-3.package.json";
        configFileParser.readURLContent(filePath);

        // presets
        assertEquals(0, configFileParser.projectsWithPreset.size());
        assertEquals(0, configFileParser.presets.size());

        // plugins
        assertEquals(0, configFileParser.projectsWithPlugins.size());
        assertEquals(0, configFileParser.plugins.size());

        // rules quantity
        assertEquals(1, configFileParser.totalErrors);
        assertEquals(0, configFileParser.totalErrorsWithPreset);
        assertEquals(1, configFileParser.totalWarnings);
        assertEquals(0, configFileParser.totalWarningsWithPreset);
        assertEquals(1, configFileParser.totalDisabled);
        assertEquals(0, configFileParser.totalDisabledWithPreset);

        // rule categories
        assertEquals(1, (int) configFileParser.disabledCountPerCategoryWithoutPreset.get(RuleCategory.STYLISTIC).get("comma-dangle"));
        assertEquals(1, (int) configFileParser.warningCountPerCategoryWithoutPreset.get(RuleCategory.STYLISTIC).get("quotes"));
        assertEquals(1, (int) configFileParser.enabledCountPerCategoryWithoutPreset.get(RuleCategory.BEST_PRACTICES).get("block-scoped-var"));
    }

    public static class ConfigFileParserMock extends ConfigFileParser {
        public ConfigFileParserMock(Set<String> configURLs) {
            super(configURLs);
        }

        /*
            Override to read file input rather than URL input.
         */
        @Override
        protected void readURLContent(String path) throws IOException {
            BufferedReader inPresets = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            BufferedReader inPlugins = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            BufferedReader inRules = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));

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
            }
        }
    }

}