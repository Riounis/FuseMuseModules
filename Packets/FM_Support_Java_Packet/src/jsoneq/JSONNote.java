package jsoneq;

public class JSONNote{
    public int pitch;
    
    public double duration;
    
    public String type = "note";
    
    //defaults of the "not relevant yet" sort
    //TODO: java JSON that is not awful
    public int triplet = 0;
    public int dotted = 0;
    public int double_dotted = 0;
    public int staccato = 0;
    public int tenuto = 0;
    public int accent = 0;
    public int fermata = 0;
    public int tied = 0;
    public int slurred = 0;
}
