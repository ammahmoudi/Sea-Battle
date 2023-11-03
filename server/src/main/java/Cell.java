
import java.util.ArrayList;

public class Cell {


    private int cellType = 1;
    private ArrayList<Point> childShips = null;
    private Point headShipCordinates = null;
    private int row;
    private int col;
    private int blood = 0;
    private boolean isDestroyed = false;


    Cell(int cellType, int col, int row)
    {
        this.cellType = cellType;
        this.row = row;
        this.col = col;
        childShips = new ArrayList<Point>();
    }

    

    public int getBlood() {
        return blood;
    }

    public void setBlood(int blood) {
        this.blood = blood;
    }


    public int getCellType() {
        return cellType;
    }
    public int getCol() {
        return col;
    }
    public Point getHeadShipCordinates() {
        return headShipCordinates;
    }
    public int getRow() {
        return row;
    }

    public void setCellType(int cellType) {
        this.cellType = cellType;
    }
    public void setChildShips(ArrayList<Point> childShips) {
        this.childShips = childShips;
    }
    public void setCol(int col) {
        this.col = col;
    }
    public void setDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }
    public void setHeadShipCordinates(Point headShipCordinates) {
        this.headShipCordinates = headShipCordinates;
    }
    public void setRow(int row) {
        this.row = row;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public ArrayList<Point> getChildShips() {
        return childShips;
    }


	public void resetChildShips() {
        this.childShips = new ArrayList<Point>();
	}
    
    
}
