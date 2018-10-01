import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author NanoDano <nanodano@devdungeon.com>
 */
public class DiscordBot implements Runnable {

    private final String designatedChannelName;
    private final IDiscordClient client;
    private IChannel designatedChannel = null;

    public DiscordBot(String botToken, String primaryChannelName, String primaryServerName) {
        client = createClient(botToken);
        designatedChannelName = primaryChannelName;
        
        
        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void handle(ReadyEvent readyEvent) {
                try {
                    System.out.println("[+] Discord bot connected as: " + client.getApplicationName());
                } catch (Exception e) {
                    System.err.println("[-] Error during Discord bot ready event.");
                    System.err.println("[-] " + e.getMessage());
                }

                Boolean serverFound = false;
                Boolean channelFound = false;
                System.out.println("[*] Guilds:");
                for (IGuild guild : client.getGuilds()) {
                    System.out.println("[*] - " + guild.getName());
                    if (guild.getName().equals(primaryServerName)) {
                        System.out.println("[*] Found the specified Discord server.");
                        serverFound = true;
                        for (IChannel channel : guild.getChannelsByName(designatedChannelName)) {
                            System.out.println("[*] Found the #feed channel. Storing.");
                            channelFound = true;
                            designatedChannel = channel;
                            break;
                        }
                    }
                }
                if (!serverFound || !channelFound) {
                    System.err.println("[-] Unable to find designated server and channel: " + primaryServerName + " #" + designatedChannelName);
                    System.exit(1);
                }
            }
        });
    }

    public Boolean isReady() {
        return client.isReady() && designatedChannel != null;
    }

    private static IDiscordClient createClient(String token) {
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);

        try {
            return clientBuilder.login();
        } catch (DiscordException e) {
            System.err.println("[-] Error connecting to Discord.");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public final void sendMessage(String message) throws RateLimitException {
        designatedChannel.sendMessage(message);
    }

    @Override
    public void run() {
        System.out.println("[*} Running Discord bot thread.");
    }

}
