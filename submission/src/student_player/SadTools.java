package student_player;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;
import javafx.beans.binding.When;
import pentago_swap.PentagoBoard;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SadTools {

    public static int myRole = 0;
    public static int opponentRole = 0;
    public static PentagoMove best = null;
    public static int[] scores = {0, 0};

    public static PentagoMove getMove(PentagoBoardState boardState, int alpha, int beta, int depth){
//        abp(boardState, alpha, beta, depth);
        miniMax(boardState, alpha, beta, depth);
        return best;
    }

    public static int miniMax(PentagoBoardState boardState, int alpha, int beta, int depth){
        ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
        Collections.shuffle(moves);
        int score;
        PentagoMove bestMove = null;
        if(boardState.gameOver() || depth == 0){
            score = smartEval(boardState);
            return score;
        }
        else{
            for(PentagoMove move : moves){
                PentagoBoardState copy = (PentagoBoardState)boardState.clone();
                copy.processMove(move);

                //if the turn player is opponent, ive just made my move and i should maximize this move
                if(copy.getTurnPlayer() == opponentRole){
                    score = miniMax(copy, alpha, beta, depth - 1);
                    if(score > alpha){
                        alpha = score;
                        bestMove = move;
                    }
                }
                else{
                    score = miniMax(copy, alpha, beta, depth - 1);
                    if(score < beta){
                        beta = score;
                        bestMove = move;
                    }
                }
                if(alpha >= beta){
                    break;
                }
            }

            best = bestMove;
            return boardState.getTurnPlayer() == myRole ? alpha : beta;
        }
    }

    //Bad code :((
    public static int abp(PentagoBoardState boardState, int alpha, int beta, int depth){
        if(depth-- == 0 || boardState.gameOver()){
            return smartEval(boardState);       //change eval funcs here
        }
        if(boardState.getTurnPlayer() == myRole){
            return getMax(boardState, alpha, beta, depth);
        }
        else{
            return getMin(boardState, alpha, beta, depth);
        }
    }

    //Useless code :((
    public static int getMax(PentagoBoardState boardState, int alpha, int beta, int depth){
        PentagoMove bestMove = null;
        ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();


        for(PentagoMove move : moves){
            PentagoBoardState copy = (PentagoBoardState)boardState.clone();
            copy.processMove(move);
            int score = abp(copy, alpha, beta, depth);

            if(score > alpha){
                alpha = score;
                bestMove = move;
            }

            //prune
            if(alpha >= beta){
                break;
            }
        }

        if(bestMove != null){
            best = bestMove;
        }
        return alpha;
    }

    public static int getMin(PentagoBoardState boardState, int alpha, int beta, int depth){
        PentagoMove bestMove = null;
        for(PentagoMove move : boardState.getAllLegalMoves()){
            PentagoBoardState copy = (PentagoBoardState) boardState.clone();
            copy.processMove(move);
            int score = abp(copy, alpha, beta, depth);

            if(score < beta){
                beta = score;
                bestMove = move;
            }

            //prune
            if(alpha >= beta){
                break;
            }
        }

        if(bestMove != null){
            best = bestMove;
        }
        return beta;
    }


    public static int sillyEval(PentagoBoardState board, int depth){

        if(board.getWinner() == Board.DRAW){
            return 1 + depth;
        }
        else if(board.getWinner() == myRole){
            return 5 + depth;
        }
        else if(board.getWinner() == opponentRole){
            return -1 + depth;
        }
        else{
            return 0;
        }

    }

    //Good code
    public static int smartEval(PentagoBoardState board){
        int score = 0;
        int whiteScore;
        int blackScore;

        whiteScore = whiteDiag1(board) + whiteDiag2(board) + whiteDiag3(board) + whiteDiag4(board) + whiteDiag5(board)
                + whiteDiag6(board) + whiteHorizontal(board) + whiteVertical(board);
        blackScore = blackDiag1(board) + blackDiag2(board) + blackDiag3(board) + blackDiag4(board) + blackDiag5(board)
                + blackDiag6(board) + blackHorizontal(board) + blackVertical(board);

        if(myRole == PentagoBoardState.WHITE) {
//            System.out.println(whiteScore);
            return whiteScore - blackScore;
        }
        else{
            return blackScore - whiteScore;
        }

//        if(board.getTurnPlayer() == myRole) {
//            return score;
//        }
//        else{
//            return -score;
//        }

    }

    public static int shapeScore(int consecutive, int open){
        if(consecutive == 5){
            return 500000;
        }
        if(open == 0 && consecutive < 5){
            return 0;
        }

        switch (consecutive){
            case 4:
                switch (open){
                    case 1:
                        return 4320;
                    case 2:
                        return 4320;
                }
            case 3:
                switch (open){
                    case 2:
                        return 720;
                    case 1:
                        return 720;
                }
            case 2:
                switch (open){
                    case 2:
                        return 120;
                    case 1:
                        return 120;
                }
            case 1:
                switch (open){
                    case 1:
                        return 20;
                    case 2:
                        return 20;
                }
                default:
                    return 500000;

        }

    }

    @SuppressWarnings("Duplicates")
    public static int blackHorizontal(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                PentagoBoardState.Piece piece = boardState.getPieceAt(i, j);
                if(piece == PentagoBoardState.Piece.BLACK){
                    consecutive ++;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    open ++;
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 1;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    open = 1;
                }
                else if(consecutive > 0){
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 0;
                }
                else{
                    open = 0;
                }
            }
            if(consecutive > 0){
                score += shapeScore(consecutive, open);
            }
            consecutive = 0;
            open = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteHorizontal(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                PentagoBoardState.Piece piece = boardState.getPieceAt(i, j);
                if(piece == PentagoBoardState.Piece.WHITE){
                    consecutive ++;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    open ++;
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 1;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    open = 1;
                }
                else if(consecutive > 0){
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 0;
                }
                else{
                    open = 0;
                }
            }
            if(consecutive > 0){
                score += shapeScore(consecutive, open);
            }
            consecutive = 0;
            open = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int blackVertical(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                PentagoBoardState.Piece piece = boardState.getPieceAt(j, i);
                if(piece == PentagoBoardState.Piece.BLACK){
                    consecutive ++;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    open ++;
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 1;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    open = 1;
                }
                else if(consecutive > 0){
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 0;
                }
                else{
                    open = 0;
                }
            }
            if(consecutive > 0){
                score += shapeScore(consecutive, open);
            }
            consecutive = 0;
            open = 0;
        }
        return score;
    }
    @SuppressWarnings("Duplicates")
    public static int whiteVertical(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                PentagoBoardState.Piece piece = boardState.getPieceAt(j, i);
                if(piece == PentagoBoardState.Piece.WHITE){
                    consecutive ++;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    open ++;
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 1;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    open = 1;
                }
                else if(consecutive > 0){
                    score += shapeScore(consecutive, open);
                    consecutive = 0;
                    open = 0;
                }
                else{
                    open = 0;
                }
            }
            if(consecutive > 0){
                score += shapeScore(consecutive, open);
            }
            consecutive = 0;
            open = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int blackDiag1(PentagoBoardState boardState) {
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 6; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i, i);
            if (piece == PentagoBoardState.Piece.BLACK) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteDiag1(PentagoBoardState boardState) {
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 6; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i, i);
            if (piece == PentagoBoardState.Piece.WHITE) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }



    @SuppressWarnings("Duplicates")
    public static int blackDiag2(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i, i + 1);
            if (piece == PentagoBoardState.Piece.BLACK) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteDiag2(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i, i + 1);
            if (piece == PentagoBoardState.Piece.WHITE) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int blackDiag3(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i + 1, i);
            if (piece == PentagoBoardState.Piece.BLACK) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteDiag3(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i + 1, i);
            if (piece == PentagoBoardState.Piece.WHITE) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int blackDiag4(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 6; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i , 5 - i);
            if (piece == PentagoBoardState.Piece.BLACK) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteDiag4(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 6; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i , 5 - i);
            if (piece == PentagoBoardState.Piece.WHITE) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int blackDiag5(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i , 5 - i - 1);
            if (piece == PentagoBoardState.Piece.BLACK) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteDiag5(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i , 5 - i - 1);
            if (piece == PentagoBoardState.Piece.WHITE) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int blackDiag6(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i + 1 , 5 - i);
            if (piece == PentagoBoardState.Piece.BLACK) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int whiteDiag6(PentagoBoardState boardState){
        int score = 0;
        int consecutive = 0;
        int open = 0;

        for (int i = 0; i < 5; i++) {
            PentagoBoardState.Piece piece = boardState.getPieceAt(i + 1 , 5 - i);
            if (piece == PentagoBoardState.Piece.WHITE) {
                consecutive++;
            } else if (piece == PentagoBoardState.Piece.EMPTY && consecutive > 0) {
                open++;
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 1;
            } else if (piece == PentagoBoardState.Piece.EMPTY) {
                open = 1;
            } else if (consecutive > 0) {
                score += shapeScore(consecutive, open);
                consecutive = 0;
                open = 0;
            } else {
                open = 0;
            }

        }
        if (consecutive > 0) {
            score += shapeScore(consecutive, open);
        }
        return score;
    }







}
