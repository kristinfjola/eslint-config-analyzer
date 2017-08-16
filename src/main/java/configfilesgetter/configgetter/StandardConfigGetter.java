package configfilesgetter.configgetter;

import configfilesgetter.ConfigGetterUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StandardConfigGetter extends ConfigGetter {
    private String DEPENDENCY_PROPERTY = "devDependencies";
    private String STANDARD_PACKAGE = "standard";

    @Override
    public boolean isUsingLinter(String baseConfigUrl) {
        boolean usesStandard = false;
        String packageUrl = baseConfigUrl + PACKAGE_FILE_NAME;
        if(ConfigGetterUtils.urlExists(packageUrl, false)) {
            usesStandard = hasDependency(packageUrl);
            if(usesStandard) configURLs.add(packageUrl);
        }

        return usesStandard;
    }

    private boolean hasDependency(String packageUrl) {
        try {
            URL url = new URL(packageUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

            String line;
            while ((line = in.readLine()) != null) {
                if(line.contains(DEPENDENCY_PROPERTY)) {
                    while((line = in.readLine()) != null) {
                        line = line.replace("\"", "");
                        line = line.replace(",", "");
                        line = line.trim();
                        if(line.equals("}")) break;
                        if(line.length() == 0 || !line.contains(":")) continue;

                        String dependency = line.substring(0, line.indexOf(':')).trim();
                        if(dependency.equals(STANDARD_PACKAGE)) {
                            in.close();
                            return true;
                        }
                    }

                    in.close(); // don't need to parse rest of file if standard was not in 'devDependencies'
                    return false;
                }
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
