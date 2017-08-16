package reporter;

import configfilesgetter.ConfigFilesGetter;

public class ConfigFileGetterReporter extends Reporter {

    private ConfigFilesGetter configFilesGetter;

    public ConfigFileGetterReporter(ConfigFilesGetter cfGetter) {
        this.configFilesGetter = cfGetter;
    }

    @Override
    public void report() {
        System.out.println("");
        System.out.println("--- ANALYZING CONFIG FILES IN REPOS ---");
        System.out.println("Total projects examined: " + configFilesGetter.totalExaminedProjects);
        System.out.println("");

        reportFiltering();
        reportLinterUsage();
        reportLinterUsageInTop();

        System.out.println("");
        System.out.println("");
    }

    private void reportFiltering() {
        System.out.println("Could not access repository API: " + configFilesGetter.couldNotAccessApi);
        System.out.println("Duplicate entries: " + configFilesGetter.duplicateProjectsPreFilter);
        System.out.println("Total left that were analyzed: " + configFilesGetter.projectsLeftAfterFiltering);
        System.out.println("");
    }

    private void reportLinterUsage() {
        int totalProjectsWithLinter = configFilesGetter.usedALinter;
        System.out.println("Used a linter total: " + totalProjectsWithLinter + " - "
                + getPercentage(totalProjectsWithLinter, configFilesGetter.projectsLeftAfterFiltering));
        System.out.println("Used ESLint: " + configFilesGetter.esLintConfigGetter.getConfigUrls().size() + " - "
                + getPercentage(configFilesGetter.esLintConfigGetter.getConfigUrls().size(), configFilesGetter.projectsLeftAfterFiltering));
        System.out.println("Used JSHint: " + configFilesGetter.jsHintConfigGetter.getConfigUrls().size());
        System.out.println("Used JSCS: " + configFilesGetter.jscsConfigGetter.getConfigUrls().size());
        System.out.println("Used Standard: " + configFilesGetter.standardConfigGetter.getConfigUrls().size());
        System.out.println("Used 2 linters: " + configFilesGetter.usedTwoLinters + " - " +
                getPercentage(configFilesGetter.usedTwoLinters, configFilesGetter.usedALinter) + " of those who use a linter");
        System.out.println("Used 3 linters: " + configFilesGetter.usedThreeLinters + " - " +
                getPercentage(configFilesGetter.usedThreeLinters, configFilesGetter.usedALinter) + " of those who use a linter");
        System.out.println("Used 4 linters: " + configFilesGetter.usedFourLinters + " - " +
                getPercentage(configFilesGetter.usedFourLinters, configFilesGetter.usedALinter) + " of those who use a linter");
        System.out.println("Used ESLint and another linter: " + configFilesGetter.usedESLintAndOther);
    }

    private void reportLinterUsageInTop() {
        if(configFilesGetter.usedLinterInTop10 > 0) {
            int num = configFilesGetter.usedLinterInTop10;
            System.out.println("Used a linter in top 10: " + num + " - " + getPercentage(num, 10));
        }
        if(configFilesGetter.usedLinterInTop50 > 0) {
            int num = configFilesGetter.usedLinterInTop50;
            System.out.println("Used a linter in top 50: " + num + " - " + getPercentage(num, 50));
        }
        if(configFilesGetter.usedLinterInTop100 > 0) {
            int num = configFilesGetter.usedLinterInTop100;
            System.out.println("Used a linter in top 100: " + num + " - " + getPercentage(num, 100));
        }
        if(configFilesGetter.usedLinterInTop300 > 0) {
            int num = configFilesGetter.usedLinterInTop300;
            System.out.println("Used a linter in top 300: " + num + " - " + getPercentage(num, 300));
        }
        if(configFilesGetter.usedLinterInTop1000 > 0) {
            int num = configFilesGetter.usedLinterInTop1000;
            System.out.println("Used a linter in top 1000: " + num + " - " + getPercentage(num, 1000));
        }
        if(configFilesGetter.usedLinterInTop3000 > 0) {
            int num = configFilesGetter.usedLinterInTop3000;
            System.out.println("Used a linter in top 3000: " + num + " - " + getPercentage(num, 3000));
        }
        if(configFilesGetter.usedLinterInTop5000 > 0) {
            int num = configFilesGetter.usedLinterInTop5000;
            System.out.println("Used a linter in top 5000: " + num + " - " + getPercentage(num, 5000));
        }
        if(configFilesGetter.usedLinterInTop10000 > 0) {
            int num = configFilesGetter.usedLinterInTop10000;
            System.out.println("Used a linter in top 10.000: " + num + " - " + getPercentage(num, 10000));
        }
        if(configFilesGetter.usedLinterInTop20000 > 0) {
            int num = configFilesGetter.usedLinterInTop20000;
            System.out.println("Used a linter in top 20.000: " + num + " - " + getPercentage(num, 20000));
        }
        if(configFilesGetter.usedLinterInTop30000 > 0) {
            int num = configFilesGetter.usedLinterInTop30000;
            System.out.println("Used a linter in top 30.000: " + num + " - " + getPercentage(num, 30000));
        }
    }
}
