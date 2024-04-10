package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class Pawn extends ChessPiece {
    private ChessMatch chessMatch;

    public Pawn(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];

        Position p = new Position(0, 0);

        if (getColor() == Color.WHITE) {
            p.setValues(position.getRow() -1, position.getColumn());
            if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }
            p.setValues(position.getRow() -2, position.getColumn());
            Position p2 = new Position(position.getRow() -1, position.getColumn());
            if (getBoard().positionExists(p)
                    && !getBoard().thereIsAPiece(p)
                    && getBoard().positionExists(p2)
                    && !getBoard().thereIsAPiece(p2)
                    && getMoveCount() == 0)
            {
                mat[p.getRow()][p.getColumn()] = true;
            }
            p.setValues(position.getRow() -1, position.getColumn() -1);
            if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }
            p.setValues(position.getRow() -1, position.getColumn() +1);
            if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }

            // Special move enPassant WHITE
            if (position.getRow() == 3) {
                Position leftPosition = new Position(position.getRow(), position.getColumn() -1);
                if (getBoard().positionExists(leftPosition)
                        && isThereOpponentPiece(leftPosition)
                        && getBoard().piece(leftPosition) == chessMatch.getEnPassantVulnerable())
                {
                    mat[leftPosition.getRow() -1][leftPosition.getColumn()] = true;
                }

                Position rightPosition = new Position(position.getRow(), position.getColumn() +1);
                if (getBoard().positionExists(rightPosition)
                        && isThereOpponentPiece(rightPosition)
                        && getBoard().piece(rightPosition) == chessMatch.getEnPassantVulnerable())
                {
                    mat[rightPosition.getRow() -1][rightPosition.getColumn()] = true;
                }
            }

        } else {
            p.setValues(position.getRow() +1, position.getColumn());
            if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }
            p.setValues(position.getRow() +2, position.getColumn());
            Position p2 = new Position(position.getRow() +1, position.getColumn());
            if (getBoard().positionExists(p)
                    && !getBoard().thereIsAPiece(p)
                    && getBoard().positionExists(p2)
                    && !getBoard().thereIsAPiece(p2)
                    && getMoveCount() == 0)
            {
                mat[p.getRow()][p.getColumn()] = true;
            }
            p.setValues(position.getRow() +1, position.getColumn() -1);
            if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }
            p.setValues(position.getRow() +1, position.getColumn() +1);
            if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }

            // Special move enPassant BLACK
            if (position.getRow() == 4) {
                Position leftPosition = new Position(position.getRow(), position.getColumn() -1);
                if (getBoard().positionExists(leftPosition)
                        && isThereOpponentPiece(leftPosition)
                        && getBoard().piece(leftPosition) == chessMatch.getEnPassantVulnerable())
                {
                    mat[leftPosition.getRow() +1][leftPosition.getColumn()] = true;
                }

                Position rightPosition = new Position(position.getRow(), position.getColumn() +1);
                if (getBoard().positionExists(rightPosition)
                        && isThereOpponentPiece(rightPosition)
                        && getBoard().piece(rightPosition) == chessMatch.getEnPassantVulnerable())
                {
                    mat[rightPosition.getRow() +1][rightPosition.getColumn()] = true;
                }
            }
        }
        return mat;
    }
    @Override
    public String toString() {
        return "P";
    }
}