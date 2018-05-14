import jm.music.data.Note;

/**
 * A class representing a note along with its temporal location.
 * @author Jake
 *
 */
@SuppressWarnings("serial") //we will not be serializing these differently
public class LocatedNote extends Note implements Comparable<LocatedNote>{

    private double time;
    
    public LocatedNote(int pitch, double rhythm, double time) {
        super(pitch, rhythm);
        this.time = time;
    }
    
    public void setTime(double newtime) { time = newtime; }
    public double getTime() { return time; }

    @Override
    public int compareTo(LocatedNote o) {
        return (new Double(time)).compareTo(new Double(o.time));
    }
}
