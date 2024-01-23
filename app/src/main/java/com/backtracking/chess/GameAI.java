package com.backtracking.chess;

import com.backtracking.chess.Pieces.Bishop;
import com.backtracking.chess.Pieces.King;
import com.backtracking.chess.Pieces.Knight;
import com.backtracking.chess.Pieces.Pawn;
import com.backtracking.chess.Pieces.Piece;
import com.backtracking.chess.Pieces.Queen;
import com.backtracking.chess.Pieces.Rook;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GameAI {
    public static Move findBestMove(Game currentGame) {
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        int moveValue = Integer.MIN_VALUE;

        List<Move> allMoves = getAllPossibleMoves(currentGame);
        for (Move move : allMoves) {
            Game tmpGame = new Game(currentGame);
            makeMove(tmpGame, move, false);
            moveValue = alphaBeta(tmpGame, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private static int alphaBeta(Game game, int depth, int alpha, int beta, boolean maximizingPlayer) {
        for (Piece piece : game.pieces) {
            if (piece instanceof King) {
                if ((piece.color == Const.BLACK && maximizingPlayer) || (piece.color != Const.BLACK && !maximizingPlayer)) {
                    if (!mayCheckBeAvoided(game, piece)) return evaluateBoard(game, maximizingPlayer) - 900;
                }
            }
        }
        if (depth == 0 ) {
            return evaluateBoard(game, maximizingPlayer);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : getAllPossibleMoves(game)) {
                Game tmpGame = new Game(game);
                makeMove(tmpGame, move, false);
                int eval = alphaBeta(tmpGame,depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Beta cut-off
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : getAllPossibleMoves(game)) {
                Game tmpGame = new Game(game);
                makeMove(tmpGame, move, false);
                int eval = alphaBeta(tmpGame, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha cut-off
                }
            }
            return minEval;
        }
    }

    public static int evaluateBoard(Game game, boolean maximizingPlayer) {
        int score = 0;

        for (Piece piece : game.pieces) {
            if (piece == null) continue;
            if (maximizingPlayer && piece.color != Const.BLACK) continue;
            if (!maximizingPlayer && piece.color == Const.WHITE) continue;

            score += getPieceValue(piece);

        }

        return score;
    }

    private static int getPieceValue(Piece piece) {
        if (piece instanceof Pawn) return 10;
        else if (piece instanceof Knight) return 30;
        else if (piece instanceof Bishop) return 30;
        else if (piece instanceof Rook) return 30;
        else if (piece instanceof Queen) return 90;
        else if (piece instanceof King) return 900;

        return 0;
    }

    static void makeMove(Game game, Move bestMove, boolean isOfficialMove) {
        if (bestMove != null) {
            if (!bestMove.isAttack()) {
                if (bestMove.getPiece() instanceof King)
                    if (Math.abs(bestMove.getPiece().position.x - bestMove.getNewPosition().x) > 1) {
                        getCloserRook(game, bestMove.getNewPosition().x, bestMove.getPiece().color).moveTo(
                                new Position((bestMove.getPiece().position.x + bestMove.getNewPosition().x) / 2, bestMove.getNewPosition().y));
                    }
                bestMove.getPiece().moveTo(bestMove.getNewPosition());
                if (bestMove.getPiece() instanceof Pawn) { // check promotion possibility
                    if (bestMove.getPiece().position.y == 7 || bestMove.getPiece().position.y == 0) {
                        Piece promotionPiece = new Queen(bestMove.getPiece().color, bestMove.getPiece().position);
                        game.pieces.add(promotionPiece);
                        game.pieces.remove(bestMove.getPiece());
                    }
                }
                if (!isOfficialMove) changeTurn(game);
            }
            else {
                if (bestMove.getPiece() instanceof Pawn) { // check promotion possibility
                    if ((bestMove.getPiece().color == Const.WHITE && bestMove.getPiece().position.y == 6) ||
                            (bestMove.getPiece().color == Const.BLACK && bestMove.getPiece().position.y == 1)) {
                        Piece promotionPiece = new Queen(bestMove.getPiece().color, bestMove.getPiece().position);
                        game.pieces.add(promotionPiece);
                        game.pieces.remove(bestMove.getPiece());
                    }
                }

                if (bestMove.getPiece() instanceof Pawn) if (!pieceOnSquare(game, bestMove.getNewPosition())) {
                    if (bestMove.getPiece().color == Const.WHITE)
                        game.pieces.remove(getPieceOn(game, new Position(bestMove.getNewPosition().x, bestMove.getNewPosition().y - 1)));
                    else
                        game.pieces.remove(getPieceOn(game, new Position(bestMove.getNewPosition().x, bestMove.getNewPosition().y + 1)));
                }

                if (!(bestMove.getPiece() instanceof King) && "transformer".equals(game.mode)) {
                    if (!(bestMove.getPiece() instanceof Pawn) || (!(bestMove.getPiece().color == Const.WHITE && bestMove.getPiece().position.y == 6)
                            && !(bestMove.getPiece().color == Const.BLACK && bestMove.getPiece().position.y == 1))) { // check promotion possibility

                        if (pieceOnSquare(game, bestMove.getNewPosition())) {
                            Class<?> capturedPieceType = getPieceOn(game, bestMove.getNewPosition()).getClass();
                            Piece newPiece = createNewPiece(capturedPieceType, bestMove.getPiece().color, game.activePiece.position);
                            game.pieces.add(newPiece);
                            game.pieces.remove(bestMove.getPiece());
                        }
                    }
                }
                if (pieceOnSquare(game, bestMove.getNewPosition())) game.pieces.remove(getPieceOn(game, bestMove.getNewPosition()));

                bestMove.getPiece().moveTo(bestMove.getNewPosition());
                if (!isOfficialMove) changeTurn(game);
            }
        }
    }

    private static void changeTurn(Game game) {
        for (Piece i : game.pieces) if (i.enPassant) {
            if (game.enPassantInPast.contains(i)) {
                i.enPassant = false;
                game.enPassantInPast.remove(i);
            }
            else game.enPassantInPast.add(i);
        }

        Piece king;
        // change to BLACK
        if (game.activeColor == Const.WHITE){
            game.activeColor = Const.BLACK;
            king = game.blackKing;
        }
        // change to WHITE
        else {
            game.activeColor = Const.WHITE;
            king = game.whiteKing;
        }
    }


    public static List<Move> getAllPossibleMoves(Game game) {
        List<Move> allMoves = new ArrayList<>();
        List<Position> movePointers = new ArrayList<>();
        List<Position> attackPointers = new ArrayList<>();

        for (Piece piece : game.pieces) {
            if (piece.color != game.activeColor) continue;
            movePointers = getMovePointers(game, piece);
            attackPointers = getAttackPointers(game, piece);
            if (piece instanceof King) {
                movePointers = removeAttacked(game, movePointers, piece);
                attackPointers = removeAttacked(game,attackPointers, piece);
            }
            else {
                movePointers = makeKingSafe(game, piece, movePointers);
                attackPointers = makeKingSafe(game, piece, attackPointers);
            }


            for (Position pos : movePointers) {
                boolean isCastle = false;
                boolean isPromotion = false;

                if (piece instanceof King)
                    if (Math.abs(piece.position.x - pos.x) > 1) {
                        isCastle = true;
                    }
                if (piece instanceof Pawn) { // check promotion possibility
                    if (piece.position.y == 7 || piece.position.y == 0) {
                        isPromotion = true;
                    }
                }
                allMoves.add(new Move(piece, pos, isCastle, isPromotion, false));

            }
            for (Position i : attackPointers) {
                boolean isPromotion = false;

                if (piece instanceof Pawn) { // check promotion possibility
                    if ((piece.color == Const.WHITE && piece.position.y == 6) ||
                            (piece.color == Const.BLACK && piece.position.y == 1)) {
                        isPromotion = true;
                    }
                }

//                if (piece instanceof Pawn) if (!pieceOnSquare(i)) {
//                    if (piece.color == Const.WHITE)
//                        pieces.remove(getPieceOn(new Position(i.x, i.y - 1)));
//                    else
//                        pieces.remove(getPieceOn(new Position(i.x, i.y + 1)));
//                }

                if (!(piece instanceof King) && "transformer".equals(game.mode)) {
                    if (!(piece instanceof Pawn) || (!(piece.color == Const.WHITE && piece.position.y == 6)
                            && !(piece.color == Const.BLACK && piece.position.y == 1))) { // check promotion possibility

//                        if (pieceOnSquare(i)) {
//                            Class<?> capturedPieceType = getPieceOn(i).getClass();
//                            Piece newPiece = createNewPiece(capturedPieceType, piece.color, piece.position);
//                            pieces.add(newPiece);
//                            pieces.remove(piece);
//                        }
                    }
                }

                allMoves.add(new Move(piece, i, false, isPromotion, true));

            }

        }
        return allMoves;
    }


    private static boolean pieceOnSquare(Game game, Position square){
        for(Piece p : game.pieces) if (p.position != null) // may be null because of getPieceOn()

            if (Position.areEqual(p.position, square)) return true;
        return false;
    }

    private static Piece getPieceOn(Game game, Position p){
        for(Piece i : game.pieces) if(Position.areEqual(p, i.position)) return i;
        return new Pawn(null, (byte) 0); // protection for null pointer exception
    }

    private static List<Position> getMovePointers(Game game, Piece p){
        List<Position> tempMovePointers = p.moveXY();
        ListIterator<Position> tempIterator = tempMovePointers.listIterator();
        int tempX, tempY, sigX, sigY;
        Position tempMovePointer;
        while (tempIterator.hasNext()) {
            tempMovePointer = tempIterator.next();
            if(tempMovePointer.x > 7 || tempMovePointer.x < 0 || tempMovePointer.y > 7 || tempMovePointer.y < 0){
                tempIterator.remove();
                continue;
            }
            if (pieceOnSquare(game, tempMovePointer)){
                tempIterator.remove();
                tempX = tempMovePointer.x; tempY = tempMovePointer.y;
                sigX = (int) Math.signum(p.position.x - tempX);
                sigY = (int) Math.signum(p.position.y - tempY);
                while(tempIterator.hasNext()){
                    tempMovePointer = tempIterator.next();
                    tempX = tempMovePointer.x; tempY = tempMovePointer.y;
                    if(sigX == Math.signum(p.position.x - tempX)
                            && sigY == Math.signum((p.position.y - tempY)))
                        tempIterator.remove();
                    else{
                        tempIterator.previous();
                        break;
                    }
                }
            }
        }
        if (p instanceof King)
            for(Piece rook : game.pieces) if(rook instanceof Rook && rook.color == p.color)
                tempMovePointers.addAll(castling(game, p, rook));

        return tempMovePointers;
    }

    private static List<Position> getAttackPointers(Game game, Piece p){
        List<Position> tempAttackPointers = p.attackXY();
        ListIterator<Position> tempIterator = tempAttackPointers.listIterator();
        int tempX, tempY, sigX, sigY;

        Position tempAttackPointer;
        while (tempIterator.hasNext()) {
            tempAttackPointer = tempIterator.next();
            if (!pieceOnSquare(game, tempAttackPointer)) tempIterator.remove();
            else {
                if (getPieceOn(game, tempAttackPointer).color == p.color)
                    tempIterator.remove();
                tempX = tempAttackPointer.x;
                tempY = tempAttackPointer.y;
                sigX = (int) Math.signum(p.position.x - tempX);
                sigY = (int) Math.signum(p.position.y - tempY);
                while (tempIterator.hasNext()) {
                    tempAttackPointer = tempIterator.next();
                    tempX = tempAttackPointer.x;
                    tempY = tempAttackPointer.y;
                    if (sigX == Math.signum(p.position.x - tempX)
                            && sigY == Math.signum((p.position.y - tempY)))
                        tempIterator.remove();
                    else {
                        tempIterator.previous();
                        break;
                    }
                }
            }
        }

        if (p instanceof Pawn){
            Position enPosition = new Position(p.position.x+1, p.position.y);
            Piece enPiece;
            if(pieceOnSquare(game, enPosition)){
                enPiece = getPieceOn(game, enPosition);
                if (enPiece instanceof Pawn) if(enPiece.enPassant) {
                    if(p.color == Const.WHITE) enPosition.y++;
                    else enPosition.y--;
                    tempAttackPointers.add(enPosition);
                }
            }
            enPosition = new Position(p.position.x-1, p.position.y);
            if(pieceOnSquare(game, enPosition)){
                enPiece = getPieceOn(game, enPosition);
                if (enPiece instanceof Pawn) if(enPiece.enPassant) {
                    if(p.color == Const.WHITE) enPosition.y++;
                    else enPosition.y--;
                    tempAttackPointers.add(enPosition);
                }
            }
        }

        return tempAttackPointers;
    }

    private static List<Position> castling(Game game, Piece king, Piece rook){
        List<Position> castling = new ArrayList<>();
        if (king.firstMove) if (rook.firstMove) {
            int step = (int) Math.signum(rook.position.x - king.position.x);
            boolean possible = true;
            Position square;
            for(int x = king.position.x+step; x != rook.position.x; x += step){
                square = new Position(x, king.position.y);
                if(pieceOnSquare(game, square)){
                    possible = false;
                    break;
                } else if(x != rook.position.x+step) if(isSquareAttacked(game, square, king)){
                    possible = false;
                    break;
                }
            }
            if(possible) castling.add(new Position(king.position.x + 2*step, king.position.y));
        }
        return castling;
    }

    private static Piece getCloserRook(Game game, int x, int color){
        Piece rook = null;
        int i;
        for (i = 0; i < game.pieces.size(); i++)
            if (game.pieces.get(i).color == color) if (game.pieces.get(i) instanceof Rook){
                rook = game.pieces.get(i);
                break;
            }
        for (; i < game.pieces.size(); i++)
            if (game.pieces.get(i).color == color) if (game.pieces.get(i) instanceof Rook) {
                assert rook != null;
                if(Math.abs(x-rook.position.x) > Math.abs(x-game.pieces.get(i).position.x)){
                    rook = game.pieces.get(i);
                    break;
                }
            }

        return rook;
    }

    private static boolean mayCheckBeAvoided(Game game, Piece king){
        List<Piece> tempPieces = new ArrayList<>(game.pieces);
        for(Piece i : tempPieces) { // needs to be like this, see README.md -> code tricks
            if (i.color == king.color) {
                if (i instanceof King) {
                    if (!removeAttacked(game, getMovePointers(game, i), i).isEmpty()) return true;
                    if (!removeAttacked(game, getAttackPointers(game, i), i).isEmpty()) return true;
                } else {
                    if (!makeKingSafe(game, i, getMovePointers(game, i)).isEmpty()) return true;
                    if (!makeKingSafe(game, i, getAttackPointers(game, i)).isEmpty()) return true;
                }
            }
        }
        return false;
    }

    private static List<Position> makeKingSafe(Game game, Piece movingPiece, List<Position> squares){
        Piece king;
        if (movingPiece.color == Const.WHITE) king = game.whiteKing;
        else king = game.blackKing;
        return removeAttacked(game, squares, king, movingPiece);
    }

    private static boolean isSquareAttacked(Game game, Position square, Piece protectedPiece){
        Piece capturedPiece = null;
        if(pieceOnSquare(game, square)) if(getPieceOn(game, square) != protectedPiece){
            capturedPiece = getPieceOn(game, square);
            game.pieces.remove(capturedPiece);
        }
        Position protectedPiecePosition = protectedPiece.position;
        protectedPiece.position = square;
        for(Piece i : game.pieces) if(i.color != protectedPiece.color) {
            for (Position attackedSquare : getAttackPointers(game, i))
                if (Position.areEqual(square, attackedSquare)) {
                    if (capturedPiece != null) game.pieces.add(capturedPiece);
                    protectedPiece.position = protectedPiecePosition;
                    return true;
                }
        }
        protectedPiece.position = protectedPiecePosition;
        if (capturedPiece != null) game.pieces.add(capturedPiece);
        return false;
    }

    // deleting attacked squares from kings moves
    private static List<Position> removeAttacked(Game game, List<Position> squares, Piece protectedPiece){
        ListIterator<Position> squaresIterator = squares.listIterator();
        Position tempSquare;
        while(squaresIterator.hasNext()) {
            tempSquare = squaresIterator.next();
            if(isSquareAttacked(game, tempSquare, protectedPiece)) squaresIterator.remove();
        }
        return squares;
    }

    // overloaded for making King safe when other pieces move
    private static List<Position> removeAttacked(Game game, List<Position> squares, Piece protectedPiece/*king*/, Piece movingPiece){
        ListIterator<Position> squaresIterator = squares.listIterator();
        Position movingPiecePosition = movingPiece.position, tempSquare;
        Piece deletedPiece = null;
        while(squaresIterator.hasNext()) {
            tempSquare = squaresIterator.next();
            if(pieceOnSquare(game, tempSquare)){
                deletedPiece = getPieceOn(game, tempSquare);
                game.pieces.remove(deletedPiece);
            }
            movingPiece.position = tempSquare;
            if(isSquareAttacked(game, protectedPiece.position, protectedPiece)) squaresIterator.remove();
            if(deletedPiece != null){
                game.pieces.add(deletedPiece);
                deletedPiece = null;
            }
        }
        movingPiece.position = movingPiecePosition;
        return squares;
    }

    private static void capture(Game game, Piece capturedPiece){
        game.pieces.remove(capturedPiece);
        if (game.activePiece instanceof King || !"transformer".equals(game.mode)) return;

        if (game.activePiece instanceof Pawn) { // check promotion possibility
            if (game.activePiece.color == Const.WHITE
                    && game.activePiece.position.y == 6) return;
            else if (game.activePiece.color == Const.BLACK && game.activePiece.position.y == 1) return;
        }

        Class<?> capturedPieceType = capturedPiece.getClass();
        Piece newPiece = createNewPiece(capturedPieceType, game.activePiece.color, game.activePiece.position);
        game.pieces.add(newPiece);
        game.pieces.remove(game.activePiece);
        game.activePiece = newPiece;
    }

    private static Piece createNewPiece(Class<?> pieceType, byte color, Position position) {
        if (pieceType == Pawn.class) {
            return new Pawn(color, position);
        }
        else if (pieceType == Rook.class) {
            return new Rook(color, position);
        } else if (pieceType == Knight.class) {
            return new Knight(color, position);
        } else if (pieceType == Bishop.class) {
            return new Bishop(color, position);
        } else if (pieceType == Queen.class) {
            return new Queen(color, position);
        } else {
            throw new IllegalArgumentException("Unknown piece type: " + pieceType);
        }
    }

}
