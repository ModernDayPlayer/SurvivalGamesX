package net.shockverse.survivalgames.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.util.org.apache.commons.lang3.tuple.MutablePair;
import net.minecraft.util.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * @description Useful tools
 * @author Duker02, LegitModern, Tagette
 */
public class Tools {
    
    /*
     * Gets a random integer from min to max inclusive.
     */
    public static int randomInt(int min, int max){
        return (int) (Math.random() * (max - min)) + min;
    }
    
    /*
     * Gets a random double from min to max inclusive.
     */
    public static double random(double min, double max){
        return (Math.random() * (max - min)) + min;
    }
    
    /*
     * Rounds a double to a specific decimal place.
     */
    public static double round(double value, int decimals) {
        BigDecimal bd = new BigDecimal(value).setScale(decimals, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }
    
    /*
     * Rounds a location to a specific decimal place.
     */
    public static Location round(Location loc, int decimals) {
        return new Location(loc.getWorld(), round(loc.getX(), decimals), round(loc.getY(), decimals), round(loc.getZ(), decimals));
    }
    
    /*
     * Rounds a vector to a specific decimal place.
     */
    public static Vector round(Vector vector, int decimals) {
        return new Vector(round(vector.getX(), decimals), round(vector.getY(), decimals), round(vector.getZ(), decimals));
    }

    /*
     * Checks if a string is an Integer.
     */
    public static boolean isInt(String i) {
        boolean is = false;
        try {
            Integer.parseInt(i);
            is = true;
        } catch (Exception e) {
        }
        return is;
    }

    /*
     * Checks if a string is a Byte.
     */
    public static boolean isByte(String i) {
        boolean is = false;
        try {
            Byte.parseByte(i);
            is = true;
        } catch (Exception e) {
        }
        return is;
    }

    /*
     * Checks if a string is a Double.
     */
    public static boolean isDouble(String i) {
        boolean is = false;
        try {
            Double.parseDouble(i);
            is = true;
        } catch (Exception e) {
        }
        return is;
    }

    /*
     * Checks if a string is a Float.
     */
    public static boolean isFloat(String i) {
        boolean is = false;
        try {
            Float.parseFloat(i);
            is = true;
        } catch (Exception e) {
        }
        return is;
    }

    /*
     * Checks if a string is a Float.
     */
    public static boolean isVector(String i) {
        boolean is = false;
        try {
            parseVector(i);
            is = true;
        } catch (Exception e) {
        }
        return is;
    }

    // Gets the Material from bukkit enum or by id
    public static Pair<Material, Byte> getMatByNameId(String name) {
        Pair<Material, Byte> mat = null;
        if (isInt(name)) {
            mat = new MutablePair<Material, Byte>(getMatById(Integer.parseInt(name)), (byte) 0);
        } else {
            if(name.contains(":")) {
                String[] split = name.split(":");
                if(split.length > 1) {
                    String id = split[0];
                    if(!Tools.isNullEmptyWhite(id) && getMatByNameId(id.trim()) != null && Tools.isByte(split[1])) {
                        mat = new MutablePair<Material, Byte>(getMatByNameId(id).getLeft(), Byte.parseByte(split[1]));
                    }
                }
            } else {
                mat = new MutablePair<Material, Byte>(Material.getMaterial(getMatID(name)), (byte) 0);
            }
        }
        return mat;
    }

    // Gets the Material from ID
    public static Material getMatById(int id) {
        return Material.getMaterial(id);
    }

    // Gets the id of a Material
    public static int getMatID(String name) {
        int matID = -1;
        Material[] mat = Material.values();
        int temp = 9999;
        Material tmp = null;
        for (Material m : mat) {
            if (m.name().toLowerCase().replaceAll("_", "").startsWith(name.toLowerCase().replaceAll("_", "").replaceAll(" ", ""))) {
                if (m.name().length() < temp) {
                    tmp = m;
                    temp = m.name().length();
                }
            }
        }
        if (tmp != null) {
            matID = tmp.getId();
        }
        return matID;
    }
    
    /*
     * Capitalizes the first letter in a string.
     */
    public static String capitalizeFront(String raw) {
        String ret = "";
//        if(!raw.trim().contains(" ")) {
             ret = raw.toLowerCase();
            char first = ret.charAt(0);
            ret = first + ret.substring(1);
//        } else {
            // TODO: support multiple words.
//        }
        return ret;
    }
    
    /*
     * Joins a string array into one string with the delimiter between each array piece.
     */
    public static String join(String[] split, String delimiter) {
        return join(0, split, delimiter);
    }
    
    /*
     * Joins a string array into one string with the delimiter between each array piece.
     */
    public static String join(int start, String[] split, String delimiter) {
        String joined = "";
        for (int i = start; i < split.length; i++) {
            String s = split[i];
            joined += s + delimiter;
        }
        joined = joined.substring(0, joined.length() - (delimiter.length()));
        return joined;
    }
    
    /*
     * Checks to see if a string is null empty or whitespace.
     */
    public static boolean isNullEmptyWhite(String string) {
        return string == null || string.trim().equals("");
    }
    
    /*
     * Checks to see if a string is null or empty.
     */
    public static boolean isNullEmpty(String string) {
        return string == null || string.equals("");
    }

    /*
     * Converts &COLOR into their correct color codes.
     */
    public static String parseColors(String message) {
        if (message != null) {
            for (ChatColor color : ChatColor.values()) {
                message = message.replaceAll("&" + color.name().toUpperCase(), color.toString());
                message = message.replaceAll("&" + color.name().toLowerCase(), color.toString());
                message = message.replaceAll("&" + color.getChar(), color.toString());
            }
            message = message.replaceAll("&DEFAULT", ChatColor.WHITE.toString());
            message = message.replaceAll("&default", ChatColor.WHITE.toString());
        }
        return message;
    }
    
    // Splits a string that has values seperated by commas and makes an array.
    public static String[] processArray(String raw){
        if(raw == null || raw.trim().equals(""))
            return null;
        String[] split = raw.trim().split(",");
        for(int i = 0; i < split.length; i++){
            split[i] = split[i].trim();
        }
        return split;
    }

    /*
     * Makes a string a certain color while creating a white color afterwards.
     */
    public static String colorize(String text, ChatColor color) {
        return color + text + ChatColor.WHITE;
    }
    
    /*
     * Counts how many times the value appears in the search string.
     */
    public static int countString(String search, String value){
        int count = 0;
        while(value.contains(search)){
            value = value.replaceFirst(search, "");
            count++;
        }
        return count;
    }

    /*
     * Parses a string into a vector.
     */
    public static Vector parseVector(String value) {
        Vector vector = new Vector(0, 0, 0);
        String[] split = value.split(",");
        if(split.length == 3) {
            String xStr = split[0];
            String yStr = split[1];
            String zStr = split[2];
            if(!isNullEmpty(xStr) && isDouble(xStr.trim())
                && !isNullEmpty(yStr) && isDouble(yStr.trim())
                && !isNullEmpty(zStr) && isDouble(zStr.trim())) {
                vector.setX(Double.parseDouble(xStr));
                vector.setY(Double.parseDouble(yStr));
                vector.setZ(Double.parseDouble(zStr));
            } else {
                throw new NumberFormatException("Not a vector.");
            }
        } else {
            throw new NumberFormatException("Not a vector.");
        }
        return vector;
    }
    
    public static String toString(Location location, boolean block) {
        String ret = "";
        if(block) {
            ret = location.getBlockX() + ", "
            + location.getBlockY() + ", " 
            + location.getBlockZ();
        } else {
            ret = location.getX() + ", "
            + location.getY() + ", " 
            + location.getZ();
        }
        return ret;
    }
    
    public static String toString(Vector vector) {
            return vector.getX() + ", "
            + vector.getY() + ", " 
            + vector.getZ();
    }
    
    public static Location getLocation(World world, Vector position) {
        return new Location(world, position.getX(), position.getY(), position.getZ());
    }
    
    public static Vector getVector(Location loc, boolean block) {
        Vector ret = new Vector(loc.getX(), loc.getY(), loc.getZ());
        if(block) 
            ret = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return ret;
    }

    public static String getTime(long millis) {
        return getTime(millis, "s", "m", "h", "d", "m", "y");
    }

    public static String getFullTime(long millis) {
        return getTime(millis, " second(s)", " minute(s)", " hour(s)", " day(s)", " month(s)", " year(s)");
    }

    public static String getTime(long millis, String secStr, String minStr, 
            String hourStr, String dayStr, String monthStr, String yearStr) {
        String time = "";
        int MINUTE = 60, HOUR = 60*MINUTE, DAY = 24*HOUR, MONTH = 30*DAY, YEAR = 12*MONTH;
        int seconds = (int) (millis / 1000);
        if (seconds >= YEAR) {
            int years = (int) (seconds / YEAR);
            seconds -= years * YEAR;
            time = time + years + yearStr + " ";
        }
        if (seconds >= MONTH) {
            int months = (int) (seconds / MONTH);
            seconds -= months * MONTH;
            time = time + months + monthStr + " ";
        }
        if (seconds >= DAY) {
            int days = (int) (seconds / DAY);
            seconds -= days * DAY;
            time = time + days + dayStr + " ";
        }
        if (seconds >= HOUR) {
            int hours = (int) (seconds / HOUR);
            seconds -= hours * HOUR;
            time = time + hours + hourStr + " ";
        }
        if (seconds > MINUTE) {
            int minutes = (int) (seconds / MINUTE);
            seconds -= minutes * MINUTE;
            time = time + minutes + minStr + " ";
        }
        if(seconds > 0 || time.equals(""))
            time = time + seconds + secStr;

        return time;
    }
}
