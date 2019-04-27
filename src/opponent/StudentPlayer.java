package opponent;

import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoPlayer;

import java.util.ArrayList;
import java.util.Random;

import static student_player.PentagoBitBoard.longToPentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

	// Create a single random number generator for the program
	static Random rng = new Random();

    private static final int TIMEOUT = 1000;


    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260616162");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */

    public Move chooseMove(PentagoBoardState boardState) {

    	// ----------- Setup -----------
    	long startTime = System.currentTimeMillis();
    	long endTime = startTime + TIMEOUT;

    	// Covert to my bitboard
    	PentagoBitBoard bitBoardState = new PentagoBitBoard(boardState);

    	//------------ Static Strategies ------------

    	// Checks when a win/loss becomes possible
    	if(bitBoardState.getTurnNumber() >= 7) {
    		// If there is a next move that guarantees a move, ignore MCTS and play it
    		long winningMove = StaticStrategies.checkOffensiveMove(bitBoardState);
    		if(winningMove != 0) {
    			System.out.println("Found a winning move!");
				return longToPentagoMove(winningMove);
			}

    		// If there is a next move that blocks an opponent from winning, ignore MCTS and play it
			// TODO some moves still allow the opponent to win
    		long defensiveMove = StaticStrategies.checkDefensiveMove(bitBoardState);
    		if(defensiveMove != 0) {
				System.out.println("Found a defensive move!");
				return longToPentagoMove(defensiveMove);
			}
		}

    	// Towards beginning occupy as many centers as possible
		if(bitBoardState.getTurnNumber() < 3) {
			long centerMove = StaticStrategies.checkCenterPlacement(bitBoardState);
			if(centerMove != 0) {
				System.out.println("Found a center placement!");
				return longToPentagoMove(centerMove);
			}
		}

    	//------------ Begin MCTS ------------

    	UCTNode root = new UCTNode(0L);

		while (System.currentTimeMillis() < endTime) {
			//----------- Descent phase -----------
			UCTNode promisingNode = selectPromisingNode(root);

			//----------- Growth phase ------------
			PentagoBitBoard promisingState = promisingNode.getState(bitBoardState);
			if(!promisingState.gameOver()) {
				expandNode(promisingNode, promisingState);
			}

			//----------- Rollout phase -----------
			UCTNode nodeToExplore = promisingNode;
			if(promisingNode.hasChildren()) {
				nodeToExplore = promisingNode.getRandomChild();
			}
			byte[] result = simulateRandomPlayout(nodeToExplore, bitBoardState);

			//----------- Update phase -----------
			nodeToExplore.backPropagate(result);
		}

		UCTNode finalSelection = root.getMaxSimsChild();
		System.out.println("Number of simulations: " + root.getNumSims()/2);
		return longToPentagoMove(finalSelection.getMove());
    }

	/**
	 * Performs the descent step of MCTS
	 * @param root Root node of UCT
	 * @return the most promising node to expand/explore
	 */
	private UCTNode selectPromisingNode(UCTNode root) {
		UCTNode currentNode = root;

		while (currentNode.hasChildren()) {
			double maxValue = Double.MIN_VALUE;
			int maxIndex = -1;

			for(int i = 0; i < currentNode.getChildren().length; i++) {
				double value = currentNode.getChildren()[i].getStateValue();
				if(value > maxValue) {
					maxValue = value;
					maxIndex = i;
				}
			}

			currentNode = currentNode.getChildren()[maxIndex];
		}
		return currentNode;
	}

	/**
	 * Performs the expansion stage of MCTS
	 * @param growthNode UCTNode to expand
	 * @param startState the current state of the game
	 */
	private void expandNode(UCTNode growthNode, PentagoBitBoard startState) {
		ArrayList<Long> availableMoves = growthNode.getState(startState).getAllLegalNonSymmetricMoves();
		UCTNode[] children = new UCTNode[availableMoves.size()];
		for(int i = 0; i < availableMoves.size(); i++) {
			UCTNode child = new UCTNode(availableMoves.get(i));
			child.setParent(growthNode);
			children[i] = child;
		}
		growthNode.setChildren(children);
	}

	/**
	 * Performs a default policy simulation
	 * @param start Start UCTNode
	 * @param startState the current state of the game
	 * @return Results of the game (and who was last to play)
	 */
	private byte[] simulateRandomPlayout(UCTNode start, PentagoBitBoard startState) {

		PentagoBitBoard state = start.getState(startState);
		while(!state.gameOver()) {
			state.processMove(state.getRandomMove());
		}
		// Returns who won and who was last to play
		return new byte[] {state.getWinner(), state.getOpponent()};
	}
}