package configfilesgetter.configgetter;

import configfilesgetter.ConfigGetterUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ESLintConfigGetter extends ConfigGetter {
    private String ESLINT_CONFIG_FILE_NAME = ".eslintrc";
    private String ESLINT_CONFIG_PROPERTY = "eslintConfig";
    private List<String> ESLintFileEndings = new ArrayList<>();

    public ESLintConfigGetter() {
        createConfigFileEndings();
    }

    @Override
    public boolean isUsingLinter(String baseConfigUrl) {
        boolean usesESLint = false;
        for(String ending : ESLintFileEndings) {
            String ESLintConfigFileUrl = baseConfigUrl + ESLINT_CONFIG_FILE_NAME + ending;
            if(ConfigGetterUtils.urlExists(ESLintConfigFileUrl, false)) {
                configURLs.add(ESLintConfigFileUrl);
                usesESLint = true;
                break;
            }
        }

        // check 'package.json'
        if(!usesESLint) {
            String packageUrl = baseConfigUrl + PACKAGE_FILE_NAME;
            if(ConfigGetterUtils.urlExists(packageUrl, false)) {
                usesESLint = checkConfigPropertyInPackage(packageUrl, ESLINT_CONFIG_PROPERTY);
                if(usesESLint) {
                    configURLs.add(packageUrl);
                }
            }
        }

        return usesESLint;
    }

    private void createConfigFileEndings() {
        ESLintFileEndings.add(".js");
        ESLintFileEndings.add(".yaml");
        ESLintFileEndings.add(".yml");
        ESLintFileEndings.add(".json");
        ESLintFileEndings.add("");    // for deprecated format with no ending
    }
}
