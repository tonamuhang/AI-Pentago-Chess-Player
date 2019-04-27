package opponent;

import java.util.Stack;

import static student_player.PentagoBitBoard.DRAW;

/**
 * Class representing an Upper Confidence Tree (UCT) for use in Monte Carlo Tree search.
 * Note that in order to save on memory, only moves are store in each node. To get the current state at a given node
 * Moves are applied form the root to the current node to generate this state
 */
class UCTNode {

	private int winScore;
	private int numSims;

	private long move;

	private UCTNode parent;
	private UCTNode[] children;

	private static final double EXPLOITATION_PARAM = Math.sqrt(2);

	UCTNode(long move) {
		this.move = move;
	}

	/**
	 * Backpropagates the result of a default policy simulation back up to the root node. Note a win increments win
	 * score by 2, a draw increments by 1 and a loss increments by 0. Each simulation increments numSims by 2. (such
	 * that it doesn't appear that the win/sim ratio is twice what it is supposed to be
	 *
	 * @param result the result of the default policy
	 */
	void backPropagate(byte[] result) {

		UCTNode currentNode = this;
		// Continue to root
		while(currentNode != null) {
			currentNode.numSims += 2;
			// Check if this node won
			if(result[1] == result[0])
				currentNode.winScore += 2;
			else if(result[0] == DRAW)
				currentNode.winScore += 1;

			// Toggle player
			result[1] = (byte) (1 - result[1]);

			// Move to parent
			currentNode = currentNode.parent;
		}
	}

	/**
	 * Calculates the value of this state given it's win score, the number of simulations and the number of simulations
	 * of it's parent
	 * @return this state's value
	 */
	double getStateValue() {

		if (this.numSims == 0)
			return Double.MAX_VALUE;

		return (this.winScore / (double) this.numSims) + EXPLOITATION_PARAM * Math.sqrt(Math.log(this.parent.numSims)/this.numSims);
	}

	/**
	 * Returns the child node that had the most simulations run on it. (this is the most promising next step)
	 * @return child node with most simulations
	 */
	UCTNode getMaxSimsChild() {
		int maxSims = Integer.MIN_VALUE;
		int maxIndex = -1;

		for(int i = 0; i < this.children.length; i++) {
			if(this.children[i].numSims > maxSims) {
				maxSims = this.children[i].numSims;
				maxIndex = i;
			}
		}

		return this.children[maxIndex];
	}

	/**
	 * Generates a PentagoBitBoard state by applying moves from root to this node. (design choice to minimize memory
	 * usage)
	 * @param startState the current state of the game (state at the root).
	 * @return the state at this node
	 */
	PentagoBitBoard getState(PentagoBitBoard startState) {

		// If we are at the root, game state is unchanged
		if(this.move == 0) return startState;

		// Get the chain of moves from the parent to this move
		Stack<Long> moveStack = new Stack<>();
		UCTNode currentNode = this;
		while(currentNode != null && currentNode.move != 0) {
			moveStack.push(currentNode.move);
			currentNode = currentNode.parent;
		}

		// Apply the moves
		PentagoBitBoard endState = (PentagoBitBoard) startState.clone();
		while(!moveStack.isEmpty()) {
			endState.processMove(moveStack.pop());
		}

		return endState;
	}

	void setParent(UCTNode parent) {
		this.parent = parent;
	}

	boolean hasChildren() {
		return !(this.children == null || this.children.length == 0);
	}

	UCTNode[] getChildren() {
		return children;
	}

	UCTNode getRandomChild() {
		return children[StudentPlayer.rng.nextInt(children.length)];
	}

	void setChildren(UCTNode[] children) {
		this.children = children;
	}

	long getMove() {
		return move;
	}

	double getWinScore() {
		return winScore;
	}

	int getNumSims() {
		return numSims;
	}
}