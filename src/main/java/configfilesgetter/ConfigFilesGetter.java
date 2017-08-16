package configfilesgetter;

import configfilesgetter.configgetter.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConfigFilesGetter {
    private String CONFIG_URL_FILE_PATH = "src/main/resources/config-urls.txt";
    private String RAW_URL_START = "https://raw.githubusercontent.com/";
    private String BRANCH = "/master/";

    public ConfigGetter esLintConfigGetter = new ESLintConfigGetter();
    public ConfigGetter jsHintConfigGetter = new JSHintConfigGetter();
    public ConfigGetter jscsConfigGetter = new JSCSConfigGetter();
    public ConfigGetter standardConfigGetter = new StandardConfigGetter();

    // for reporting
    public int totalExaminedProjects = 0;
    public int projectsLeftAfterFiltering = 0;
    public int usedALinter = 0;
    public int usedTwoLinters = 0;
    public int usedThreeLinters = 0;
    public int usedFourLinters = 0;
    public int usedESLintAndOther = 0;
    public int couldNotAccessApi = 0;
    public int duplicateProjectsPreFilter = 0;
    public int usedLinterInTop10 = 0;
    public int usedLinterInTop50 = 0;
    public int usedLinterInTop100 = 0;
    public int usedLinterInTop300 = 0;
    public int usedLinterInTop1000 = 0;
    public int usedLinterInTop3000 = 0;
    public int usedLinterInTop5000 = 0;
    public int usedLinterInTop10000 = 0;
    public int usedLinterInTop20000 = 0;
    public int usedLinterInTop30000 = 0;

    long startTime = 0;

    public ConfigFilesGetter() {}

    public Set<String> writeOutProjectURLs(String PROJECT_INFO_FILE_PATH) {
        startTime = System.nanoTime();
        int projectsAnalyzed = 0;
        Set<JSONObject> allProjects = new HashSet<>();

        // read to list
        try {
            InputStream in = getClass().getResourceAsStream(PROJECT_INFO_FILE_PATH);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject projectInfo = new JSONObject(line);
                allProjects.add(projectInfo);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // analyze projects
        try {
            List<JSONObject> projects = removeDuplicates(allProjects);
            projects = removeDeletedProjects(projects);
            projects = sortProjects(projects);

            System.out.println("Starting to analyze " + projects.size() + " projects");
            for(JSONObject projectInfo : projects) {
                if(projectsAnalyzed % 100 == 0) {
                    long timeElapsed = (System.nanoTime() - startTime)/1000000000;
                    System.out.println("Analyzed: " + projectsAnalyzed);
                    System.out.println("Seconds elapsed: " + timeElapsed);
                }

                boolean foundConfiguration = checkProject(projectInfo);
                if(foundConfiguration) usedALinter++;
                setUsageNumbers();

                projectsAnalyzed++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeOutURLs();

        long totalTimeSeconds = (long) ((System.nanoTime() - startTime)/1000000000.0);
        System.out.println("");
        System.out.println("Total seconds to find configuration files: " + totalTimeSeconds);

        return esLintConfigGetter.getConfigUrls();
    }

    private void setUsageNumbers() {
        switch (projectsLeftAfterFiltering) {
            case 10: usedLinterInTop10 = usedALinter;
                break;
            case 50: usedLinterInTop50 = usedALinter;
                break;
            case 100: usedLinterInTop100 = usedALinter;
                break;
            case 300: usedLinterInTop300 = usedALinter;
                break;
            case 1000: usedLinterInTop1000 = usedALinter;
                break;
            case 3000: usedLinterInTop3000 = usedALinter;
                break;
            case 5000: usedLinterInTop5000 = usedALinter;
                break;
            case 10000: usedLinterInTop10000 = usedALinter;
                break;
            case 20000: usedLinterInTop20000 = usedALinter;
                break;
            case 30000: usedLinterInTop30000 = usedALinter;
                break;
            default: break;
        }
    }

    /*
        Returns true if there was some configuration file
     */
    protected boolean checkProject(JSONObject projectInfo) throws IOException, JSONException {
        String apiURL = projectInfo.getString("p_url");
        try {
            String baseConfigUrl = getBaseConfigUrl(apiURL);
            projectsLeftAfterFiltering++;
            return isUsingLinter(baseConfigUrl);
        } catch (Exception e) {
            couldNotAccessApi++;
            System.out.println("Could not check project: " + apiURL);
            e.printStackTrace();
        }

        return false;
    }

    private List<JSONObject> removeDuplicates(Set<JSONObject> projects) throws JSONException {
        Map<String, JSONObject> projectsToAnalyze = new HashMap<>(); // <apiURL, projectInfo>

        for(JSONObject projectInfo : projects) {
            String apiURL = projectInfo.getString("p_url");
            Long timeStamp = Double.valueOf(projectInfo.getString("c_created_at")).longValue();

            if(!projectsToAnalyze.keySet().contains(apiURL)) {
                projectsToAnalyze.put(apiURL, projectInfo);
            } else {
                Long analyzedTimeStamp = Double.valueOf(projectsToAnalyze.get(apiURL).getString("c_created_at")).longValue();
                if(timeStamp > analyzedTimeStamp) {
                    projectsToAnalyze.put(apiURL, projectInfo);
                }
                duplicateProjectsPreFilter++;
            }
        }

        List<JSONObject> projectsWithoutDuplicates = new ArrayList<JSONObject>(projectsToAnalyze.values());
        return projectsWithoutDuplicates;
    }

    private List<JSONObject> removeDeletedProjects(List<JSONObject> projects) throws JSONException {
        System.out.println("Starting to remove deleted projects");
        int analyzed = 0;
        List<JSONObject> deletedProjects = new ArrayList<>();
        for(JSONObject projectInfo: projects) {
            if(analyzed % 100 == 0) {
                long timeElapsed = (System.nanoTime() - startTime)/1000000000;
                System.out.println("Seconds elapsed: " + timeElapsed);
                System.out.println("Checked deleted: " + analyzed);
            }
            analyzed++;

            String apiURL = projectInfo.getString("p_url");
            String gitHubPath = "https://github.com/" + getProjectPath(apiURL);
            if(!ConfigGetterUtils.urlExists(gitHubPath, true)) {
                deletedProjects.add(projectInfo);
                couldNotAccessApi++;
            }
        }
        projects.removeAll(deletedProjects);
        return projects;
    }

    private String getBaseConfigUrl(String apiURL) {
        String projectPath = getProjectPath(apiURL);
        String projectUrl = RAW_URL_START + projectPath + BRANCH;
        return projectUrl;
    }

    private String getProjectPath(String apiURL) {
        String sub = "/repos/";
        String projectPath = apiURL.substring(apiURL.indexOf(sub) + sub.length());
        return projectPath;
    }

    private boolean isUsingLinter(String baseConfigUrl) {
        int nrOfLintersUsed = 0;

        // ESLint
        boolean usesESLint = false;
        if(esLintConfigGetter.isUsingLinter(baseConfigUrl)) {
            nrOfLintersUsed++;
            usesESLint = true;
        }

        // JSHint
        if(jsHintConfigGetter.isUsingLinter(baseConfigUrl)) {
            nrOfLintersUsed++;
        }

        // JSCS
        if(jscsConfigGetter.isUsingLinter(baseConfigUrl)) {
            nrOfLintersUsed++;
        }

        // Standard
        if(standardConfigGetter.isUsingLinter(baseConfigUrl)) {
            nrOfLintersUsed++;
        }

        if(usesESLint && nrOfLintersUsed > 1) {
            usedESLintAndOther++;
        }

        usedTwoLinters = nrOfLintersUsed == 2 ? usedTwoLinters + 1 : usedTwoLinters;
        usedThreeLinters = nrOfLintersUsed == 3 ? usedThreeLinters + 1 : usedThreeLinters;
        usedFourLinters = nrOfLintersUsed == 4 ? usedFourLinters + 1 : usedFourLinters;

        if(nrOfLintersUsed == 3) System.out.println("Used 3 linters: " + baseConfigUrl);
        if(nrOfLintersUsed == 4) System.out.println("Used 4 linters: " + baseConfigUrl);

        return nrOfLintersUsed >= 1;
    }

    private void writeOutURLs() {
        try{
            PrintWriter writer = new PrintWriter(CONFIG_URL_FILE_PATH, "UTF-8");
            for(String url : esLintConfigGetter.getConfigUrls()) {
                writer.println(url);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<JSONObject> sortProjects(List<JSONObject> projects) {
        Collections.sort(projects, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    int stars1 = Integer.parseInt(o1.getString("stars"));
                    int stars2 = Integer.parseInt(o2.getString("stars"));
                    return (stars1 > stars2 ? -1 : (stars1 == stars2 ? 0 : 1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        return projects;
    }
}
