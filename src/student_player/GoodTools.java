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
import java.util.List;
import java.util.Random;

public class GoodTools {

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

    public static int smartEval(PentagoBoardState board){
        int score = 0;
        int whiteScore = 0;
        int blackScore = 0;

        score += evalDiag1(board) + evalDiag2(board) + evalDiag3(board) + evalDiag4(board) + evalDiag5(board)
                + evalDiag6(board) + evalHorizontal(board) + evalVertical(board);

        return score;

//        if(board.getTurnPlayer() == myRole) {
//            return score;
//        }
//        else{
//            return -score;
//        }

    }

    public static int shapreScore(int consecutive, int openEnds, boolean myturn){
        if(openEnds == 0 && consecutive < 5){
            return 0;
        }
        if(consecutive == 5){
            return 500000;
        }
        switch (consecutive){
            case 4:
                switch (openEnds){
                    case 1:
                        if(myturn){
                            return 50000;
                        }
                        return 10000;
                    case 2:
                        if(myturn){
                            return 50000;
                        }
                        return 50000;
                }
            case 3:
                switch (openEnds){
                    case 1:
                        if(myturn){
                            return 7;
                        }
                        return 5;
                    case 2:
                        if(myturn){
                            return 10000;
                        }
                        return 50;
                }
            case 2:
                switch (openEnds){
                    case 1:
                        return 2;
                    case 2:
                        return 5;
                }
            case 1:
                switch (openEnds){
                    case 1:
                        return 0;
                    case 2:
                        return 1;
                }
            default:
                return 50000;
        }





//        switch (consecutive){
//            case 4:
//                switch (openEnds){
//                    case 1:
//                        if(myturn){
//                            return 50000;
//                        }
//                        return 50000;
//                    case 2:
//                        if(myturn){
//                            return 50000;
//                        }
//                        return 50000;
//                }
//            case 3:
//                switch (openEnds){
//                    case 1:
//                        if(myturn){
//                            return 7;
//                        }
//                        return 5;
//                    case 2:
//                        if(myturn){
//                            return 10000;
//                        }
//                        return 50;
//                }
//            case 2:
//                switch (openEnds){
//                    case 1:
//                        return 2;
//                    case 2:
//                        return 5;
//                }
//            case 1:
//                switch (openEnds){
//                    case 1:
//                        return 0;
//                    case 2:
//                        return 1;
//                }
//                default:
//                    return 50000;
//        }
    }

    @SuppressWarnings("Duplicates")
    public static int evalHorizontal(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                PentagoBoardState.Piece piece = board.getPieceAt(i, j);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;

                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 1;
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }

            }
            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }
            consecutive = 0;
            openEnd = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalVertical(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                PentagoBoardState.Piece piece = board.getPieceAt(j, i);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }

            }
            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }
            consecutive = 0;
            openEnd = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag1(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 0; i < 6; i++){

                PentagoBoardState.Piece piece = board.getPieceAt(i, i);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }

            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }
//            consecutive = 0;
//            openEnd = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag2(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 1; i < 6; i++){

                PentagoBoardState.Piece piece = board.getPieceAt(i, i - 1);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }


            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }

        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag3(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 0; i < 5; i++){
                PentagoBoardState.Piece piece = board.getPieceAt(i, i + 1);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }


            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }

        }
        return score;
    }


    @SuppressWarnings("Duplicates")
    public static int evalDiag4(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 0; i < 6; i++){
                PentagoBoardState.Piece piece = board.getPieceAt(i, 5 - i);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }


            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }
//            consecutive = 0;
//            openEnd = 0;
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag5(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 0; i < 5; i++){
                PentagoBoardState.Piece piece = board.getPieceAt(i, 4 - i);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }


            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }
        }
        return score;
    }

    @SuppressWarnings("Duplicates")
    public static int evalDiag6(PentagoBoardState board){
        int score = 0;
        int consecutive = 0;
        int openEnd = 0;
        int player = board.getTurnPlayer();

        for(int i = 1; i < 6; i++){
                PentagoBoardState.Piece piece = board.getPieceAt(i, 6 - i);
                PentagoBoardState.Piece chess;
                if(myRole == 0){
                    chess = PentagoBoardState.Piece.WHITE;
                }
                else{
                    chess = PentagoBoardState.Piece.BLACK;
                }

                if( piece == chess){
                    consecutive++;
                    //TODO
                    if(consecutive == 5){
                        scores[player] += 50000;
                    }
                }
                else if(piece == PentagoBoardState.Piece.EMPTY && consecutive > 0){
                    openEnd++;
                    score += shapreScore(consecutive, openEnd, player == myRole);
                }
                else if(piece == PentagoBoardState.Piece.EMPTY){
                    openEnd = 1;
                }
                else if(consecutive > 0){
                    score += shapreScore(consecutive, openEnd, player == myRole);
                    consecutive = 0;
                    openEnd = 0;
                }
                else{
                    openEnd = 0;
                }


            if(openEnd > 0){
                score += shapreScore(consecutive, openEnd, player == myRole);
            }

        }
        return score;
    }



}
