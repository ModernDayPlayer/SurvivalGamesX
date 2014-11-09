package net.shockverse.survivalgames.extras;

import net.shockverse.survivalgames.core.Tools;
import net.shockverse.survivalgames.core.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import net.shockverse.survivalgames.exceptions.PropertyException;

/**
 * @description Manipulates a properties file
 * @author TaylorKelly
 */
public class PropertyFile {
    
    public static PropertyFile getFile(String pathFile) {
        return propFiles.get(pathFile);
    }
    
    public static String getShortcut(PropertyEntry entry) {
        String shortcut = entry.getKey();
        while(entry.getParent() != null 
                && entry.getParent().getKey() != null) {
            shortcut = entry.getParent().getKey() + "." + shortcut;
            entry = entry.getParent();
        }
        return shortcut;
    }
    
    private static HashMap<String, PropertyFile> propFiles = new HashMap<String, PropertyFile>();
    private HashMap<String, PropertyEntry> propShortcuts;
    private PropertyList properties;
    private File file;
    private int lineNumber;
    private PropertyEntry lastEntry;

    public PropertyFile(String configPath, String fileName) {
        this(configPath, new File(fileName));
        
    }

    public PropertyFile(String configPath, String path, String fileName) {
        this(configPath, new File(path, fileName));
    }

    public PropertyFile(String configPath, File file) {
        this.file = file;
        properties = new PropertyList(this, null, null, null);
        propShortcuts = new HashMap<String, PropertyEntry>();
        propFiles.put(configPath, this);
    }
    
    public HashMap<String, PropertyEntry> getShortcuts() {
        return propShortcuts;
    }
    
    public PropertyEntry getProperty(String shortcut) {
        return propShortcuts.get(shortcut);
    }

    public void loadProperties() throws IOException, PropertyException {
        lineNumber = 0;

        if (!file.exists()) {
            file.createNewFile();
        }
        Scanner scan = new Scanner(file);
        properties = loadPropertyList(scan);
    }

    protected PropertyList loadPropertyList(Scanner scan) throws PropertyException {
        return loadPropertyList(scan, null, null, null);
    }

    protected PropertyList loadPropertyList(Scanner scan, PropertyList parent, String lkey, String lcomment) throws PropertyException {
        PropertyList list = new PropertyList(this, parent, lkey, lcomment);
        propShortcuts.put(getShortcut(list), list);
        String line = "";
        while (scan.hasNextLine()) {
            lineNumber++;
            line = scan.nextLine().trim();
            if (line.startsWith("}")) {
                break;
            }
            if ((!line.contains("=") && !line.contains("{") && !line.contains("+")) || line.startsWith("#")) {
                continue;
            }
            if (!line.contains("{")) {
                if(line.startsWith("+")) {
                    String value = line.substring(line.indexOf("+") + 1).trim().replaceAll("\\\\'", "&&quote&&");
                    lastEntry.appendValue(value.replaceAll("'", "").replaceAll("&&quote&&", "'"));
                } else {
                    int equals = line.indexOf("=");
                    int commentIndex = line.length();
                    if (line.contains("#")) {
                        commentIndex = line.indexOf("#");
                    }

                    String key = line.substring(0, equals).trim();
                    if (key.equals("")) {
                        continue;
                    }
                    String value = line.substring(equals + 1, commentIndex).trim().replaceAll("\\\\'", "&&quote&&");
                    int quoteProblems = Tools.countString("'", value) - 2;
                    if (!value.startsWith("'")) {
                        quoteProblems++;
                    }
                    if (!value.endsWith("'")) {
                        quoteProblems++;
                    }
                    if (quoteProblems > 0) {
                        String source = key;
                        if(lkey != null)
                            source = lkey + "(" + key + ")";
                        throw new PropertyException("Unable to parse file " + file.getName() + ".\n\t Missing/extra single quote(s) for " + source + " on line " + lineNumber + ".\n\tNote: To have single quotes in a value you must escape the quote like so:\n\t\texample = 'This isn\\'t a bad example.'");
                    }
                    value = value.replaceAll("'", "").replaceAll("&&quote&&", "'");
                    String comment = "";
                    if (commentIndex < line.length() - 1) {
                        comment = line.substring(commentIndex + 1, line.length()).trim();
                    }
                    lastEntry = new PropertyEntry(list, key, value, comment);
                    list.addProperty(lastEntry);
                }
            } else {
                int bracket = line.indexOf("{");
                int commentIndex = line.length();
                if (line.contains("#")) {
                    commentIndex = line.indexOf("#");
                }
                String key = line.substring(0, bracket).trim();
                String comment = "";
                if (commentIndex < line.length() - 1) {
                    comment = line.substring(commentIndex + 1, line.length()).trim();
                }
                if (key == null || key.trim().equals("")) {
                    throw new PropertyException("Unable to parse file " + file.getName() + ".\n\tOpening bracket '{' must be on the same line as the array name.");
                }
                list.addProperty(loadPropertyList(scan, list, key, comment));
            }
            if (!scan.hasNextLine() && lkey != null) {
                throw new PropertyException("Unable to parse file " + file.getName() + ".\n\tMissing closing braket '}'.");
            }
        }
        return list;
    }

    public PropertyList getProperties() {
        return properties;
    }

    public void saveProperties() {
        BufferedWriter bwriter = null;
        FileWriter fwriter = null;
        try {
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            fwriter = new FileWriter(file);
            bwriter = new BufferedWriter(fwriter);
            saveProperties(bwriter, properties, 0);
            bwriter.flush();
        } catch (IOException e) {
            Logger.error("Could not save " + file.getName() + ". Reload the plugin to get a fresh copy.");
        } finally {
            try {
                if (bwriter != null) {
                    bwriter.flush();
                    bwriter.close();
                }
                if (fwriter != null) {
                    fwriter.close();
                }
            } catch (IOException e) {
                Logger.error("IO Exception with file " + file.getName() + " (on close).");
            }
        }
    }

    protected void saveProperties(BufferedWriter bwriter, PropertyList list, int indent) throws IOException {
        if (list.getKey() != null && indent != 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < indent - 1; i++) {
                builder.append("    ");
            }
            if(!list.isModified()) {
                builder.append("# ");
            }
            builder.append(list.getKey());
            builder.append(" {");
            if (!list.getComment().equals("")) {
                builder.append("   # ");
                builder.append(list.getComment());
            }
            bwriter.write(builder.toString());
            bwriter.newLine();
        }
        for (PropertyEntry entry : list.getProperties()) {
            StringBuilder builder = new StringBuilder();
            List<String> multiLineVals = new ArrayList<String>();
            if (entry.getKey().equals("\\n")) {
                bwriter.newLine();
                continue;
            }
            if (entry instanceof PropertyList) {
                saveProperties(bwriter, (PropertyList) entry, indent + 1);
            } else {
                for (int i = 0; i < indent; i++) {
                    builder.append("    ");
                }
                if(!entry.isModified() && !entry.getKey().equals("#")) {
                    builder.append("# ");
                }
                if (!entry.getKey().equals("#")) {
                    String val = entry.getValue();
                    if(entry.getValue().contains("\n")) {
                        String[] split = entry.getValue().split("\\n");
                        if(split != null) {
                            if(split.length > 0 && !Tools.isNullEmpty(split[0])) {
                                val = split[0];
                            }
                            for(int i = 1; i < split.length; i++) {
                                if(!Tools.isNullEmpty(split[i]))
                                    multiLineVals.add(split[i].replaceAll("\\\\'", "&&quote&&").replaceAll("'", "\\\\'").replaceAll("&&quote&&", "\\\\'"));
                            }
                        }
                    }
                    builder.append(entry.getKey());
                    builder.append(" = ");
                    builder.append("'");
                    builder.append(val.replaceAll("\\\\'", "&&quote&&").replaceAll("'", "\\\\'").replaceAll("&&quote&&", "\\\\'"));
                    builder.append("'");
                } else {
                    builder.append(entry.getKey());
                    
                }
                if (!entry.getComment().equals("")) {
                    if (!entry.getKey().equals("#")) {
                        builder.append("   #");
                    }
                    builder.append(" ");
                    builder.append(entry.getComment());
                }
            }
            bwriter.write(builder.toString());
            bwriter.newLine();
            for(String val : multiLineVals) {
                builder = new StringBuilder();
                if(!entry.isModified() && !entry.getKey().equals("#")) {
                    builder.append("# ");
                }
                for (int i = 0; i < indent - 1 + 2; i++) {
                    builder.append("    ");
                }
                builder.append("+ ");
                builder.append("'");
                builder.append(val);
                builder.append("'");
                bwriter.write(builder.toString());
                bwriter.newLine();
            }
            multiLineVals.clear();
        }
        if (indent != 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < indent - 1; i++) {
                builder.append("    ");
            }
            if(!list.isModified()) {
                builder.append("# ");
            }
            builder.append("}");
            bwriter.write(builder.toString());
        }
    }
}