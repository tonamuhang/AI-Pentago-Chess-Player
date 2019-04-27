package student_player;


import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoard;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

enum PLAYER{
    SELF, ENEMY;
}

enum PATTERN{
    XXXXX(50000),
    _XXXX_(1000),
    X_XXX(100), XX_XX(100), _XXXXO(100),
    _XXX_(40), _X_XX_(40),
    _XXXO(20), _X_XXO(20), _XX_XO(20), X__XX(20), X_X_X(20), O_XXX_O(20),
    __XX__(10), _X_X_(10), _X__X_(10),
    ___XXO(1), __X_XO(1), _X__XO(1), X___X(1);
    public final int value;
    PATTERN(final int newValue){
        this.value = newValue;
    }
}

class Node<T>{
    private List<Node<T>> children = new ArrayList<Node<T>>();
    private Node<T> parent = null;
    private T data = null;

    public Node(T data){
        this.data = data;
    }

    public Node(T data, Node<T> parent){
        this.data = data;
        this.setParent(parent);
    }

    public List<Node<T>> getChildren(){
        return children;
    }

    public void setParent(Node<T> parent){
        parent.addChild(this);
        this.parent = parent;
    }

    public void addChild(T data){
        Node<T> child = new Node<T>(data);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Node<T> child){
        child.setParent(this);
        this.children.add(child);
    }

    public boolean isRoot(){
        if(this.parent == null){
            return true;
        }
        return false;
    }

    public boolean isLeaf(){
        return !isRoot();
    }

    public void removeParent(){
        this.parent = null;
    }

    public T getData(){
        return this.data;
    }
}

public class MyTools {
    public static ArrayList<Integer> scores = new ArrayList<>();    //should be reset after every move
    public static int max = 999999;
    public static int min = -max;
    public static int score;
    public static int myRole = 0;

    public static double getSomething() {
        return Math.random();
    }

    @SuppressWarnings("Duplicates")
    //returns the index of the best move
    public static int evaluate(PentagoBoardState boardState, int depth){
        PentagoBoardState copy = (PentagoBoardState) boardState.clone();
        ArrayList<PentagoMove> moves = copy.getAllLegalMoves();
        int result = 0;
        int player = 0;

        //If reach desired depth, we will evaluate the current board and return the value
        if(depth == 0){
            //Pattern to be used to match with evaluation table

        }
        else {
            for (PentagoMove move : moves) {
                copy.processMove(move);
                scores.add(evaluate(copy, depth - 1));  //Add the calculated score
            }
            int max = 0;
            int index = 0;
            int value = 0;

            //Find the max value's index in scores
            for(Integer i : scores){
                value = scores.get(i);
                if(value > max){
                    max = value;
                    index = i;
                }
            }
            return index;
        }
        return 0;
    }


    public static Node generateTree(Node root, PentagoBoardState board, int depth){
        PentagoBoardState copy = (PentagoBoardState) board.clone();
        ArrayList<PentagoMove> moves = copy.getAllLegalMoves();

        if(depth == 0){
            return new Node(copy);
        }
        else {
            for (PentagoMove move : moves) {
                PentagoBoardState cp = (PentagoBoardState) copy.clone();
                cp.processMove(move);
                root.addChild(generateTree(root, cp, depth - 1));
            }
        }

        return root;
    }

    //Version 2

    //Evaluate a given pattern
    public static int evalPattern(String pattern){
        if(pattern.equals("BBBBB_") || pattern.equals("_BBBBB") || pattern.equals("WBBBBB") || pattern.equals("BBBBBW")){
            return PATTERN.XXXXX.value;
        }
        else if(pattern.equals("BBBB__") || pattern.equals("__BBBB")){
            return PATTERN._XXXX_.value;
        }
        else if(pattern.equals("BBBBW_") || pattern.equals("_WBBBB")){
            return PATTERN._XXXXO.value;
        }
        else if(pattern.equals("BBB___") || pattern.equals("___BBB")){
            return PATTERN._XXX_.value;
        }
        else if(pattern.equals("BB____") || pattern.equals("____BB") || pattern.equals("B_B___")
        || pattern.equals("___B_B")){
            return PATTERN.__XX__.value;
        }
        return 0;
    }
    public static int evalHorizontal(PentagoBoardState board){
        int p = board.getTurnPlayer();
        int score = 0;

        for(int i = 0; i < 6; i++){
            String pattern = "";
            for(int j = 0; j < 6; j++){
                pattern += board.getPieceAt(i, j).toString();
            }
            score += evalPattern(pattern);
        }
        if(p == myRole) {
            return score;
        }
        else{
            return -score;
        }
    }

    public static int evalVertical(PentagoBoardState board){
        int p = board.getTurnPlayer();
        int score = 0;

        for(int i = 0; i < 6; i++){
            String pattern = "";
            for(int j = 0; j < 6; j++){
                pattern += board.getPieceAt(j, i).toString();
            }
            score += evalPattern(pattern);
        }
        if(p == myRole) {
            return score;
        }
        else{
            return -score;
        }
    }


    //-----------------------------------------

    //Alpha beta prune with minimax
    /*
    public static int abp(int depth, int alpha, int beta, PentagoBoardState board){

        if(depth == 0){
            return eval(board);
        }

        ArrayList<PentagoMove> moves = board.getAllLegalMoves();

        for(PentagoMove move : moves){
            PentagoBoardState copy = (PentagoBoardState)board.clone();
            copy.processMove(move);
            val = -abp(depth - 1, -beta, -alpha, copy);
            if(val >= beta){

                return beta;
            }
            if(val > alpha){
                alpha = val;
            }
        }

        return alpha;
    }*/

    public static int DEPTH = 3;
    public static Object[] abp(int depth, int alpha, int beta, PentagoBoardState board){
        Object[] x = {eval(board), null};


//        if((int)x[0] >= 10000){
//            return x;
//        }

        if(depth == 0){
            return x;
        }

        ArrayList<PentagoMove> moves = board.getAllLegalMoves();
        Object[] bestMove = {0, null};
        int count = 0;
        for(PentagoMove move : moves){
            PentagoBoardState copy = (PentagoBoardState)board.clone();
            copy.processMove(move);
            

            Object[] tempMove = abp(depth - 1, -beta, -alpha, copy);
            int val = -(int)tempMove[0];

            if(depth == DEPTH - 1){
            	count ++;
            	System.out.println(count + ": " + move.toPrettyString());
                System.out.println(alpha + " " + beta + " " + val);
//                System.out.println(copy.toString());
            }

            if(val >= beta){
                return tempMove;
            }
            if(val > alpha){
                alpha = val;
                bestMove[1] = move;
                if(depth == DEPTH) {
                    bestMove = tempMove;
                    bestMove[1] = move;
//                    System.out.println(copy.toString());
                }
            }
        }


        return bestMove;
    }

    public static int abprune(int depth, int alpha, int beta, PentagoBoardState boardState){
        if(depth == 0){
            return sillyEval(boardState);
        }

        ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
        for(PentagoMove move : moves){
            int val = -abprune(depth - 1, -beta, -alpha, boardState);
            if(val >= beta){
                return beta;
            }
            if(val > alpha){
                alpha = val;
            }
        }
        return alpha;
    }

    public static PentagoMove getAbpMove(PentagoBoardState boardState, int depth){
        int score = -1;
        PentagoMove bestMove = null;

        for(PentagoMove m : boardState.getAllLegalMoves()){
            PentagoBoardState copy = (PentagoBoardState)boardState.clone();
            copy.processMove(m);
            int moveScore = abprune(depth, -99999, 99999, copy);

            if(moveScore >= score){
                score = moveScore;
                bestMove = m;
            }
        }
        return bestMove;
    }


    public static int eval(PentagoBoardState board){
        int score = 0;

        score += evalRow(board) + evalCol(board) + evalDiag1(board) + evalDiag2(board);
        return score;
    }


    /*
    ___     ___
    ___     ___
    ___     ___

    ___     ___
    ___     ___
    ___     ___

     */
    public static int patternMatch(String pattern){
        if(pattern.contains("XXXXX")){
            return 50000;
        }
        else if(pattern.contains("XXXX__") || pattern.contains("__XXXX") || pattern.contains("_XXXX_")){
            return 4320;
        }
        else if(pattern.contains("_XXX_") || pattern.contains("XXX_") || pattern.contains("_XXX") ||
                pattern.contains("XXXX_") || pattern.contains("_XXXX")){
            return 720;
        }
        else if(pattern.contains("_XX_") || pattern.contains("_XX") || pattern.contains("XX_")){
            return 120;
        }
        else if(pattern.contains("XX")){
            return 60;
        }
        else if(pattern.contains("_X_")){
            return 20;
        }
        return 0;
    }

    @SuppressWarnings("Duplicates")
    public static int evalRow(PentagoBoardState board){
        int score = 0;
        int player = board.getTurnPlayer();
        String role = "";
        String opponent = "";

        if(player == 0){
            role = "w";
            opponent = "b";
        }
        else{
            role = "b";
            opponent = "w";
        }

        for(int i = 0; i < 6; i++){
            String pattern = "";
            for(int j = 0; j < 6; j++){
                if(board.getPieceAt(i, j).toString().equals(role)){
                    pattern += "X";
                }
                else if (board.getPieceAt(i, j).toString().equals(opponent)){
                    pattern += "O";
                }
                else{
                    pattern += "_";
                }

            }
            score += patternMatch(pattern);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalCol(PentagoBoardState board){
        int score = 0;
        int player = board.getTurnPlayer();
        String role = "";
        String opponent = "";

        if(player == 0){
            role = "w";
            opponent = "b";
        }
        else{
            role = "b";
            opponent = "w";
        }

        for(int i = 0; i < 6; i++){
            String pattern = "";
            for(int j = 0; j < 6; j++){
                if(board.getPieceAt(j, i).toString().equals(role)){
                    pattern += "X";
                }
                else if (board.getPieceAt(j, i).toString().equals(opponent)){
                    pattern += "O";
                }
                else{
                    pattern += "_";
                }

            }
            score += patternMatch(pattern);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag1(PentagoBoardState board){
        int score = 0;
        int player = board.getTurnPlayer();
        String role = "";
        String opponent = "";

        if(player == 0){
            role = "w";
            opponent = "b";
        }
        else{
            role = "b";
            opponent = "w";
        }

        //Check main diagonal
        String pattern = "";
        for(int i = 0; i < 6; i++){
            if(board.getPieceAt(i, i).toString().equals(role)){
                pattern += "X";
            }
            else if(board.getPieceAt(i, i).toString().equals(opponent)){
                pattern += "O";
            }
            else{
                pattern += "_";
            }
        }
        score += patternMatch(pattern);

        //Check side diagonal
        pattern = "";
        for(int i = 0; i < 5; i++){
            if(board.getPieceAt(i, i + 1).toString().equals(role)){
                pattern += "X";
            }
            else if(board.getPieceAt(i, i + 1).toString().equals(opponent)){
                pattern += "O";
            }
            else{
                pattern += "_";
            }
        }
        score += patternMatch(pattern);

        pattern = "";
        for(int i = 0; i < 5; i++){
            if(board.getPieceAt(i + 1, i).toString().equals(role)){
                pattern += "X";
            }
            else if(board.getPieceAt(i + 1, i).toString().equals(opponent)){
                pattern += "O";
            }
            else{
                pattern += "_";
            }
        }
        score += patternMatch(pattern);

        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag2(PentagoBoardState board){
        int score = 0;
        int player = board.getTurnPlayer();
        String role = "";
        String opponent = "";

        if(player == 0){
            role = "w";
            opponent = "b";
        }
        else{
            role = "b";
            opponent = "w";
        }

        //Check main diagonal
        String pattern = "";
        for(int i = 0; i < 6; i++){
            if(board.getPieceAt(i, 5 - i).toString().equals(role)){
                pattern += "X";
            }
            else if(board.getPieceAt(i, 5 - i).toString().equals(opponent)){
                pattern += "O";
            }
            else{
                pattern += "_";
            }
        }
        score += patternMatch(pattern);

        //Check side diagonal
        pattern = "";
        for(int i = 0; i < 5; i++){
            if(board.getPieceAt(i, 5 - i - 1).toString().equals(role)){
                pattern += "X";
            }
            else if(board.getPieceAt(i, 5 - i - 1).toString().equals(opponent)){
                pattern += "O";
            }
            else{
                pattern += "_";
            }
        }
        score += patternMatch(pattern);

        pattern = "";
        for(int i = 0; i < 5; i++){
            if(board.getPieceAt(i + 1, 6 - 1 - i).toString().equals(role)){
                pattern += "X";
            }
            else if(board.getPieceAt(i + 1, 6 - 1 - i).toString().equals(opponent)){
                pattern += "O";
            }
            else{
                pattern += "_";
            }
        }
        score += patternMatch(pattern);

        return score;
    }

    public static int greedyEval(PentagoBoardState boardState) {
        int utility = 0;
        int streak = 0;
        int player = boardState.getTurnPlayer();
        PentagoBoardState.Piece color;

        if(player == 0){
            color = PentagoBoardState.Piece.WHITE;
        }
        else{
            color = PentagoBoardState.Piece.BLACK;
        }

        //count horizontal doubles
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (boardState.getPieceAt(i, j) == color &&
                        boardState.getPieceAt(i,j+1) == color) {
                    utility += streak + 1;
                    streak++;
                } else {
                    streak = 0;
                }
            }
        }
        //count vertical doubles
        for (int i = 0; i < 6; i++) {
            for(int j = 0; j < 5; j++) {
                if(boardState.getPieceAt(j, i) == color
                        && boardState.getPieceAt(j+1, i) == color) {
                    utility += streak + 1;
                    streak++;
                }
                else {
                    streak = 0;
                }
            }
        }
        //count main diagonal up-left to down-right
        for (int i = 0; i < 5; i++) {
            if (boardState.getPieceAt(i, i) == color
                    && boardState.getPieceAt(i+1, i+1) == color) {
                utility += streak + 1;
                streak++;
            }
            else {
                streak = 0;
            }
        }
        //count main diagonal up-right to down-left
        for (int i = 0; i < 5; i++) {
            if (boardState.getPieceAt(i, 5-i) == color
                    && boardState.getPieceAt(i+1, 4-i) == color) {
                utility += streak + 1;
                streak++;
            } else {
                streak = 0;
            }
        }
        return utility;
    }

    public static int sillyEval(PentagoBoardState board){
        Random rand = new Random();

        if(board.gameOver() && board.getWinner() == board.getTurnPlayer()){
            return 10000;
        }
        else if(board.gameOver() && board.getWinner() == Board.DRAW){
            return 0;
        }
        else{
            return rand.nextInt(100) + 200;
        }
    }




}