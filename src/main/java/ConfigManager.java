import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 *
 * @author NanoDano <nanodano@devdungeon.com>
 */
public class ConfigManager {

    public static String discordBotToken;
    public static String discordChannelName;
    public static String discordServerName;
    public static String twitterToken;
    public static String hashTagsToMonitor;
    public static String subredditsToMonitor;
    public static String twitterOauthConsumerKey;
    public static String twitterOauthConsumerSecret;
    public static String twitterOauthAccessToken;
    public static String twitterOauthAccessTokenSecret;
    public static Integer secondsBetweenChecks;

    private static final Map<String, String> TEMPLATE_CONFIG = new HashMap() {
        {
            put("discordBotToken", "XXXXX");
            put("discordChannelName", "feed");
            put("discordServerName", "DevDungeon.com");
            put("twitterToken", "XXXXX");
            put("hashTagsToMonitor", "#infosec,#golang,#python");
            put("subredditsToMonitor", "learnprogramming,learnpython,learnjava,java,python,golang,programmerhumor");
            put("twitterOauthConsumerKey", "Get from Twitter https://apps.twitter.com/");
            put("twitterOauthConsumerSecret", "Get from Twitter https://apps.twitter.com/");
            put("twitterOauthAccessToken", "Get from Twitter https://apps.twitter.com/");
            put("twitterOauthAccessTokenSecret", "Get from Twitter https://apps.twitter.com/");
            put("secondsBetweenChecks", "60000");
        }
    };

    public static void writeTemplateConfigFile(String outputFilename) {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(outputFilename);
            TEMPLATE_CONFIG.entrySet().forEach((entry) -> {
                prop.setProperty(entry.getKey(), entry.getValue());
            });
            prop.store(output, outputFilename);
            System.out.println("[+] Generated template config file at: " + outputFilename);
            System.out.println("[*] Edit the file and fill in the correct values.");
        } catch (IOException io) {
            System.err.println("[-] Error trying to save sample config file: " + outputFilename);
            System.err.println("[-] " + io.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.err.println("[-] Error trying to save sample config file: " + outputFilename);
                    System.err.println("[-] " + e.getMessage());
                }
            }
        }
    }

    public static void loadConfigFile(String configFilename) throws FileNotFoundException, IOException {
        // load file, set properties
        Properties prop = new Properties();
        prop.load(new FileInputStream(configFilename));
                
        discordBotToken = prop.getProperty("discordBotToken");
        discordChannelName = prop.getProperty("discordChannelName");
        discordServerName = prop.getProperty("discordServerName");

        twitterOauthConsumerKey = prop.getProperty("twitterOauthConsumerKey");
        twitterOauthConsumerSecret = prop.getProperty("twitterOauthConsumerSecret");
        twitterOauthAccessToken = prop.getProperty("twitterOauthAccessToken");
        twitterOauthAccessTokenSecret = prop.getProperty("twitterOauthAccessTokenSecret");
        hashTagsToMonitor = prop.getProperty("hashTagsToMonitor");
        secondsBetweenChecks = Integer.parseInt(prop.getProperty("secondsBetweenChecks"));
    }

}
