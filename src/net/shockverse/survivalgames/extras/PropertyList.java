package net.shockverse.survivalgames.extras;

import java.util.ArrayList;
import java.util.List;
import net.shockverse.survivalgames.core.Logger;
import net.shockverse.survivalgames.core.Tools;
import org.bukkit.util.Vector;

public class PropertyList extends PropertyEntry {

    private PropertyFile file;
    private List<PropertyEntry> props;

    public PropertyList(PropertyFile file, PropertyList parent, String key, String comment) {
        super(parent, key, null, comment);
        this.file = file;
        props = new ArrayList<PropertyEntry>();
    }
    
    public PropertyFile getFile() {
        return file;
    }

    public List<PropertyEntry> getProperties() {
        final List<PropertyEntry> finalProps = new ArrayList<PropertyEntry>(props);
        return finalProps;
    }

    protected void addProperty(PropertyEntry entry) {
        file.getShortcuts().put(PropertyFile.getShortcut(entry), entry);
        props.add(entry);
    }

    public void removeProperty(String key) {
        file.getShortcuts().remove(PropertyFile.getShortcut(getProperty(key)));
        props.remove(getProperty(key));
    }

    public PropertyEntry getProperty(String key) {
        PropertyEntry entry = null;
        for (PropertyEntry prop : props) {
            if (prop.getKey().equalsIgnoreCase(key)) {
                entry = prop;
            }
        }
        return entry;
    }

    public boolean containsProperty(String key) {
        boolean found = false;
        for (PropertyEntry entries : props) {
            if (entries.getKey().equalsIgnoreCase(key)) {
                found = true;
            }
        }
        return found;
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, "");
    }

    public Boolean getBoolean(String key, boolean defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        Boolean value = defaultValue;
        if (containsProperty(key)) {
            try {
                value = Boolean.parseBoolean(getProperty(key).getValue());
            } catch (Exception e) {
                Logger.warning("Trying to get Boolean from " + key + ": " + getProperty(key).getValue());
                value = defaultValue;
            }
            removeProperty(key);
        }
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setBoolean(String key, Boolean value) {
        setBoolean(key, value, "");
    }
    
    public void setBoolean(String key, Boolean value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public String getString(String key, String defaultValue) {
        return getString(key, defaultValue, "");
    }

    public String getString(String key, String defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        String value = defaultValue;
        if (containsProperty(key)) {
            value = getProperty(key).getValue();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value, comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setString(String key, String value) {
        setString(key, value, "");
    }
    
    public void setString(String key, String value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value, comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public Integer getInt(String key, Integer defaultValue) {
        return getInt(key, defaultValue, "");
    }

    public Integer getInt(String key, Integer defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        Integer value = defaultValue;
        if (containsProperty(key)) {
            try {
                value = Integer.parseInt(getProperty(key).getValue());
            } catch (Exception e) {
                Logger.warning("Trying to get Integer from " + key + ": " + getProperty(key).getValue());
                value = defaultValue;
            }
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setInt(String key, Integer value) {
        setInt(key, value, "");
    }
    
    public void setInt(String key, Integer value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public Long getLong(String key, Long defaultValue) {
        return getLong(key, defaultValue, "");
    }

    public Long getLong(String key, Long defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        Long value = defaultValue;
        if (containsProperty(key)) {
            try {
                value = Long.parseLong(getProperty(key).getValue());
            } catch (Exception e) {
                Logger.warning("Trying to get Long from " + key + ": " + getProperty(key).getValue());
                value = defaultValue;
            }
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setLong(String key, Long value) {
        setLong(key, value, "");
    }
    
    public void setLong(String key, Long value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public Double getDouble(String key, Double defaultValue) {
        return getDouble(key, defaultValue, "");
    }

    public Double getDouble(String key, Double defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        Double value = defaultValue;
        if (containsProperty(key)) {
            try {
                value = Double.parseDouble(getProperty(key).getValue());
            } catch (Exception e) {
                Logger.warning("Trying to get Double from " + key + ": " + getProperty(key).getValue());
                value = defaultValue;
            }
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setDouble(String key, Double value) {
        setDouble(key, value, "");
    }
    
    public void setDouble(String key, Double value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public Byte getByte(String key, Byte defaultValue) {
        return getByte(key, defaultValue, "");
    }

    public Byte getByte(String key, Byte defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        Byte value = defaultValue;
        if (containsProperty(key)) {
            try {
                value = Byte.parseByte(getProperty(key).getValue());
            } catch (Exception e) {
                Logger.warning("Trying to get Byte from " + key + ": " + getProperty(key).getValue());
                value = defaultValue;
            }
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setByte(String key, Byte value) {
        setByte(key, value, "");
    }
    
    public void setByte(String key, Byte value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public Vector getVector(String key, Vector defaultValue) {
        return getVector(key, defaultValue, "");
    }

    public Vector getVector(String key, Vector defaultValue, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        Vector value = defaultValue;
        if (containsProperty(key)) {
            try {
                value = Tools.parseVector(getProperty(key).getValue());
            } catch (Exception e) {
                Logger.warning("Trying to get Vector from " + key + ": " + getProperty(key).getValue());
                value = defaultValue;
            }
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, value.toString(), comment);
        newEntry.modify();
        addProperty(newEntry);
        return value;
    }
    
    public void setVector(String key, Vector value) {
        setVector(key, value, "");
    }
    
    public void setVector(String key, Vector value, String comment) {
        if(containsProperty(key)) {
            if(Tools.isNullEmpty(comment))
                comment = getProperty(key).getComment();
            removeProperty(key);
        }
        PropertyEntry newEntry = new PropertyEntry(this, key, Tools.toString(value), comment);
        newEntry.modify();
        addProperty(newEntry);
    }

    public PropertyList getList(String key) {
        return getList(key, "");
    }

    public PropertyList getList(String key, String comment) {
        if (comment == null || comment.trim().equals("")) {
            comment = "";
        }
        PropertyList list = new PropertyList(file, this, key, comment);
        if (containsProperty(key)) {
            PropertyEntry entry = getProperty(key);
            if (entry instanceof PropertyList) {
                list = (PropertyList) entry;
            } else {
                Logger.warning("Trying to get PropList from " + key + ".");
            }
            removeProperty(key);
        }
        list.modify();
        addProperty((PropertyEntry) list);
        return list;
    }

    public void comment(String comment) {
        props.add(new PropertyEntry(this, "#", "", comment));
    }

    public void newLine() {
        props.add(new PropertyEntry(this, "\\n", "", ""));
    }
}
