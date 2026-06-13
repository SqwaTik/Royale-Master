package royale.events.api.events.render;
import royale.events.api.events.Event;
public class TextFactoryEvent
implements Event {
private String text;
public void setText(String text) {
this.text = text;
} public TextFactoryEvent(String text) {
this.text = text;
}
public String getText() {
return this.text;
}
public void replaceText(String protect, String replaced) {
if (this.text == null || this.text.isEmpty())
return; 
if (this.text.contains(protect) && (
this.text.equalsIgnoreCase(protect) || this.text.contains(protect + " ") || this.text.contains(" " + protect) || this.text.contains("⏏" + protect) || this.text.contains(protect + "§")))
this.text = this.text.replace(protect, replaced); 
}
}


