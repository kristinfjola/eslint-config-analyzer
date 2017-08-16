import configfilesgetter.ConfigFilesGetter;
import parser.ConfigFileParser;
import reporter.ConfigFileGetterReporter;
import reporter.ConfigParserReporter;
import reporter.Reporter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Main {
    public static void main(String[] args) {
        String PROJECT_INFO_FILE_PATH = "/javascript-projects-86366.txt";

        ConfigFilesGetter configFilesGetter = new ConfigFilesGetter();
        Set<String> configURLs = configFilesGetter.writeOutProjectURLs(PROJECT_INFO_FILE_PATH);
        Reporter configFilesReporter = new ConfigFileGetterReporter(configFilesGetter);
        configFilesReporter.report();

        ConfigFileParser configFileParser = new ConfigFileParser(configURLs);
        configFileParser.parseConfigFiles();
        Reporter configParserReporter = new ConfigParserReporter(configFileParser);
        configParserReporter.report();
    }
}
