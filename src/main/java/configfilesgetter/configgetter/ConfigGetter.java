package configfilesgetter.configgetter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ConfigGetter {
    protected String PACKAGE_FILE_NAME = "package.json";
    public Set<String> configURLs = new HashSet<>();

    public abstract boolean isUsingLinter(String baseConfigUrl);

    public Set<String> getConfigUrls(){
        return configURLs;
    }

    protected boolean checkConfigPropertyInPackage(String packageUrl, String configProperty) {
        try {
            URL url = new URL(packageUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                if(line.contains(configProperty)) {
                    in.close();
                    return true;
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
