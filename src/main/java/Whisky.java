import java.io.Serializable;



import java.io.Serializable;
        import java.util.concurrent.atomic.AtomicInteger;

public class Whisky  implements Serializable {

    private  int id;
    private String name;
    private String origin;
    public Whisky(int id, String name, String origin) {
        this.id = id;
        this.name = name;
        this.origin = origin;
    }

    public Whisky() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}