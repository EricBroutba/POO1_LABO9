package chess.engine.pieces;

import chess.PieceType;
import chess.engine.Piece;
import chess.engine.Player;

public class Knight extends Piece {
    public Knight(Player owner) {
        super(PieceType.KNIGHT, owner);
    }

    @Override
    public List<Move> getMoves(int x, int y) {
        return true;
    }
}
