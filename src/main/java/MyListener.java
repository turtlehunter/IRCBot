import org.pircbotx.Colors;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.output.OutputChannel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MyListener extends ListenerAdapter {
    public static OutputChannel outputChannel;
    public static PircBotX bot;

    public static ArrayList<String> autorized = new ArrayList<String>();
    public static HashMap<String, String> voxel = new HashMap<String, String>();
    private String botName = "UrielBot";

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        parse(event);
    }

    private void parse(GenericMessageEvent event) {
        if(event.getMessage().startsWith("??> ") && isAutorized(event.getUser())) {
            outputChannel.message(getVoxel1(event.getMessage()));
        } else if(event.getMessage().startsWith("?? ") && isAutorized(event.getUser())) {
            outputChannel.message(getVoxel2(event.getMessage()));
        } else if(event.getMessage().startsWith("??<") && isAutorized(event.getUser())) {
            event.getUser().send().message(getVoxel1(event.getMessage()));
        }
        if(event.getMessage().startsWith("$")) {
            if (isAutorized(event.getUser())) {
                if (event.getMessage().startsWith("$nick")) {
                    event.respond("Changing nick");
                    event.getBot().sendIRC().changeNick(event.getMessage().split(" ")[1]);
                    botName = event.getMessage().split(" ")[1];
                } else if (event.getMessage().startsWith("$msg")) {
                    outputChannel.message(getArray(event.getMessage()));
                } else if (event.getMessage().startsWith("$leave")) {
                    try {
                        save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    event.getBot().sendIRC().quitServer();
                } else if (event.getMessage().startsWith("$msguser")) {
                    event.getBot().sendIRC().message(getArray(event.getMessage()).split(" ")[0], getArray(getArray(event.getMessage())));
                } else if (event.getMessage().startsWith("$act")) {
                    event.getBot().sendIRC().action("#mchelptraining", getArray(event.getMessage()));
                } else if (event.getMessage().startsWith("$auth")) {
                    autorized.add(getArray(event.getMessage()).substring(0, getArray(event.getMessage()).length() - 1));
                    outputChannel.message("Authorized " + getArray(event.getMessage()) + "by " + event.getUser().getNick());
                    try {
                        save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (event.getMessage().startsWith("$help")) {
                    outputChannel.message("Commands: $nick, $msg, $leave, $act, $help, voxelhead commands. PangeaBot commands (some) : .dx, .help, .m");
                } else if (event.getMessage().startsWith("$getAuths")) {
                    outputChannel.message(getArray2());
                }
            }
        }
    }

    private static String getVoxel2(String message) {
        String str = message.substring(3);
        System.out.println(str);
        String[] strs = str.split(" ");
        if(strs.length > 2) {
            System.out.println(strs[1]);
            System.out.println(strs[0]);
            return getHash(strs[1]);
        } else {
            return getHash(str);
        }

    }

    private static String getVoxel1(String message) { //line input by user
        String str = message.substring(4);
        System.out.println(str);
        String[] strs = str.split(" ");
        System.out.println(strs[1]);
        System.out.println(strs[0]);
        return getHash(strs[1]);
    }

    private static String getHash(String str) { //looks for it in the hashmap
        for(Map.Entry<String, String> entry:voxel.entrySet()) {
            System.out.println(entry.getKey());
            if(str.equals(entry.getKey())) {

                return entry.getValue();

            }
        }
        return "Not found";
    }

    private static String getArray2() {
        if(autorized.size() > 1) {
            String str2 = "";
            for (String str : autorized) {
                str2 += str + ", ";
            }
            return str2.substring(0, str2.length() - 2);
        } else {
            return autorized.get(0);
        }
    }

    private static void save() throws IOException {
        ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream("save"));
        fileOut.writeObject(autorized);
        fileOut.close();


    }

    private static boolean isAutorized(User user) {
        for(String str:autorized) {
            if(user.getNick().equals(str)) return true;
        }
        return false;
    }

    private static String getArray(String message) {
        String[] strs = message.split(" ");
        strs = Arrays.copyOfRange(strs, 1, strs.length);
        String str2 = "";
        for (String str: strs) {
            str2 += str + " ";
        }
        return str2;
    }

    public static void main(String[] args) throws Exception {
        //Configure what we want our bot to do
        download();
        Configuration configuration = new Configuration.Builder()
                .setName("UrielBot") //Set the nick of the bot. CHANGE IN YOUR CODE
                .setLogin("boturielsalis")
                .setServerHostname("irc.esper.net") //Join the freenode network
                .addAutoJoinChannel("#mchelptraining") //Join the official #pircbotx channel
                .addListener(new MyListener()) //Add our listener that will be called on Events
                .buildConfiguration();

        //Create our bot with the configuration
        bot = new PircBotX(configuration);
        outputChannel = new OutputChannel(bot, bot.getUserChannelDao().getChannel("#mchelptraining"));
        autorized.add("urielsalis");
        //Connect to the server
        try {
            load();
        } catch (Exception ignored) {

        }
        bot.startBot();
        bot.sendIRC().identify("nopass");

    }

    private static void load() throws IOException, ClassNotFoundException {
        ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream("save"));
        autorized = (ArrayList<String>) fileIn.readObject();
        fileIn.close();

    }

    private static void download() { //download the list
        String generate_URL = "http://home.ghoti.me:8080/~faqbot/faqdatabase";
        String inputLine;
        try {
            URL data = new URL(generate_URL);

            HttpURLConnection con = (HttpURLConnection) data.openConnection();
            /* Read webpage coontent */
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            /* Read line by line */
            while ((inputLine = in.readLine()) != null) {
                process(inputLine);
            }
            /* close BufferedReader */
            in.close();
            /* close HttpURLConnection */
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void process(String line) {
        String[] f = line.split("\\|");
        voxel.put(f[0], f[1]);
    }
}
