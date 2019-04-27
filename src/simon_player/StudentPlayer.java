package simon_player;

import java.util.*;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import pentago_swap.SimonPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends SimonPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("SimonBot");
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
        //MyTools.getSomething();
    	PentagoBoardState mybs = (PentagoBoardState) boardState.clone();
    	int myPlayerID = mybs.getTurnPlayer();
    	
    	ArrayDeque<PGPair<Move, PentagoBoardState>> allmoveboards = new ArrayDeque<PGPair<Move, PentagoBoardState>>();
    	
    	ArrayList<PentagoMove> allMoves = mybs.getAllLegalMoves();
    	for (PentagoMove pm : allMoves) {
    		PentagoBoardState newpbs = (PentagoBoardState) mybs.clone();
    		newpbs.processMove(pm);
    		if (newpbs.gameOver() && newpbs.getWinner() != myPlayerID) {
    			if (newpbs.getWinner() == Board.DRAW) {
    				allmoveboards.addLast(PGPair.of(pm, newpbs));
    			}
    		} else if (newpbs.gameOver() && newpbs.getWinner() == myPlayerID) {
    			allmoveboards.addFirst(PGPair.of(pm, newpbs));
    			break;
    		} else {
    			allmoveboards.add(PGPair.of(pm, newpbs));
    		}
    	}
    	

    	// evaluate all moveboards for best move.
    	int maxBoardValue = 0;
    	PGPair<Move, PentagoBoardState> bestBoard = allmoveboards.getFirst();
    	for (PGPair<Move, PentagoBoardState> moveRes : allmoveboards) {
    		PentagoBoardState checkpbs = moveRes.getValue();
    		if (checkpbs.gameOver() && checkpbs.getWinner() == myPlayerID) {
    			return moveRes.getKey();
    		} else {
    			int boardval = MyTools.CalculateBoardValue(checkpbs);
    			if (boardval > maxBoardValue) {
    				maxBoardValue = boardval;
    				bestBoard = moveRes;
    			}
    		}
    	}
    	return bestBoard.getKey();
    	
    	
    	
    	

    }
}