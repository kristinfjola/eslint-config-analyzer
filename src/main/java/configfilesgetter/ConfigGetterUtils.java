package configfilesgetter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ConfigGetterUtils {
    public static boolean urlExists(String path, boolean printCode) {
        try {
            URL u = new URL(path);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("HEAD");
            huc.setConnectTimeout(1000);
            huc.connect();
            int statusCode = huc.getResponseCode();
            if(statusCode != HttpURLConnection.HTTP_OK) {
                if(printCode) System.out.println("Status code: " + statusCode + " for: " + path);
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
