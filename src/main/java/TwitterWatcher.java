
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sx.blah.discord.util.RateLimitException;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author NanoDano <nanodano@devdungeon.com>
 */
public class TwitterWatcher implements Runnable {

    private final DiscordBot discordBot;
    private final Twitter twitterClient;
    private final String hashTagsToMonitor;
    private String queryString;
    private final Integer secondsBetweenChecks;
    private final List<Long> knownTweetIds = new ArrayList<>();
    private final Query query = new Query();

    public TwitterWatcher(
            DiscordBot discordBot,
            String twitterOauthConsumerKey,
            String twitterOauthConsumerSecret,
            String twitterOauthAccessToken,
            String twitterOauthAccessTokenSecret,
            String hashTagsToMonitor,
            Integer secondsBetweenChecks) throws TwitterException {
        System.out.println("[*] Initializing Twitter watcher...");
        this.discordBot = discordBot;
        this.hashTagsToMonitor = hashTagsToMonitor;
        this.secondsBetweenChecks = secondsBetweenChecks;
        createSearchQuery();
        query.setQuery(queryString);

        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(twitterOauthConsumerKey)
                .setOAuthConsumerSecret(twitterOauthConsumerSecret)
                .setOAuthAccessToken(twitterOauthAccessToken)
                .setOAuthAccessTokenSecret(twitterOauthAccessTokenSecret);

        this.twitterClient = new TwitterFactory(configBuilder.build()).getInstance();

        // TODO periodically purge old tweets so it doesn't grow forever
        System.out.println("[+] Connected to Twitter as: " + twitterClient.getScreenName());
    }

    private void createSearchQuery() {
        //queryString = Arrays.stream(hashTagsToMonitor.split(",")).collect(Collectors.joining(" OR "));
        queryString = String.join(" OR ", hashTagsToMonitor.split(","));
        queryString += " +exclude:retweets";
        System.out.println("[*] Generating Twitter query: " + queryString);
    }

    private void checkTweets() {
        int tweetsPostedCount;
        List<Status> latestTweets;
        try {
            System.out.print("[*] Checking for new tweets... ");
            latestTweets = twitterClient.search(query).getTweets();

            // Determine which tweets are new
            List<Status> newTweets = latestTweets.stream()
                    .filter(tweet -> !knownTweetIds.contains(tweet.getId()))
                    .collect(Collectors.toList());

            tweetsPostedCount = 0;
            if (newTweets.size() > 0) {
                System.out.println(newTweets.size() + " new tweets found.");
                for (Status tweet : newTweets) {
                    knownTweetIds.add(tweet.getId());
                    // aggregate tweet data to send one message
                    if (tweetsPostedCount < 4) {
                        String url = "https://twitter.com/" + tweet.getUser().getScreenName() + "/status/" + tweet.getId();
                        discordBot.sendMessage(url);
                        tweetsPostedCount++;
                    } else {
                        System.out.println("[*] Too many tweets found. Skipping rest of tweets.");
                        //break; // Don't blast the Discord API, it will reject messages anyway
                    }
                }
            } else {
                System.out.println("no new tweets found.");
            }
        } catch (TwitterException ex) {
            System.err.println("[-] Error fetching Twitter feed: " + ex.getMessage());
        } catch (RateLimitException ex) {
            System.err.println("[-] Error sending tweets to Discord. " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            checkTweets();
            try {
                System.out.println("[*] Sleeping for " + secondsBetweenChecks.toString() + " seconds.");
                Thread.sleep(secondsBetweenChecks);
            } catch (InterruptedException ex) {
                System.out.println("[*] TwitterWatcher sleep interrupted. " + ex.getMessage());
            }
        }
    }

}
