package codepath.com.data;

/**
 * This is the class that will represent a Todo.
 * It contains ID, a title and a flag indicating if todo is urgent.
 * It can be further enhanced adding more properties.
 */
public class Todo {

    public long id;
    public String title;
    public boolean urgent;//true=todo is urgent

    //constructor
    public Todo(long newId, String newTitle, boolean newUrgent) {
        id = newId;
        title = newTitle;
        urgent = newUrgent;
    }


    //setters and getters:


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgency(boolean urgency) {
        urgent = urgency;
    }

}

