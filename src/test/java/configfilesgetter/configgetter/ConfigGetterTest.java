package configfilesgetter.configgetter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigGetterTest {
    ConfigGetter eslintConfigGetter;
    ConfigGetter jshintConfigGetter;
    ConfigGetter jscsConfigGetter;
    ConfigGetter standardConfigGetter;

    @Before
    public void setUp() throws Exception {
        eslintConfigGetter = new ESLintConfigGetter();
        jshintConfigGetter = new JSHintConfigGetter();
        jscsConfigGetter = new JSCSConfigGetter();
        standardConfigGetter = new StandardConfigGetter();
    }

    @Test
    public void testIsUsingESLintTrueConfigFile() {
        String url = "https://raw.githubusercontent.com/facebook/react/17ab69c1ec9e2d994bb58955214c463df3b377cb/";
        boolean isUsing = eslintConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingESLintTruePackageFile() {
        String url = "https://raw.githubusercontent.com/postcss/postcss/73fb5ebf62adbf537d1581bfe679b6ede4594b4c/";
        boolean isUsing = eslintConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingJSHintTrueConfigFile() {
        String url = "https://raw.githubusercontent.com/moment/moment/b8a7fc310eb3625e83fc0c8f1ea2840fa83c7378/";
        boolean isUsing = jshintConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingJSHintTruePackageFile() {
        String url = "https://raw.githubusercontent.com/apidoc/apidoc/940db623fc0b9b8cab26ebf5ea6e05d5be83c7cf/";
        boolean isUsing = jshintConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingJSCSTrueConfigFile() {
        String url = "https://raw.githubusercontent.com/moment/moment/b8a7fc310eb3625e83fc0c8f1ea2840fa83c7378/";
        boolean isUsing = jscsConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingJSCSTruePackageFile() {
        String url = "https://raw.githubusercontent.com/jonathantneal/postcss-write-svg/6f046936d9faec3b8cf9d8ca69b3a0901b757c42/";
        boolean isUsing = jscsConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingStandardTrue() {
        String url = "https://raw.githubusercontent.com/request/request/e8fca511ba2800a809c7759a1c09ea33a440e4fb/";
        boolean isUsing = standardConfigGetter.isUsingLinter(url);
        assertTrue(isUsing);
    }

    @Test
    public void testIsUsingAllFalse() {
        String url = "https://raw.githubusercontent.com/resume/resume.github.com/6ca53fd9de25f25fc6498b75003462de5a539ec5/";
        boolean isUsing1 = eslintConfigGetter.isUsingLinter(url);
        boolean isUsing2 = jshintConfigGetter.isUsingLinter(url);
        boolean isUsing3 = jscsConfigGetter.isUsingLinter(url);
        boolean isUsing4 = standardConfigGetter.isUsingLinter(url);
        assertFalse(isUsing1);
        assertFalse(isUsing2);
        assertFalse(isUsing3);
        assertFalse(isUsing4);
    }

}