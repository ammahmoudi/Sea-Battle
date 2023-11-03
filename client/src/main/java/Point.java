
public class Point {

    //================================================================

    private int row;
    private int col;

    //================================================================

    public Point(int col, int row)
    {
        this.col = col;
        this.row = row;
    }

    //================================================================

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Point)
        {
            Point p = (Point) obj;
            
            if(p.getCol() == this.col
            && p.getRow() == this.row)
            {
                return true;
            }
        }
        return super.equals(obj);
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}