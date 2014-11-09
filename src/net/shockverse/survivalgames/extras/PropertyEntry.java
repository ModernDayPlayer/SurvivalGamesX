package net.shockverse.survivalgames.extras;

public class PropertyEntry {

    private PropertyList parent;
    private String key;
    private String value;
    private String comment;
    private boolean modified;

    public PropertyEntry(PropertyList parent, String key, String value) {
        this(parent, key, value, "");
    }

    public PropertyEntry(PropertyList parent, String key, String value, String comment) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.comment = comment;
        this.modified = false;
    }
    
    public PropertyEntry getParent() {
        return parent;
    }
    
    public void setParent(PropertyList newParent) {
        if(parent != null)
            parent.removeProperty(key);
        newParent.addProperty(this);
        parent = newParent;
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String newKey) {
        if(parent != null && parent.getFile() != null)
            parent.getFile().getShortcuts().remove(PropertyFile.getShortcut(this));
        key = newKey;
        if(parent != null && parent.getFile() != null)
            parent.getFile().getShortcuts().put(PropertyFile.getShortcut(this), this);
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String newValue) {
        value = newValue;
    }
    
    public void appendValue(String append) {
        value += "\n" + append;
    }

    public String getComment() {
        return comment;
    }
    
    public void setComment(String newComment) {
        comment = newComment;
    }
    
    public void modify() {
        modified = true;
    }
    
    public boolean isModified() {
        return modified;
    }
}
