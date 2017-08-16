package configfilesgetter.configgetter;

import configfilesgetter.ConfigGetterUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JSHintConfigGetter extends ConfigGetter {

    private String JSHINT_CONFIG_FILE_NAME = ".jshintrc";
    private String JSHINT_CONFIG_PROPERTY = "jshintConfig";

    @Override
    public boolean isUsingLinter(String baseConfigUrl) {
        boolean usesJSHint = false;
        String JSHintConfigFileUrl = baseConfigUrl + JSHINT_CONFIG_FILE_NAME;
        if(ConfigGetterUtils.urlExists(JSHintConfigFileUrl, false)) {
            configURLs.add(JSHintConfigFileUrl);
            usesJSHint = true;
        }

        // check 'package.json'
        if(!usesJSHint) {
            String packageUrl = baseConfigUrl + PACKAGE_FILE_NAME;
            if(ConfigGetterUtils.urlExists(packageUrl, false)) {
                usesJSHint = checkConfigPropertyInPackage(packageUrl, JSHINT_CONFIG_PROPERTY);
                if(usesJSHint) configURLs.add(packageUrl);
            }
        }

        return usesJSHint;
    }

}
