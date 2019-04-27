package student_player;

import boardgame.Move;

import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;

import javax.swing.text.DefaultEditorKit;
import java.util.ArrayList;
import java.util.Random;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260736135");
    }
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...	
        GoodTools.opponentRole = boardState.getOpponent();
        GoodTools.myRole = 1 - boardState.getOpponent();
        SadTools.opponentRole = boardState.getOpponent();
        SadTools.myRole = 1 - boardState.getOpponent();


        Move myMove = null;
//        if(boardState.getTurnNumber() == 0){
//            if(boardState.isPlaceLegal(new PentagoCoord(1, 1))) {
//                myMove = new PentagoMove(1, 1, PentagoBoardState.Quadrant.TL, PentagoBoardState.Quadrant.TR, SadTools.myRole);
//            }
//            else if(boardState.isPlaceLegal(new PentagoCoord(1, 4))){
//                myMove = new PentagoMove(1, 4, PentagoBoardState.Quadrant.TL, PentagoBoardState.Quadrant.TR, SadTools.myRole);
//            }
//            else if(boardState.isPlaceLegal(new PentagoCoord(4, 1))){
//                myMove = new PentagoMove(4, 1, PentagoBoardState.Quadrant.TL, PentagoBoardState.Quadrant.TR, SadTools.myRole);
//            }
//            else if (boardState.isPlaceLegal(new PentagoCoord(4, 4))){
//                myMove = new PentagoMove(4, 4, PentagoBoardState.Quadrant.TL, PentagoBoardState.Quadrant.TR, SadTools.myRole);
//            }
//            else{
//                myMove = SadTools.getMove(boardState, -999999, 999999, 3);
//            }
//        }
////        myMove = GoodTools.getMove(boardState, -999999, 999999, 5);
//        else {
        if(boardState.getTurnNumber() < 5) {
            myMove = SadTools.getMove(boardState, -9999999, 9999999, 2);
        }
        else if (boardState.getTurnNumber() < 10){
        	myMove = SadTools.getMove(boardState, -9999999, 9999999, 3);
        }
        else if (boardState.getTurnNumber() < 15) {
        	myMove = SadTools.getMove(boardState, -9999999, 9999999, 4);
        }
        else {
        	myMove = SadTools.getMove(boardState, -9999999, 9999999, 5);
        }
//        }
//        myMove = SadTools.getMove(boardState, -999999, 999999, 3);
        return myMove;
    }
}