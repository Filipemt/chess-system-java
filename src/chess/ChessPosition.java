package chess;

import boardgame.Position;

public class ChessPosition {

    private Character column;
    private Integer row;

    public ChessPosition(Character column, Integer row) {
        if (column < 'A' || column > 'H' || row < 1 || row > 8) {
            throw new ChessException("Error instantiating ChessPosition: Valid value are from A1 to H8.");
        }
        this.column = column;
        this.row = row;
    }

    public Character getColumn() {
        return column;
    }

    public Integer getRow() {
        return row;
    }

    protected Position toPosition() {
        return new Position(8 - row, column - 'A');
    }

    protected static ChessPosition fromPosition(Position position) {
        return new ChessPosition((char) ('A' - position.getColumn()), 8 - position.getRow());
    }

    @Override
    public String toString() {
        return "" + column + row;
    }
}
