package configfilesgetter.configgetter;

import configfilesgetter.ConfigGetterUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JSCSConfigGetter extends ConfigGetter {

    private String JSCS_CONFIG_PROPERTY = "jscsConfig";
    private List<String> JSCSFileNames = new ArrayList<>();

    public JSCSConfigGetter() {
        createConfigFileEndings();
    }

    @Override
    public boolean isUsingLinter(String baseConfigUrl) {
        boolean usesJSCS = false;
        for(String ending : JSCSFileNames) {
            String JSCSConfigFileUrl = baseConfigUrl + ending;  // JSCS has two different names
            if(ConfigGetterUtils.urlExists(JSCSConfigFileUrl, false)) {
                configURLs.add(JSCSConfigFileUrl);
                usesJSCS = true;
                break;
            }
        }

        if(!usesJSCS) {
            String packageUrl = baseConfigUrl + PACKAGE_FILE_NAME;
            if(ConfigGetterUtils.urlExists(packageUrl, false)) {
                usesJSCS = checkConfigPropertyInPackage(packageUrl, JSCS_CONFIG_PROPERTY);
                if(usesJSCS) {
                    configURLs.add(packageUrl);
                }
            }
        }

        return usesJSCS;
    }

    public void createConfigFileEndings() {
        JSCSFileNames.add(".jscs.json");
        JSCSFileNames.add(".jscsrc");
    }
}
