package greta.FlipperDemo.dm.managers;
import eu.aria.util.translator.Replacer;
import eu.aria.util.translator.Translator;
import eu.aria.util.translator.api.FileCache;
import eu.aria.util.translator.api.KeyValueReplacer;
import eu.aria.util.translator.auxiliary.DoubleListHashMap;
import eu.aria.util.translator.auxiliary.ListHashMap;
import eu.aria.util.translator.replaceable.ReplaceableTag;
import eu.aria.util.translator.replaceable.SelectableAlternative;
import eu.aria.util.translator.replaceable.SelectableTagsList;
import eu.aria.util.translator.replaceable.TagContainer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestReplGroup {
  private Translator translator;
  
  private HashMap<Integer, Replacer> replacers = new HashMap<>();
  
  private ListHashMap<String, KeyValueReplacer> tagMap = new ListHashMap();
  
  private DoubleListHashMap<String, String, KeyValueReplacer> attributeMap = new DoubleListHashMap();
  
  public TestReplGroup(String configPath) {
    this.translator = new Translator();
    try {
      this.translator.init(configPath);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public TestReplGroup(Translator translator) {
    this.translator = translator;
  }
  
  public void openFile(String filePath) {
    openFile(new File(filePath));
  }
  
  public void openFile(File file) {
    readString(FileCache.getInstance().getFileContent(file));
  }
  
  public void readString(String content) {
    this.translator.readString(content, false);
  }
  
  public void generateComponents() {
    generateComponents((TagContainer)this.translator.getTemplateParser());
  }
  
  public void performReplacements(String id) {
    this.translator.updateXML(this.replacers, id);
  }
  
  public void replaceVar(String name, String value) {
    if (!this.tagMap.forEach(name, r -> r.setValue(value)))
      System.err.println("No replacer with name (tagId) '" + name + "' found!"); 
  }
  
  public void replaceAttribute(String tagId, String attribute, String value) {
    if (!this.attributeMap.forEach(tagId, attribute, r -> r.setValue(value)))
      System.err.println("No replacer with tagId '" + tagId + "' and attribute name '" + attribute + "' found!"); 
  }
  
  public void addXMLListener(Translator.XMLListener listener, boolean withDTD) {
    this.translator.addXMLListener(listener, withDTD);
  }
  
  public void removeXMLListener(Translator.XMLListener listener) {
    this.translator.removeXMLListener(listener);
  }
  
  private void generateComponents(TagContainer tagContainer) {
    this.replacers.clear();
    this.tagMap.clear();
    this.attributeMap.clear();
    ArrayList<ReplaceableTag> tags = new ArrayList<>(tagContainer.getTags());
    for (ReplaceableTag tag : tags)
      generateComponentsTag(tag); 
    for (SelectableTagsList selTags : tagContainer.getSelectableTags())
      generateComponentsSelectable(selTags); 
  }
  
  private void generateComponentsSelectable(SelectableTagsList tags) {
    if (tags.size() == 0)
      return; 
    KeyValueReplacer replacer = new KeyValueReplacer(true, tags.getName(), null);
    List<String> options = tags.getOptions();
    if (options.isEmpty()) {
      System.err.println("No options detected for selectable tags list '" + tags.getName() + "'");
      return;
    } 
    replacer.setDefault(options.get(0));
    for (ReplaceableTag tag : tags.getTags()) {
      SelectableAlternative alternative = tag.getSelectableAlternative();
      if (alternative != null)
        for (TagContainer option : alternative.getOptions()) {
          for (ReplaceableTag innerTag : option.getTags())
            generateComponentsTag(innerTag); 
          for (SelectableTagsList selTags : option.getSelectableTags())
            generateComponentsSelectable(selTags); 
        }  
    } 
    this.replacers.put(Integer.valueOf(tags.getUniqueId()), replacer);
    this.tagMap.put(tags.getName(), replacer);
  }
  
  private void generateComponentsTag(ReplaceableTag tag) {
    if (tag.getTemplate().isReplaceAll()) {
      KeyValueReplacer replacer = new KeyValueReplacer(true, tag.getId(), null);
      replacer.setDefault("");
      this.replacers.put(Integer.valueOf(tag.getUniqueId()), replacer);
      this.tagMap.put(tag.getId(), replacer);
    } else {
      for (ReplaceableTag.ReplaceableAttribute attribute : tag.getAttributes()) {
        KeyValueReplacer replacer = new KeyValueReplacer(false, tag.getId(), attribute.getNodeName());
        replacer.setDefault(attribute.getOriginalValue());
        this.replacers.put(Integer.valueOf(attribute.getUniqueId()), replacer);
        this.attributeMap.put(tag.getId(), attribute.getNodeName(), replacer);
      } 
    } 
  }
}
