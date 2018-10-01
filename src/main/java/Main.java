import java.io.IOException;
import twitter4j.TwitterException;

public class Main {

    public static void main(String[] args) {
        // Help message/usage
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            printUsage();
            System.exit(0);
        }

        // Generate a template config file
        if (args[0].equals("--generate-config")) {
            if (args.length < 2) {
                System.err.println("[-] Error: Not enough arguments.");
                System.exit(1);
            } else {
                ConfigManager.writeTemplateConfigFile(args[1]);
                System.exit(0);
            }
        }

        // Load configuration
        try {
            ConfigManager.loadConfigFile(args[0]);
        } catch (IOException ex) {
            System.err.println("[-] Error loading file: " + args[0]);
            System.err.println("[-] " + ex.getMessage());
            System.exit(1);
        }

        // Initialize and connect Discord discordBot
        DiscordBot discordBot = new DiscordBot(ConfigManager.discordBotToken, ConfigManager.discordChannelName, ConfigManager.discordServerName);
        new Thread(discordBot).start();
        System.out.println("[*] Waiting for Discord bot to be ready...");
        while (!discordBot.isReady()) {
        }
        System.out.println("[+] Discord bot is now ready.");

        // Initialize Twitter monitor
        TwitterWatcher twitterWatcher;
        try {
            twitterWatcher = new TwitterWatcher(discordBot, ConfigManager.twitterOauthConsumerKey,
                    ConfigManager.twitterOauthConsumerSecret, ConfigManager.twitterOauthAccessToken, ConfigManager.twitterOauthAccessTokenSecret,
                    ConfigManager.hashTagsToMonitor, ConfigManager.secondsBetweenChecks);
            new Thread(twitterWatcher).start();
        } catch (TwitterException ex) {
            System.err.println("[-] Error connecting to Twitter. " + ex.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  # Print this help menu");
        System.out.println("  discordfeedwatcher [-h|--help]");
        System.out.println("");
        System.out.println("  # Generate a sample configuration file");
        System.out.println("  discordfeedwatcher --generate-config <filename>");
        System.out.println("");
        System.out.println("  # Run the tool");
        System.out.println("  discordfeedwatcher <config-filename>");
    }

}
