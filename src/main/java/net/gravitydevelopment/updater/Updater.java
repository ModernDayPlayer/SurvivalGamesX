/*
 * Updater for Bukkit.
 *
 * This class provides the means to safely and easily update a plugin, or check to see if it is updated using dev.bukkit.org
 */

package net.gravitydevelopment.updater;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.shockverse.survivalgames.core.Logger;
import net.shockverse.survivalgames.core.Tools;
import org.bukkit.Bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Check dev.bukkit.org to find updates for a given plugin, and download the updates if needed.
 * <p/>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating.
 * <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * <p/>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false you may NOT run the auto-updater.
 * <br>
 * If you are unsure about these rules, please read the plugin submission guidelines: http://goo.gl/8iU5l
 *
 * @author Gravity
 * @version 2.0
 */

public class Updater {

    private Plugin plugin;
    private UpdateType type;
    private ChannelType channel;
    private UpdateInfo updateInfo;
    
    private boolean announce; // Whether to announce file downloads
    private boolean specialBuild;

    private URL url; // Connecting to RSS
    private File file; // The plugin's file
    private Thread thread; // Updater thread

    private int id = -1; // Project's Curse ID
    private String apiKey = null; // BukkitDev ServerMods API key
    private static final String TITLE_VALUE = "name"; // Gets remote file's title
    private static final String LINK_VALUE = "downloadUrl"; // Gets remote file's download link
    private static final String TYPE_VALUE = "releaseType"; // Gets remote file's release type
    private static final String VERSION_VALUE = "gameVersion"; // Gets remote file's build version
    private static final String QUERY = "/servermods/files?projectIds="; // Path to GET
    private static final String HOST = "https://api.curseforge.com"; // Slugs will be appended to this to get to the project's RSS feed

    private static final int BYTE_SIZE = 1024; // Used for downloading files
    private YamlConfiguration config; // Config file
    private String updateFolder;// The folder that downloads will be placed in
    private UpdateResult result = UpdateResult.SUCCESS; // Used for determining the outcome of the update process
    
    public class UpdateInfo {
        
        private String name;
        private String downloadLink;
        private String releaseType;
        private String pluginVersion;
        private String gameVersion;
        private ChannelType channel;
        
        /**
         * A file that stores the information received from the curse API to be used when updating.
         * @param title The title for the file.
         * @param link The link to download the file.
         * @param type The type of release for the file.
         * @param gameVersion The version of MineCraft the plug-in is made for.
         */
        public UpdateInfo(String name, String downloadLink, String releaseType, String pluginVersion, String gameVersion, ChannelType channel) {
            this.name = name;
            this.downloadLink = downloadLink;
            this.releaseType = releaseType;
            this.pluginVersion = pluginVersion;
            this.gameVersion = gameVersion;
            this.channel = channel;
        }
        
        /**
         * Returns the name of the file without the version.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the download link for the file.
         */
        public String getDownloadLink() {
            return downloadLink;
        }
        
        /**
         * Returns the release type for the file.
         */
        public String getReleaseType() {
            return releaseType;
        }
        
        /**
         * Returns the version of the plug-in.
         */
        public String getPluginVersion() {
            return pluginVersion;
        }
        
        /**
         * Returns the version of MineCraft the plug-in was made for.
         */
        public String getGameVersion() {
            return gameVersion;
        }
        
        /**
         * Returns the channel for the update file.
         */
        public ChannelType getChannel() {
            return channel;
        }
    }

    /**
     * Gives the dev the result of the update process. Can be obtained by called getResult().
     */
    public enum UpdateResult {
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        SUCCESS,
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE,
        /**
         * The server administrator has disabled the updating system
         */
        DISABLED,
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD,
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO,
        /**
         * When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.
         */
        FAIL_NOVERSION,
        /**
         * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
         */
        FAIL_BADID,
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY,
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE,
        /**
         * The current plugin version does not exist on bukkit.
         */
        SPECIAL_BUILD
    }

    /**
     * Allows the dev to specify the type of update that will be run.
     */
    public enum UpdateType {
        /**
         * Run a version check, and then if the file is out of date, download the newest version.
         */
        DEFAULT,
        /**
         * Don't run a version check, just find the latest update and download it.
         */
        NO_VERSION_CHECK,
        /**
         * Get information about the version and the download size, but don't actually download anything.
         */
        NO_DOWNLOAD
    }
    
    /**
     * Allows the user to use a specific channel.
     */
    public enum ChannelType {
        RELEASE("", 0), BETA("-Beta", 1), ALPHA("-Alpha", 2);
        
        private String _tag;
        private int _id;
        
        private ChannelType(String tag, int id) {
            _tag = tag;
            _id = id;
        }
        
        public String getTag() {
            return _tag;
        }
        
        public int getId() {
            return _id;
        }
        
        public static ChannelType getFromVersion(String version) {
            for(ChannelType type : ChannelType.values()) {
                if(version.contains(type.getTag()))
                    return type;
            }
            return ChannelType.RELEASE;
        }
        
        public static ChannelType getType(String name) {
            try {
                return Enum.valueOf(ChannelType.class, name);
            } catch(Exception ex){}
            return ChannelType.RELEASE;
        }
        
    }

    /**
     * Initialize the updater
     *
     * @param plugin   The plugin that is checking for an update.
     * @param id       The dev.bukkit.org id of the project
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main class.
     * @param type     Specify the type of update this will be. See {@link net.gravitydevelopment.updater.Updater.UpdateType}
     * @param channel  Specifies what channel the user wants to use.
     * @param announce True if the program should announce the progress of new updates in console
     */
    public Updater(Plugin plugin, int id, File file, UpdateType type, ChannelType channel, boolean announce) {
        this.plugin = plugin;
        this.type = type;
        this.channel = channel;
        this.announce = announce;
        this.file = file;
        this.id = id;
        this.updateFolder = plugin.getServer().getUpdateFolder();

        try {
            this.url = new URL(Updater.HOST + Updater.QUERY + id);
        } catch (final MalformedURLException e) {
            Logger.error("The project ID provided for updating, " + id + " is invalid.");
            this.result = UpdateResult.FAIL_BADID;
            e.printStackTrace();
        }

        this.thread = new Thread(new UpdateRunnable());
        this.thread.start();
    }

    /**
     * Get the result of the update process.
     */
    public UpdateResult getResult() {
        this.waitForThread();
        return this.result;
    }

    /**
     * Get the latest version's release type (release, beta, or alpha).
     */
    public String getLatestType() {
        this.waitForThread();
        return this.updateInfo.getReleaseType();
    }

    /**
     * Get the latest version's game version.
     */
    public String getLatestGameVersion() {
        this.waitForThread();
        return this.updateInfo.gameVersion;
    }

    /**
     * Get the latest version's name.
     */
    public String getLatestName() {
        this.waitForThread();
        return this.updateInfo.name + " v" + this.updateInfo.pluginVersion;
    }

    /**
     * Get the latest version's file link.
     */
    public String getLatestFileLink() {
        this.waitForThread();
        return this.updateInfo.downloadLink;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
     * before allowing anyone to check the result.
     */
    private void waitForThread() {
        if ((this.thread != null) && this.thread.isAlive()) {
            try {
                this.thread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save an update from dev.bukkit.org into the server's update folder.
     */
    private void saveFile(File folder, String file, String u) {
        if (!folder.exists()) {
            folder.mkdir();
        }
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            // Download the file
            final URL url = new URL(u);
            final int fileLength = url.openConnection().getContentLength();
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file);

            final byte[] data = new byte[Updater.BYTE_SIZE];
            int count;
            if (this.announce) {
                Logger.info("Found a new " + updateInfo.getChannel().name().toLowerCase() + " update: " + this.updateInfo.pluginVersion);
            }
            
            long downloaded = 0;
            // Show a message every half a second. Delay the first message.
            long lastPercentAnounce = System.currentTimeMillis() + 500;
            while ((count = in.read(data, 0, Updater.BYTE_SIZE)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                final double percent = ((downloaded * 100) / fileLength);
                if (this.announce && lastPercentAnounce + 500 < System.currentTimeMillis()) {
                    // Update the last percent announce.
                    lastPercentAnounce = System.currentTimeMillis();
                    Logger.info("    Downloading update: " + Tools.round(percent, 1) + "% of " + fileLength + " bytes.");
                }
            }
            Logger.info("    Downloading update: 100% of " + fileLength + " bytes.");
            //Just a quick check to make sure we didn't leave any files from last time...
            for (final File xFile : new File(this.plugin.getDataFolder().getParent(), this.updateFolder).listFiles()) {
                if (xFile.getName().endsWith(".zip")) {
                    xFile.delete();
                }
            }
            // Check to see if it's a zip file, if it is, unzip it.
            final File dFile = new File(folder.getAbsolutePath() + "/" + file);
            if (dFile.getName().endsWith(".zip")) {
                // Unzip
                this.unzip(dFile.getCanonicalPath());
            }
        } catch (final Exception ex) {
            Logger.warning("The auto-updater tried to download a new update, but was unsuccessful.");
            this.result = UpdateResult.FAIL_DOWNLOAD;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (final Exception ex) {
            }
        }
    }

    /**
     * Part of Zip-File-Extractor, modified by Gravity for use with Bukkit
     */
    private void unzip(String file) {
        try {
            final File fSourceZip = new File(file);
            final String zipPath = file.substring(0, file.length() - 4);
            ZipFile zipFile = new ZipFile(fSourceZip);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    final byte buffer[] = new byte[Updater.BYTE_SIZE];
                    final FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos, Updater.BYTE_SIZE);
                    while ((b = bis.read(buffer, 0, Updater.BYTE_SIZE)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    final String name = destinationFilePath.getName();
                    if (name.endsWith(".jar") && this.pluginFile(name)) {
                        destinationFilePath.renameTo(new File(this.plugin.getDataFolder().getParent(), this.updateFolder + "/" + name));
                    }
                }
                entry = null;
                destinationFilePath = null;
            }
            e = null;
            zipFile.close();
            zipFile = null;

            // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
            for (final File dFile : new File(zipPath).listFiles()) {
                if (dFile.isDirectory()) {
                    if (this.pluginFile(dFile.getName())) {
                        final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName()); // Get current dir
                        final File[] contents = oFile.listFiles(); // List of existing files in the current dir
                        for (final File cFile : dFile.listFiles()) // Loop through all the files in the new dir
                        {
                            boolean found = false;
                            for (final File xFile : contents) // Loop through contents to see if it exists
                            {
                                if (xFile.getName().equals(cFile.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Move the new file into the current dir
                                cFile.renameTo(new File(oFile.getCanonicalFile() + "/" + cFile.getName()));
                            } else {
                                // This file already exists, so we don't need it anymore.
                                cFile.delete();
                            }
                        }
                    }
                }
                dFile.delete();
            }
            new File(zipPath).delete();
            fSourceZip.delete();
        } catch (final IOException ex) {
            Logger.warning("The auto-updater tried to unzip a new update file, but was unsuccessful.");
            this.result = UpdateResult.FAIL_DOWNLOAD;
            ex.printStackTrace();
        }
        new File(file).delete();
    }

    /**
     * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out of a zip.
     */
    private boolean pluginFile(String name) {
        for (final File file : new File("plugins").listFiles()) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the program should continue by evaluation whether the plugin is already updated, or shouldn't be updated
     */
    private boolean versionCheck() {
        if (this.type != UpdateType.NO_VERSION_CHECK) {
            final String version = this.plugin.getDescription().getVersion();

            if (version.equalsIgnoreCase(updateInfo.pluginVersion)) {
                // We already have the latest version, or this build is tagged for no-update
                this.result = UpdateResult.NO_UPDATE;
                return false;
            }
        }
        return true;
    }

    private boolean read() {
        try {
            final URLConnection conn = this.url.openConnection();
            conn.setConnectTimeout(5000);

            if (this.apiKey != null) {
                conn.addRequestProperty("X-API-Key", this.apiKey);
            }
            conn.addRequestProperty("User-Agent", "Updater (by Gravity)");

            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();

            final JSONArray array = (JSONArray) JSONValue.parse(response);

            if (array.size() == 0) {
                Logger.warning("The updater could not find any files for the project id " + this.id);
                this.result = UpdateResult.FAIL_BADID;
                return false;
            }
            
            List<String> versions = new ArrayList<String>();
            
            for(Object json : (JSONArray) array) {
                String title = (String) ((JSONObject) json).get(Updater.TITLE_VALUE);
                String[] titleSplit = title.split(" v");
                if(titleSplit.length == 2) {
                    versions.add(titleSplit[1]);
                }
            }
            
            if(!versions.contains(plugin.getDescription().getVersion())) {
                this.result = UpdateResult.SPECIAL_BUILD;
                return false;
            }
            
            for(int i = array.size() - 1; i >= 0; i--) {
                String title = (String) ((JSONObject) array.get(i)).get(Updater.TITLE_VALUE);
                String[] titleSplit = title.split(" v");
                if(titleSplit.length == 2) {
                    
                    String name = titleSplit[0];
                    String downloadLink = (String) ((JSONObject) array.get(i)).get(Updater.LINK_VALUE);
                    String releaseType = (String) ((JSONObject) array.get(i)).get(Updater.TYPE_VALUE);
                    String gameVersion = (String) ((JSONObject) array.get(i)).get(Updater.VERSION_VALUE);
                    String pluginVersion = titleSplit[1].trim();
                    
                    ChannelType channelType = ChannelType.getFromVersion(pluginVersion);
                    if(channel.getId() >= channelType.getId()) {
                        updateInfo = new UpdateInfo(name, downloadLink, releaseType, pluginVersion, gameVersion, channelType);
                        break;
                    }
                    
                } else {
                    Logger.warning(title + " is not in a correct format.");
                }
            }

            return true;
        } catch (final IOException e) {
            if (e.getMessage().contains("HTTP response code: 403")) {
                Logger.warning("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
                Logger.warning("Please double-check your configuration to ensure it is correct.");
                this.result = UpdateResult.FAIL_APIKEY;
            } else {
                Logger.warning("The updater could not contact dev.bukkit.org for updating.");
                Logger.warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
                this.result = UpdateResult.FAIL_DBO;
            }
            e.printStackTrace();
            return false;
        }
    }

    private class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            if (Updater.this.url != null) {
                // Obtain the results of the project's file feed
                if (Updater.this.read()) {
                    if (Updater.this.versionCheck()) {
                        if ((Updater.this.updateInfo.downloadLink != null) && (Updater.this.type != UpdateType.NO_DOWNLOAD)) {
                            String name = Updater.this.file.getName();
                            // If it's a zip file, it shouldn't be downloaded as the plugin's name
                            if (Updater.this.updateInfo.downloadLink.endsWith(".zip")) {
                                final String[] split = Updater.this.updateInfo.downloadLink.split("/");
                                name = split[split.length - 1];
                            }
                            Updater.this.saveFile(new File(Updater.this.plugin.getDataFolder().getParent(), Updater.this.updateFolder), name, Updater.this.updateInfo.downloadLink);
                        } else {
                            Updater.this.result = UpdateResult.UPDATE_AVAILABLE;
                        }
                    }
                }
            }
        }
    }
}