package tubeScanner.code.events;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by Alex on 16.04.2015.
 */
public class SearchCodeEvent extends Event {
    private String code;
    public SearchCodeEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public SearchCodeEvent(String code){
        this(EventType.ROOT);//todo root muss man auf SearchCodeEvent ersetzen
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
