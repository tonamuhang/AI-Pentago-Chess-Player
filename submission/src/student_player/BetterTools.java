package student_player;

import boardgame.Board;
import boardgame.BoardState;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.ArrayList;
import java.util.Hashtable;

class MyHashtable extends Hashtable<PentagoMove, Integer> {

private Integer maxValue = Integer.MIN_VALUE;

@Override
public synchronized Integer put(PentagoMove k, Integer v) {
        maxValue = Math.max(maxValue, v);
        return super.put(k, v);
        }

@Override
public synchronized void clear() {
        super.clear();
        maxValue = Integer.MIN_VALUE;
        }

public Integer getMaxValue() {
        return maxValue;
        }

}

public class BetterTools {
    public static int myRole;
    public static MyHashtable bestScore;
    public static int DEPTH;
    public static int color;

    public static PentagoMove getMove(PentagoBoardState boardState, int depth, int alpha, int beta) {
        bestScore = new MyHashtable();
        DEPTH = depth;

        negamax(boardState, 0, alpha, beta, 1);

        for (Object o : bestScore.keySet()) {
            if (bestScore.get(o).equals(bestScore.getMaxValue())) {
                return (PentagoMove) o;
            }
        }
        return null;

    }

    public static int negamax(PentagoBoardState boardState, int depth, int alpha, int beta, int color) {
        if (boardState.gameOver() || depth == DEPTH) {
            return color * sillyEval(boardState, depth);    //change eval here
        }
        int max = alpha;
        ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
        for (PentagoMove move : moves) {
            PentagoBoardState copy = (PentagoBoardState) boardState.clone();
            copy.processMove(move);

            int val = -negamax(copy, depth + 1, -beta, -alpha, -color);
            max = max > val ? max : val;

            if (depth == 0) {
                bestScore.put(move, max);
            }

            alpha = alpha > val ? alpha : val;
            if (alpha >= beta) {
                return alpha;
            }
        }
        return max;
    }

    public static int sillyEval(PentagoBoardState boardState, int depth) {
        if (boardState.gameOver() && boardState.getWinner() == Board.DRAW) {
            return 0;
        } else if (boardState.gameOver() && boardState.getWinner() == myRole) {
            return 10000;
        } else {
            return -10000;
        }
    }

    public static int smartEval(PentagoBoardState board, int depth) {
        int score = 0;
//        score += evalHorizontal(board) + evalVertical(board);

        return score / depth;
//        if(board.getTurnPlayer() == myRole) {
//            return score / depth;
//        }
//        else{
//            return -score / depth;
//        }

    }
}







