package java.engine.board.bitboards;

import java.engine.board.Board;
import java.engine.board.Piece;
import java.engine.board.bitboards.transformers.Rotate180;
import java.engine.board.bitboards.transformers.Transformation;
import java.engine.movegen.Location;
import java.engine.movegen.Move;

import java.util.Optional;
import java.util.function.Function;

import static java.engine.board.Piece.Side.BLACK;
import static java.engine.board.Piece.Side.WHITE;
import static java.engine.board.Piece.Type.*;

/**
 * Created by melvic on 6/25/18.
 */
public class Bitboard implements Board {
    private long[] bitboards = new long[8];
    private Move<Integer> lastMove;

    /**
     * The side-to-move bitboards come first before the piece type ones,
     * so we need to prepare an offset.
     */
    private static final int PIECE_TYPE_OFFSET = 2;

    public Bitboard() { }

    public Bitboard(Bitboard bitboard) {
        this.bitboards = bitboard.getBitboards().clone();
    }

    public Bitboard(Bitboard bitboard, Move<Integer> move) {
        this(bitboard);
        updateByBitboardMove(move);
    }

    public void initialize() {
        clear();

        // Initialize the white pieces
        updateBoard(Piece.white.of(PAWN), bitboard -> bitboard | 0x000000000000ff00L);
        updateBoard(Piece.white.of(KNIGHT), bitboard -> bitboard | 0x0000000000000042L);
        updateBoard(Piece.white.of(BISHOP), bitboard -> bitboard | 0x0000000000000024L);
        updateBoard(Piece.white.of(ROOK), bitboard -> bitboard | 0x0000000000000081L);
        updateBoard(Piece.white.of(QUEEN), bitboard -> bitboard | 0x0000000000000008L);
        updateBoard(Piece.white.of(KING), bitboard -> bitboard | 0x0000000000000010L);

        // Rotate each of the white pieces' initial position to get the one
        // for the corresponding black piece.
        Transformation transformation = new Rotate180();
        for (int i = 0; i < 6; i++) {
            Piece.Type type = Piece.Type.values()[i];
            long pieceTypeBoard = getPieceTypeBoard(type);
            updateBoard(Piece.black.of(type),
                    bitboard -> bitboard | transformation.apply(pieceTypeBoard));
        }

        // Swap the black king and the black queen
        Function<Long, Long> toggleKingQueen = bitboard -> bitboard ^ 0x1800000000000000L;
        updatePieceTypeBoard(QUEEN, toggleKingQueen);
        updatePieceTypeBoard(KING, toggleKingQueen);
    }

    public long getPieceBoard(Piece piece) {
        return getPieceTypeBoard(piece.getType()) & getSideBoard(piece.getSide());
    }

    public long getPieceTypeBoard(Piece.Type type) {
        return bitboards[type.ordinal() + PIECE_TYPE_OFFSET];
    }

    public long getSideBoard(Piece.Side sideToMove) {
        return bitboards[sideToMove.ordinal()];
    }

    public void updateBoard(Piece piece, Function<Long, Long> operation) {
        updatePieceTypeBoard(piece.getType(), operation);
        updateSideBoard(piece.getSide(), operation);
    }

    public void updatePieceTypeBoard(Piece.Type type, Function<Long, Long> operation) {
        bitboards[type.ordinal() + PIECE_TYPE_OFFSET] = operation.apply(getPieceTypeBoard(type));
    }

    public void updateSideBoard(Piece.Side side, Function<Long, Long> operation) {
        bitboards[side.ordinal()] = operation.apply(getSideBoard(side));
    }

    public long getOccupied() {
        return getSideBoard(WHITE) | getSideBoard(BLACK);
    }

    public long getOpponents(Piece.Side sideToMove) {
        return getSideBoard(sideToMove.opposite());
    }

    public long getEmptySquares() {
        return ~getOccupied();
    }

    public long getPawns(Piece.Side side) {
        return getPieceBoard(Piece.build(side).of(PAWN));
    }

    public long getKnights(Piece.Side side) {
        return getPieceBoard(Piece.build(side).of(KNIGHT));
    }

    public long getBishops(Piece.Side side) {
        return getPieceBoard(Piece.build(side).of(BISHOP));
    }

    public long getRooks(Piece.Side side) {
        return getPieceBoard(Piece.build(side).of(ROOK));
    }

    public long getQueen(Piece.Side side) {
        return getPieceBoard(Piece.build(side).of(QUEEN));
    }

    public long getKing(Piece.Side side) {
        return getPieceBoard(Piece.build(side).of(KING));
    }

    @Override
    public Optional<Piece> at(Location location) {
        return at(toBitPosition(location));
    }

    private Optional<Piece> at(int position) {
        long singlePieceBoard = singleBitset(position);
        for (int i = PIECE_TYPE_OFFSET; i < bitboards.length; i++) {
            if (isEmptySet(bitboards[i] & singlePieceBoard)) continue;
            Piece.Type type = Piece.Type.values()[i - PIECE_TYPE_OFFSET];
            Piece.Side side = !isEmptySet(getSideBoard(WHITE) & singlePieceBoard)?
                    WHITE : BLACK;
            Piece piece = Piece.build(side).of(type);
            return Optional.of(piece);
        }
        return Optional.empty();
    }

    @Override
    public int evaluate() {
        return 0;
    }

    @Override
    public void updateByMove(Move<Location> move) {
        int sourcePosition = toBitPosition(move.getSource());
        int destPosition = toBitPosition(move.getDestination());
        updateByBitboardMove(Move.from(sourcePosition).to(destPosition));
    }

    public void updateByBitboardMove(Move<Integer> move) {
        at(move.getSource()).ifPresent(piece -> {
            updateByMove(piece, move.getSource(), move.getDestination());
            this.lastMove = move;
        });
    }

    @Override
    public void clear() {
        for (int i = 0; i < bitboards.length; i++) {
            bitboards[i] = 0;
        }
    }

    public void updateByMove(Piece piece, int source, int destination) {
        long sourcePieceBoard = singleBitset(source);
        long destPieceBoard = singleBitset(destination);
        updateByMove(piece, sourcePieceBoard, destPieceBoard);
    }

    public void updateByMove(Piece piece, long sourcePieceBoard, long destPieceBoard) {
        long moveBoard = sourcePieceBoard ^ destPieceBoard;

        // handle captures
        Piece.Side oppositeSide = piece.getSide().opposite();
        long oppositeSideBoard = getSideBoard(oppositeSide);
        for (int i = PIECE_TYPE_OFFSET; i < bitboards.length; i++) {
            long oppositeBoard = bitboards[i] & oppositeSideBoard;
            if (isNonEmptySet(oppositeBoard & destPieceBoard)) {
                bitboards[i] ^= destPieceBoard;
                updateSideBoard(oppositeSide, bitboard -> bitboard ^ destPieceBoard);
                break;
            }
        }

        // move the piece type and color
        updateBoard(piece, bitboard -> bitboard ^ moveBoard);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                builder.append(stringifySquare(row, col));
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    private String stringifySquare(int row, int col) {
        return at(Board.locate(row, col)).map(piece -> {
            String pieceString = "";

            switch (piece.getType()) {
                case PAWN: pieceString = "P"; break;
                case KNIGHT: pieceString = "N"; break;
                case BISHOP: pieceString = "B"; break;
                case ROOK: pieceString = "R"; break;
                case QUEEN: pieceString = "Q"; break;
                case KING: pieceString = "K"; break;
            }

            if (piece.getSide() == WHITE)
                return 'W' + pieceString;

            return 'B' + pieceString;
        }).orElse("__") + " ";
    }

    public long[] getBitboards() {
        return bitboards;
    }

    public static int toBitPosition(Location location) {
        return location.getFile().ordinal() + location.getRank().ordinal() * SIZE;
    }

    public static long singleBitset(int position) {
        return 1L << position;
    }

    public static boolean isEmptySet(long bitboard) {
        return bitboard == 0;
    }

    public static boolean isNonEmptySet(long bitboard) {
        return !isEmptySet(bitboard);
    }

    public static long leastSignificantOneBit(long bitboard) {
        return bitboard & -bitboard;
    }

    /**
     * Retrieve the index of a 1 bit in a given bitboard.
     * It is assumed that the bitboard contains only one
     * 1 bit, and that the rest are zeroes.
     */
    public static int getPieceIndex(long bitboard) {
        long ls1b = leastSignificantOneBit(bitboard);
        int i = -1;

        while (Bitboard.isNonEmptySet(ls1b)) {
            ls1b >>>= 1;
            i++;
        }

        return i;
    }

    public static Move<Integer> toBitboardMove(Move<Location> move) {
        return Move.from(toBitPosition(move.getSource()))
                .to(toBitPosition(move.getDestination()));
    }
}
