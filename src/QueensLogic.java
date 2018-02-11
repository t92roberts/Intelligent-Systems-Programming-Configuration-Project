/**
 * This class implements the logic behind the BDD for the n-queens problem
 * You should implement all the missing methods
 * 
 * @author Stavros Amanatidis
 *
 */

import net.sf.javabdd.*;

public class QueensLogic {
    private int x = 0;
    private int y = 0;
    private int[][] board;
    private int nodes = 2000000;
    private int cache = (int)(nodes * 0.1);

    public BDDFactory factory = JFactory.init(nodes, cache);
    public BDD[][] varArray;
    private BDD queen = factory.one();
    public QueensLogic() {
       //constructor
    }

    public void initializeGame(int size) {
        this.x = size;
        this.y = size;
        this.board = new int[x][y];
        this.factory.setVarNum(size*size);
        this.varArray = BuildArray(size);
        this.MakeRules();
    }


    public int[][] getGameBoard() {
        return board;
    }

    // Placing BBD variables into NxN array
    public BDD[][] BuildArray(int N) {
      BDD[][] varArray = new BDD[N][N];
      for (int col = 0; col < N; col++){
        BDD False = factory.zero();
        for (int row = 0; row < N; row++){
          varArray[col][row] = factory.nithVar(row + (col*N));
          False.orWith(varArray[col][row].id());
        }
        queen.andWith(False);
      }
      return varArray;
    }

    public void MakeRules(){
      for (int col = 0; col < this.y; col++){
        for (int row = 0; row < this.x; row++){
          Build(col, row);
        }
      }
    }


    public void Build(int col, int row){
      BDD colvalid = factory.one(), rowvalid = factory.one();
      BDD upright = factory.one(), downright = factory.one();

      // checking the columns
      for (int i = 0; i < this.y; i++){
        if (i != row){
          // Checking every pair in columns is valid
          BDD pair_valid = this.varArray[col][i].apply(this.varArray[col][row], BDDFactory.nand);
          // Combining this pairs validity with the columns validity
          colvalid.andWith(pair_valid);
        }
      }

      // checking the rows
      for (int i = 0; i < this.x; i++){
        if (i != col){
          // checking every pair in rows if valid
          BDD pair_valid = this.varArray[i][row].apply(this.varArray[col][row], BDDFactory.nand);
          // Combining this pairs
          rowvalid.andWith(pair_valid);
        }
      }

      for (int i = 0; i < this.x; i++){
        int u = i - col + row;
        if(u >= 0 && u < this.x){
          if(i != col){
            BDD pair_valid = this.varArray[i][u].apply(this.varArray[col][row], BDDFactory.nand);
            upright.andWith(pair_valid);
          }
        }
      }

      for (int i = 0; i < this.x; i++){
        int u = col + row - i;
        if(u >= 0 && u < this.x){
          if(i != col){
            BDD pair_valid = this.varArray[i][u].apply(this.varArray[col][row], BDDFactory.nand);
            downright.andWith(pair_valid);
          }
        }
      }

      colvalid.andWith(rowvalid);
      upright.andWith(colvalid);
      downright.andWith(upright);
      queen.andWith(downright);

    }

    private boolean IsInvalidMove(int col, int row){
      BDD new_bdd = this.queen.restrict(this.varArray[col][row]);
      return new_bdd.isZero();
    }

    private void updateBDD(){
      for(int col = 0; col < this.y; col++){
        for(int row = 0; row < this.x; row++){
          if(IsInvalidMove(col, row)){
             board[col][row] = -1;
          }
        }
      }
    }

    private void autoCompleteSolution() {
        for(int col = 0; col < this.x; col++) {
            for (int row = 0; row < this.y; row++) {
                if (board[col][row] == 0) {
                    board[col][row] = 1;
                }
            }
        }
    }

    public boolean insertQueen(int column, int row) {

        if (board[column][row] == -1 || board[column][row] == 1) {
            return true;
        }

        board[column][row] = 1;

        this.queen = this.queen.restrict(this.varArray[column][row]);

        updateBDD();

        if (queen.pathCount() == 1) {
            autoCompleteSolution();
        }

        return true;
    }
}
