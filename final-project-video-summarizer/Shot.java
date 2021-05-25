public class Shot {
    
    int start;
    int end;

    Shot(int s, int e)
    {
        this.start = s;
        this.end = e;
    }

    public int get_start()
    {
        return this.start;
    }

    public int get_end()
    {
        return this.end;
    }
}
