package ship;
public class Ship {

     private int areaRow;
     private int areaCol;


     Ship(int areaRow, int areaCol)
     {
        this.areaRow = areaRow;
        this.areaCol = areaCol;

     }


     public void setAreaCol(int areaCol) {
         this.areaCol = areaCol;
     }

     public int getAreaCol() {
         return areaCol;
     }

     public void setAreaRow(int areaRow) {
         this.areaRow = areaRow;
     }

     public int getAreaRow() {
         return areaRow;
     }


}