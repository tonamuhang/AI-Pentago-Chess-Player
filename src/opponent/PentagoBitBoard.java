package opponent;

import boardgame.Board;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static pentago_swap.PentagoBoardState.*;

/**
 * A class to more efficiently represent the state an actions applicable to a PentagoBoardState
 */
public class PentagoBitBoard {

	private static final int NUM_QUADS = 4;
	private static final int MAX_TURNS = 36;
	private static final int QUAD_SIZE = 3;

	// Supposedly faster than java.util.Random
	// https://lemire.me/blog/2016/02/01/default-random-number-generators-are-slow/
	private static final ThreadLocalRandom rand = ThreadLocalRandom.current();

	static final byte DRAW = Byte.MAX_VALUE;
	private static final byte NOBODY = Byte.MAX_VALUE - 1;

	/**
	 * Pieces are stored in two long values where a bit set (1) indicates a piece is present at that location.
	 * <pre>
	 * Pentago Board:               Pieces are stored left to right using the following pattern:
	 * -------------
	 * |r 1 r|1 r 1|                 |           Unused           |Row 1 |Row 2 |Row 3 |Row 4 |Row 5 |Row 6 |
	 * |r 2 r|2 r 2|                 |----------------------------|------|------|------|------|------|------|
	 * |r 3 r|3 r 3|    pieces[i] =  |............................|r1r1r1|r2r2r2|r3r3r3|r4r4r4|r5r5r5|r6r6r6|
	 * |-----|-----|
	 * |r 4 r|4 r 4|
	 * |r 5 r|5 r 5|
	 * |r 6 r|6 r 6|
	 * -------------
	 * </pre>
	 * pieces[0] are for White Placements
	 * pieces[1] are for Black Placements
	 *
	 */
	private long[] pieces;

	private byte turnPlayer;
	private byte turnNumber;
	private byte winner;


	/**
	 * Bit mask to access each quadrant separately
	 */
	private static final long[] QUADRANT_MASKS = {
		0b111000111000111000000000000000000000L, // Quadrant 0
		0b000111000111000111000000000000000000L, // Quadrant 1
		0b000000000000000000111000111000111000L, // Quadrant 2
		0b000000000000000000000111000111000111L  // Quadrant 3
	};

	/**
	 * Stores all the possible arrangements of a winning set of pieces
	 */
	private static final long[] WINNING_MASKS = new long[32];

	// Generates the WINNING_MASKS
	static {
		// Generate the rows
		long baseRowMask = 0b111110000000000000000000000000000000L;
		int i = 0;
		WINNING_MASKS[i++] = baseRowMask;
		for(; i < 6; i++) {
			baseRowMask = baseRowMask >> 6;
			WINNING_MASKS[i] = baseRowMask;
		}
		baseRowMask = baseRowMask >> 1;
		WINNING_MASKS[i++] = baseRowMask;
		for(; i < 12; i++) {
			baseRowMask = baseRowMask << 6;
			WINNING_MASKS[i] = baseRowMask;
		}

		// Generate the columns
		long baseColumnMask = 0b100000100000100000100000100000000000L;
		WINNING_MASKS[i++] = baseColumnMask;
		for(; i < 24; i++) {
			baseColumnMask = baseColumnMask >> 1;
			WINNING_MASKS[i] = baseColumnMask;
		}

		// Diagonal masks (hardcoded since the logic to generate them would be too complex)
		WINNING_MASKS[24] = 0b100000010000001000000100000010000000L;
		WINNING_MASKS[25] = 0b010000001000000100000010000001000000L;
		WINNING_MASKS[26] = 0b000000010000001000000100000010000001L;
		WINNING_MASKS[27] = 0b000000100000010000001000000100000010L;
		WINNING_MASKS[28] = 0b000001000010000100001000010000000000L;
		WINNING_MASKS[29] = 0b000010000100001000010000100000000000L;
		WINNING_MASKS[30] = 0b000000000010000100001000010000100000L;
		WINNING_MASKS[31] = 0b000000000001000010000100001000010000L;

	}

	// Didn't end up using this but it contains all configurations that are almost a win (one placement away after quadrant swap)
	private static final Long[] oneAwayMasks;

	// Generates the oneAwayMasks (without duplicates, that's why I use a HashSet)
	static {

		HashSet<Long> oneAwaySet = new HashSet<>();

		for(long winningMask : WINNING_MASKS) {
			long oneBitMask = 1;
			for(int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
				if((winningMask & ~oneBitMask) != winningMask) {
					oneAwaySet.add(winningMask & ~oneBitMask);
				}
				oneBitMask = oneBitMask << 1;
			}
		}

		oneAwayMasks = oneAwaySet.toArray(new Long[0]);
	}


	/**
	 * All possible non-symmetric swaps of a quadrant
	 */
	public static final byte[][] QUAD_SWAPS = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};

	/**
	 * Maps a source and destination quadrant to
	 * how many bits need to be shifted to get from the source quadrant to the destination quadrant
	 * The -1 are dummy values that should not be used
	 */
	private static final int[][] QUADRANT_BIT_SHIFTS = {
		{-1, 3, 18, 21},  // From Quad 0 -> Quad 1 is 3 bits, Quad 0 -> Quad 2 is 18 bits and Quad 0 -> Quad 3 is 21 bits
		{-1,-1, 15, 18},  // From Quad 1 -> Quad 2 is 15 bits, Quad 1 -> Quad 3 is 18 bits
		{-1,-1,-1, 3},    // From Quad 2 -> Quad 3 is 3 bits
		{-1,-1,-1, 0}     // From Quad 3 -> Quad 3 is 0 bits (not used as an actual swap since it would be invalid)
	};


	/**
	 * Constructor for conversion of PentagoBoardState to a PentagoBitBoard
	 * @param board
	 */
	PentagoBitBoard(PentagoBoardState board) {

		this.pieces = new long[2];

		// Get the pieces
		for(int x = 0; x < BOARD_SIZE; x++) {
			for(int y = 0; y < BOARD_SIZE; y++) {

				//Shift the last iteration's pieces left
				this.pieces[BLACK] = this.pieces[BLACK] << 1;
				this.pieces[WHITE] = this.pieces[WHITE] << 1;

				PentagoBoardState.Piece p = board.getPieceAt(x, y);
				if(p == PentagoBoardState.Piece.BLACK) {
					this.pieces[BLACK] = this.pieces[BLACK] | 1;
				} else if (p == PentagoBoardState.Piece.WHITE) {
					this.pieces[WHITE] = this.pieces[WHITE] | 1;
				}

			}
		}

		this.turnPlayer = (byte) board.getTurnPlayer();

		// NOTE I use turn number as number of plays on the board (makes for faster allocation of legal moves array)
		this.turnNumber = (byte) (board.getTurnNumber() * 2 + this.turnPlayer);
		if(board.getWinner() == Board.DRAW) {
			this.winner = DRAW;
		} else if(board.getWinner() == Board.NOBODY) {
			this.winner = NOBODY;
		} else {
			this.winner = (byte) board.getWinner();
		}
	}


	/**
	 * Creates a blank PentagoBitBoard for debugging purposes
	 */
	private PentagoBitBoard() {
		this.pieces = new long[2];
		this.winner = NOBODY;
	}

	/**
	 * Constructor for Cloning
	 * @param board Existing board
	 */
	private PentagoBitBoard(PentagoBitBoard board) {
		this.pieces = new long[2];
		this.pieces[0] = board.pieces[0];
		this.pieces[1] = board.pieces[1];
		this.winner = board.winner;
		this.turnPlayer = board.turnPlayer;
		this.turnNumber = board.turnNumber;
	}

	@Override
	public Object clone() {
		return new PentagoBitBoard(this);
	}

	/**
	 * Creates a PentagoBitBoard with preset properties
	 * @param pieces Black and white piece placements
	 * @param winner Current winner of the state
	 * @param turnPlayer Current player
	 * @param turnNumber Current turn number
	 */
	// For Debug
	private PentagoBitBoard(long[] pieces, byte winner, byte turnPlayer, byte turnNumber) {
		this.pieces = pieces;
		this.winner = winner;
		this.turnPlayer = turnPlayer;
		this.turnNumber = turnNumber;
	}

	/**
	 * Returns all legal moves as a long:
	 * <pre>
	 *|                       |P|     |                                    |
	 *|                       |I|4 bit|                                    |
	 *|        unused         |D|quads|       36 bits for coordinate       |
	 *----------------------------------------------------------------------
	 *|.......................|p|sq|lq|cccccccccccccccccccccccccccccccccccc|
	 * </pre>
	 * 	Note that sq must be less than lq
	 *
	 * @param availableSpots Specifies which locations on the board this method should generate moves for
	 * @param quadrantSwaps Specifies the domain of quadrant swaps that should be used for move generation
	 * @param intialSize Provide an expected size of moves such that the returned ArrayList is populated more efficiently
	 *
	 * @return All legal moves constrained to positions in availableSwaps and swaps in quadrantSwaps as longs
	 */
	ArrayList<Long> getAllLegalMoves(long availableSpots, byte[][] quadrantSwaps, int intialSize) {

		ArrayList<Long> moves = new ArrayList<>(intialSize);

		long mask = 1;
		for(int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
			if((availableSpots & mask) == mask) {
				for(byte[] quadrantSwap : quadrantSwaps) {
					moves.add((((((((long)turnPlayer << 2) | quadrantSwap[0]) << 2) | quadrantSwap[1]) << 36) | mask));
				}
			}
			mask = mask << 1;
		}

		return moves;
	}

	/**
	 * Efficiently attempts to play a random position on the board until a legal placement is found then selects
	 * a random quadrant swap.
	 *
	 * @return A random legal move
	 */
	long getRandomMove() {

		long availableSpots = ~(this.pieces[WHITE] | this.pieces[BLACK]);

		long move;
		// Pick a random empty coordinate
		do {
			move = 1L << rand.nextInt(BOARD_SIZE * BOARD_SIZE);
		} while((move & availableSpots) != move);

		// Pick random quadrant swap
		byte[] swaps = QUAD_SWAPS[rand.nextInt(QUAD_SWAPS.length)];

		int largerQuad = swaps[1];
		int smallerQuad = swaps[0];

		return ((((((long)turnPlayer << 2) | smallerQuad) << 2) | largerQuad) << 36) | move;
	}


	/**
	 * Generates all legal moves available in this state ignoring moves that are symmetric:
	 * Symmetry is identified by examining the number of identical quadrants to determine
	 * a smaller subset of all legal moves to generate
	 *
	 * @return All legal moves ignoring symmetric moves
	 */
	ArrayList<Long> getAllLegalNonSymmetricMoves() {

		// Identify which quadrants are identical
		ArrayList<ArrayList<Byte>> equalQuadrants = partitionQuadrants();

		ArrayList<Long> moves;

		byte[][] swaps = QUAD_SWAPS;
		long placements = ~(this.pieces[WHITE] | this.pieces[BLACK]);
		int initialCapacity = ((BOARD_SIZE * BOARD_SIZE) - this.turnNumber) * swaps.length;

		switch (equalQuadrants.size()) {

			case 1:
				// 4 identical quadrants (Q0=Q1=Q2=Q3):
				// Try a move for each free spot in Q0 and just swap Q1 -> Q2

				byte Q0 = equalQuadrants.get(0).get(0);

				placements = placements & QUADRANT_MASKS[Q0];
				swaps = new byte[][]{{1, 2}};
				initialCapacity = (QUAD_SIZE * QUAD_SIZE) * swaps.length;

				moves = getAllLegalMoves(placements, swaps, initialCapacity);
				break;

			case 2:
				// 2 pairs of identical quadrants (Q0=Q2, Q1=Q3):
				// Try a move for each free spot in Q0 and Q1 and do all the swaps
				if (equalQuadrants.get(0).size() == 2) {

					Q0 = equalQuadrants.get(0).get(0);
					byte Q1 = equalQuadrants.get(1).get(0);

					placements = placements & (QUADRANT_MASKS[Q0] | QUADRANT_MASKS[Q1]);
					initialCapacity = (QUAD_SIZE * QUAD_SIZE) * 2 * swaps.length;

					moves = getAllLegalMoves(placements, swaps, initialCapacity);
				}
				// 1 unique, 3 identical quadrants(Q0, Q1=Q2=Q3):
				// Try a move for each free spot in Q0 and do a swap for Q0 -> Q1, Q0 -> Q2, Q0 -> Q2, Q1 -> Q2
				// And try a move for each free spot in Q1 and do all the swaps
				else {

					ArrayList<Byte> uniqueQuadrant = equalQuadrants.get(0).size() < equalQuadrants.get(1).size() ? equalQuadrants.get(0) : equalQuadrants.get(1);
					ArrayList<Byte> identicalQuadrants = equalQuadrants.get(0).size() > equalQuadrants.get(1).size() ? equalQuadrants.get(0) : equalQuadrants.get(1);

					// Generate moves for the unique quadrant
					long placementsQ0 = placements & QUADRANT_MASKS[uniqueQuadrant.get(0)];
					byte[][] swapsQ0 = new byte[4][2];
					int initialCapacityQ0 = (QUAD_SIZE * QUAD_SIZE) * swaps.length;

					Q0 = equalQuadrants.get(0).get(0);

					// Generate first 3 swaps
					for(int i = 0; i < identicalQuadrants.size(); i++) {
						byte quadrant = identicalQuadrants.get(i);
						byte largerQuadrant = quadrant > Q0 ? quadrant : Q0;
						byte smallerQuadrant = quadrant < Q0 ? quadrant : Q0;
						swapsQ0[i][0] = smallerQuadrant;
						swapsQ0[i][1] = largerQuadrant;
					}

					// Generate last swap
					byte largerQuadrant = identicalQuadrants.get(0) > identicalQuadrants.get(1) ? identicalQuadrants.get(0) : identicalQuadrants.get(1);
					byte smallerQuadrant = identicalQuadrants.get(0) < identicalQuadrants.get(1) ? identicalQuadrants.get(0) : identicalQuadrants.get(1);
					swapsQ0[swapsQ0.length-1][0] = smallerQuadrant;
					swapsQ0[swapsQ0.length-1][1] = largerQuadrant;

					moves = getAllLegalMoves(placementsQ0, swapsQ0, initialCapacityQ0);

					// Next generate moves for one of the 3 identical quadrants
					long placementsQ1 = placements & QUADRANT_MASKS[identicalQuadrants.get(0)];
					byte[][] swapsQ1 = swaps;
					int initialCapacityQ1 = (QUAD_SIZE * QUAD_SIZE) * swapsQ1.length;

					moves.addAll(getAllLegalMoves(placementsQ1, swapsQ1, initialCapacityQ1));
				}

				break;

			// Default is to return all legal moves as usual
			default:
				moves = getAllLegalMoves(placements, swaps, initialCapacity);
				break;
		}

		return moves;
	}

	/**
	 * Splits quadrants into partitions where quadrants in the same partition are identical.
	 * Useful for identifying symmetric moves.
	 *
	 * Examples:
     * <pre>
     * Q0 = Q1 =/= Q2 = Q3      Q0 =/= Q1 = Q2 = Q3      Q0 = Q1 = Q2 = Q3
     * {                        {                        {
     *     {0, 1},                     {0},                     {0, 1, 2, 3}
     *     {2, 3}                      {1, 2, 3}         }
     * }                        }
     * </pre>
	 *
	 * @return An Array List of partitions where each partition contains identical quadrants.
	 */
	private ArrayList<ArrayList<Byte>> partitionQuadrants() {

		// Put each quadrant (the two piece longs) into a list
		ArrayList<ArrayList<Long>> quadrants = new ArrayList<>(NUM_QUADS);

		for(int i = 0; i < NUM_QUADS; i++) {
			quadrants.add(new ArrayList<>());
			for(long pieceLong : this.pieces) {
				// Shift all quadrants to the same bit position
				quadrants.get(i).add(pieceLong >> QUADRANT_BIT_SHIFTS[i][NUM_QUADS-1]);
			}
		}

		// Partition quadrants into buckets where they are equal
		HashMap<ArrayList<Long>, ArrayList<Byte>> equalQuadrants = new HashMap<>();

		for(byte j = 0; j < NUM_QUADS; j++) {

			// Found a similar quadrant, add quadrant number to existing partition
			if(equalQuadrants.containsKey(quadrants.get(j))) {
				equalQuadrants.get(quadrants.get(j)).add(j);
			}
			// New unique quadrant, create a new partition with the quadrant number
			else {
				ArrayList<Byte> newPartition = new ArrayList<>();
				newPartition.add(j);
				equalQuadrants.put(quadrants.get(j), newPartition);
			}
		}

		return new ArrayList<>(equalQuadrants.values());
	}

	/**
	 * Swaps two quadrants on the board. Note that smallerQuad < largerQuad. No legality is checked on this for efficiency
	 *
	 * @param smallerQuad First quadrant to swap
	 * @param largerQuad Second quadrant to swap
	 */
	private void swapQuadrants(int smallerQuad, int largerQuad) {

		for(int i = 0; i < this.pieces.length; i++) {
			// Shift the smaller quad down to make for the new larger quad

			long newLarge = (this.pieces[i] & QUADRANT_MASKS[smallerQuad]) >> QUADRANT_BIT_SHIFTS[smallerQuad][largerQuad];

			// Shift the larger quad up to make the new smaller quad
			long newSmall = (this.pieces[i] & QUADRANT_MASKS[largerQuad]) << QUADRANT_BIT_SHIFTS[smallerQuad][largerQuad];

			// Perform the update
			long newConfig = newLarge | newSmall;
			long updateMask = ~ (QUADRANT_MASKS[smallerQuad] | QUADRANT_MASKS[largerQuad]);

			this.pieces[i] = (this.pieces[i] & updateMask) | newConfig;
		}
	}


	/**
	 * Applies a move to the current board state. Note that legality is not checked here for efficiency
	 * as it is assumed that the move applied was generated from getAllLegalNonSymmetricMoves() or similar legal
	 * move generator.
	 *
	 * @param move The next move to play
	 */
	void processMove(long move) {

		//Extract info from move
		int player = (int) ((move >> 40) & 1);
		int smallerQuad = (int) ((move >> 38) & 0b11);
		int largerQuad = (int) ((move >> 36) & 0b11);
		long coord = move & 0b111111111111111111111111111111111111L;

		//Place the coordinate based on player
		this.pieces[player] = this.pieces[player] | coord;
		this.swapQuadrants(smallerQuad, largerQuad);
		this.turnNumber++;

		this.updateWinner();

		this.turnPlayer = (byte) (1 - this.turnPlayer);

	}


	/**
	 * Reverses the effect of a recently placed move. Note that legality is not checked for efficiency as it is assumed
	 * that the move to reverse was most recently applied
	 * @param move the move to undo
	 */
	void undoMove(long move) {

		//Extract info from move
		int player = (int) ((move >> 40) & 1);
		int smallerQuad = (int) ((move >> 38) & 0b11);
		int largerQuad = (int) ((move >> 36) & 0b11);
		long coord = move & 0b111111111111111111111111111111111111L;

		// Re-swap the quadrants
		this.swapQuadrants(smallerQuad, largerQuad);

		// Undo the placement
		this.pieces[player] = this.pieces[player] & ~coord;
		this.turnNumber--;

		this.updateWinner();

		this.turnPlayer = (byte) (1 - this.turnPlayer);
	}

	/**
	 * Checks if the board is in a winning configuration and updates the winner variable if so.
	 */
	private void updateWinner() {
		boolean playerWin = checkWin(this.turnPlayer);
		boolean otherWin = checkWin((byte) (1 - this.turnPlayer));

		if (playerWin) { // Current player has won
			this.winner = otherWin ? DRAW : this.turnPlayer;
		} else if (otherWin) { // Player's move caused the opponent to win
			this.winner = (byte) (1 - this.turnPlayer);
		} else if (gameOver()) {
			this.winner = DRAW;
		}
	}

	/**
	 * Helper method for updateWinner()
	 * @param turnPlayer which player to check win for
	 * @return true if turnPlayer has five pieces in a row, false otherwise
	 */
	private boolean checkWin(byte turnPlayer) {
		for(long mask: WINNING_MASKS) {
			if((mask & this.pieces[turnPlayer]) == mask) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the game is over
	 * @return true if game is over, false if still ongoing
	 */
	boolean gameOver() {
		return (this.turnNumber >= MAX_TURNS) || this.winner != NOBODY;
	}

	/**
	 * Converts an col, row position to a placement in a bit move.
	 *
	 * @param col column position (from 0 to 5 inclusive)
	 * @param row row position (from 0 to 5 inclusive
	 * @return long with placement bit at correct position
	 */
	private static long colRowToLong(int col, int row) {

		// Get col in correct position:
		long bit = (1 << (BOARD_SIZE - 1 - col));

		// Get row in correct position
		bit = bit << (BOARD_SIZE * (BOARD_SIZE - 1 - row));

		return bit;
	}

	/**
	 * Extracts the column and row number placement of a move long.
	 *
	 * @param move The move to extract
	 * @return integer array containing coordinates
	 */
	private static int[] bitToColRow(long move) {

		int bitPosition = 0;
		long mask = 1;
		while((move & mask) != mask) {
			bitPosition++;
			mask = mask << 1;
		}

		int col = (BOARD_SIZE*BOARD_SIZE - 1 - bitPosition) % BOARD_SIZE;
		int row = (BOARD_SIZE*BOARD_SIZE - 1 - bitPosition) / BOARD_SIZE;

		return new int[] {col, row};
	}

	/**
	 * Coverts a move long to a PentagoMove object
	 * @param move The move long to convert
	 * @return a new equivalent Pentago move
	 */
	static PentagoMove longToPentagoMove(long move) {

		int player = (int) ((move >> 40) & 1);
		int smallerQuad = (int) ((move >> 38) & 0b11);
		int largerQuad = (int) ((move >> 36) & 0b11);
		long coord = move & 0b111111111111111111111111111111111111L;

		int[] coordColRow = bitToColRow(coord);

		// I used a different coordinate system so return row -> y, col -> x
		return new PentagoMove(coordColRow[1], coordColRow[0], Quadrant.values()[smallerQuad], Quadrant.values()[largerQuad], player);
	}

	long[] getPieces() {
		return this.pieces.clone();
	}

	byte getWinner() {
		return winner;
	}

	byte getOpponent() {
		return (byte) (1 - this.turnPlayer);
	}

	void togglePlayer() {
		this.turnPlayer = getOpponent();
	}

	byte getTurnNumber() {
		return this.turnNumber;
	}

	byte getTurnPlayer() {
		return this.turnPlayer;
	}

	@Override
	public String toString() {
		StringBuilder boardString = new StringBuilder();
		String rowMarker = "--------------------------\n";
		boardString.append(rowMarker);
		for(int y = 0; y < BOARD_SIZE; y++) {
			boardString.append("|");
			for(int x = 0; x < BOARD_SIZE; x++) {
				boardString.append(" ");

				long xy = colRowToLong(x, y);
				if((pieces[WHITE] & xy) == xy) {
					boardString.append('W');
				} else if((pieces[BLACK] & xy) == xy) {
					boardString.append('B');
				} else {
					boardString.append(" ");
				}

				boardString.append(" |");
				if(x == BOARD_SIZE/2 - 1) {
					boardString.append("|");
				}
			}
			boardString.append("\n");
			if(y == BOARD_SIZE/2 - 1) {
				boardString.append(rowMarker);
			}
		}
		boardString.append(rowMarker);
		return boardString.toString();
	}

	public static void main(String[] args) {
		maskTest();
		countWinsByPlacementType();
		PentagoBitBoard pbs = new PentagoBitBoard();

		Scanner scanner = new Scanner(System.in);
		int id = 0;
		while(pbs.winner == NOBODY) {
			System.out.print("Enter move (x y a b): ");
			String moveStr = scanner.nextLine();
			String[] moveStrs = moveStr.split(" ");

			long m = ((((((long)id << 2) | Integer.parseInt(moveStrs[2])) << 2) | Integer.parseInt(moveStrs[3])) << 36) | colRowToLong(Integer.parseInt(moveStrs[0]), Integer.parseInt(moveStrs[1]));

			pbs.processMove(m);
			System.out.println(pbs);
			id = 1 - id;
		}

		switch(pbs.winner) {
			case WHITE:
				System.out.println("White wins.");
				break;
			case BLACK:
				System.out.println("Black wins.");
				break;
			case DRAW:
				System.out.println("Draw.");
				break;
			case NOBODY:
				System.out.println("Nobody has won.");
				break;
			default:
				System.out.println("Unknown error.");
		}
	}

	private static void maskTest() {
		System.out.println("Winning Masks:");
		for (long mask: WINNING_MASKS) {
			System.out.println(new PentagoBitBoard(new long[] {mask, 0L}, (byte)0, (byte)0, (byte)0));
		}
		System.out.println("One Away Masks:");
		for (long mask: oneAwayMasks) {
			System.out.println(new PentagoBitBoard(new long[] {mask, 0L}, (byte)0, (byte)0, (byte)0));
		}
	}

	private static void countWinsByPlacementType() {
		long centerPiece = 0b000000010000000000000000000000000000L;
		long cornerPiece = 0b100000000000000000000000000000000000L;
		long edgePiece = 0b010000000000000000000000000000000000L;

		int[] winCounts = {0,0,0};

		for (long mask : WINNING_MASKS) {
			if((mask & centerPiece) == centerPiece) {
				winCounts[0]++;
			} else if ((mask & cornerPiece) == cornerPiece) {
				winCounts[1]++;
			} else if ((mask & edgePiece) == edgePiece) {
				winCounts[2]++;
			}
		}

		System.out.println("Center Piece wins: " + winCounts[0]);
		System.out.println("Corner Piece wins: " + winCounts[1]);
		System.out.println("Edge Piece wins: " + winCounts[2]);
	}
}