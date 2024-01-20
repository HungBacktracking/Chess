package com.backtracking.chess;

import com.backtracking.chess.Pieces.Piece;

public class Move {
    private Piece piece;
    private Position oldPosition;
    private Position newPosition;
    private boolean isCastle;
    private boolean isPromotion;
    private boolean isAttack;

    public Move(Piece piece, Position newPos, boolean isCastle, boolean isPromotion, boolean isAttack) {
        this.piece = piece;
        this.newPosition = newPos;
        this.isCastle = isCastle;
        this.isPromotion = isPromotion;
        this.isAttack = isAttack;
    }

    // Getter for the piece
    public Piece getPiece() {
        return piece;
    }

    // Getter for the old position
    public Position getOldPosition() {
        return oldPosition;
    }

    // Getter for the new position
    public Position getNewPosition() {
        return newPosition;
    }

    // Getter for the isCastle flag
    public boolean isCastle() {
        return isCastle;
    }

    // Getter for the isPromotion flag
    public boolean isPromotion() {
        return isPromotion;
    }

    // Getter for the isAttack flag
    public boolean isAttack() {
        return isAttack;
    }
}
