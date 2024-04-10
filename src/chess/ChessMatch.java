package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private Integer turn;
    private Board board;
    private Color currentPlayer;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8,8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public Integer getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j); // fazendo downCasting de boardPiece para chessPiece
            }
        }

        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();

        validateSourcePosition(source);
        validateTargetPosition(source, target);

        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can´t put yourself in check!");
        }

        ChessPiece movedPiece = (ChessPiece)board.piece(target);

        // SpecialMove promotion
        promoted = null;
        if (movedPiece instanceof Pawn) {
            if (movedPiece.getColor() == Color.WHITE && target.getRow() == 0 || movedPiece.getColor() == Color.BLACK&& target.getRow() == 7) {
                promoted = (ChessPiece)board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }


        check = (testCheck(opponent(currentPlayer))) ? true : false;

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        // SpecialMove enPassant
        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() -2 || target.getRow() == source.getRow() +2)) {
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("There isn´t piece on source position");
        }

        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
            throw new ChessException("The chosen piece isn´t yours, choose your piece!");
        }

        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There isn´t possible moves for the chosen piece!");
        }
    }

    private void  validateTargetPosition(Position source, Position target) {
            if (!board.piece(source).possibleMove(target)) {
                throw new ChessException("The chosen piece can´t move to target position!");
            }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Piece makeMove(Position source, Position target) {
        ChessPiece piece = (ChessPiece) board.removePiece(source);
        piece.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(piece,target);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // SpecialMove castling kingSide rook
        if (piece instanceof King && target.getColumn() == source.getColumn() +2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() +3);
            Position targetRook = new Position(source.getRow(), source.getColumn() +1);

            ChessPiece rook = (ChessPiece)board.removePiece(sourceRook);
            board.placePiece(rook, targetRook);

            rook.increaseMoveCount();
        }

        // SpecialMove castling queenSide rook
        if (piece instanceof King && target.getColumn() == source.getColumn() -2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() -4);
            Position targetRook = new Position(source.getRow(), source.getColumn() -1);

            ChessPiece rook = (ChessPiece)board.removePiece(sourceRook);
            board.placePiece(rook, targetRook);

            rook.increaseMoveCount();
        }

        // SpecialMove enPassant
        if (piece instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;

                if (piece.getColor() == Color.WHITE) {
                    pawnPosition = new Position(target.getRow() +1, target.getColumn());
                } else {
                    pawnPosition = new Position(target.getRow() -1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    public ChessPiece replacePromotedPiece(String type) {
        if (promoted == null) {
            throw new IllegalStateException("There is no piece to be promoted!");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            throw new InvalidParameterException("Invalid type for promotion!");
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece piece = board.removePiece(pos);
        piecesOnTheBoard.remove(piece);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        if (type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece piece = (ChessPiece) board.removePiece(target);
        piece.decreaseMoveCount();
        board.placePiece(piece, source);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, source);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // SpecialMove castling kingSide rook
        if (piece instanceof King && target.getColumn() == source.getColumn() +2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() +3);
            Position targetRook = new Position(source.getRow(), source.getColumn() +1);

            ChessPiece rook = (ChessPiece)board.removePiece(targetRook);
            board.placePiece(rook, sourceRook);

            rook.decreaseMoveCount();
        }

        // SpecialMove castling queenSide rook
        if (piece instanceof King && target.getColumn() == source.getColumn() -2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() -4);
            Position targetRook = new Position(source.getRow(), source.getColumn() -1);

            ChessPiece rook = (ChessPiece)board.removePiece(targetRook);
            board.placePiece(rook, sourceRook);

            rook.decreaseMoveCount();
        }

        // SpecialMove enPassant
        if (piece instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece)board.removePiece(target);
                Position pawnPosition;

                if (piece.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, target.getColumn());
                } else {
                    pawnPosition = new Position(4, target.getColumn());
                }
                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There isn´t " + color + "king on the board!"); // Se essa exceção estourar, significa que o meu sistema de xadrez está com problema.
    }

    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());

        for (Piece p : opponentPieces) {
            boolean [][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(Color color) {
        if(!testCheck(color)) {
            return false;
        }

        List<Piece> list =  piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();

            for (int i=0; i< board.getRows(); i++) {
                for (int j=0; j< board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);

                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup() {
        placeNewPiece('A', 1, new Rook(board, Color.WHITE));
        placeNewPiece('B', 1, new Knight(board, Color.WHITE));
        placeNewPiece('C', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('D', 1, new Queen(board, Color.WHITE));
        placeNewPiece('E', 1, new King(board, Color.WHITE, this));
        placeNewPiece('F', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('G', 1, new Knight(board, Color.WHITE));
        placeNewPiece('H', 1, new Rook(board, Color.WHITE));
        placeNewPiece('A', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('B', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('C', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('D', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('E', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('F', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('G', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('H', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('A', 8, new Rook(board, Color.BLACK));
        placeNewPiece('B', 8, new Knight(board, Color.BLACK));
        placeNewPiece('C', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('D', 8, new Queen(board, Color.BLACK));
        placeNewPiece('E', 8, new King(board, Color.BLACK, this));
        placeNewPiece('F', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('G', 8, new Knight(board, Color.BLACK));
        placeNewPiece('H', 8, new Rook(board, Color.BLACK));
        placeNewPiece('A', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('B', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('C', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('D', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('E', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('F', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('G', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('H', 7, new Pawn(board, Color.BLACK, this));
    }
}
