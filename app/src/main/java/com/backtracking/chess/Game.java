package com.backtracking.chess;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.backtracking.chess.Pieces.Bishop;
import com.backtracking.chess.Pieces.King;
import com.backtracking.chess.Pieces.Knight;
import com.backtracking.chess.Pieces.Pawn;
import com.backtracking.chess.Pieces.Piece;
import com.backtracking.chess.Pieces.Queen;
import com.backtracking.chess.Pieces.Rook;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


class Game implements Serializable {
    String mode;
    String category;
    byte state;
    private byte previousState;
    List<Position> movePointers;
    List<Position> attackPointers;
    Piece activePiece;
    byte activeColor;
    List<Piece> pieces;
    List<Piece> capturedPieces; // added for future extensions
    Piece whiteKing, blackKing;
    List<Piece> enPassantInPast;

    final Context context;
    final GameActivity gameActivity;

    Game(Context c, GameActivity gA){
        context = c;
        gameActivity = gA;
    }

    // Deep copy constructor
    public Game(Game other) {
        this.mode = other.mode;
        this.category = other.category;
        this.state = other.state;
        this.previousState = other.previousState;

        // Deep copy of List<Position> movePointers and attackPointers
        this.movePointers = new ArrayList<>();
        for (Position pos : other.movePointers) {
            this.movePointers.add(new Position(pos)); // Assuming Position has a copy constructor
        }
        this.attackPointers = new ArrayList<>();
        for (Position pos : other.attackPointers) {
            this.attackPointers.add(new Position(pos)); // Assuming Position has a copy constructor
        }

        // Deep copy of activePiece
        this.activePiece = other.activePiece != null ? other.activePiece.clonePiece() : null; // Assuming Piece has a method clonePiece()

        this.activeColor = other.activeColor;

        // Deep copy of List<Piece> pieces
        this.pieces = new ArrayList<>();
        for (Piece piece : other.pieces) {
            this.pieces.add(piece.clonePiece()); // Assuming Piece has a method clonePiece()
        }

        // Deep copy of capturedPieces
        this.capturedPieces = new ArrayList<>();
        for (Piece piece : other.capturedPieces) {
            this.capturedPieces.add(piece.clonePiece()); // Assuming Piece has a method clonePiece()
        }

        // Deep copy of whiteKing and blackKing
        this.whiteKing = other.whiteKing != null ? other.whiteKing.clonePiece() : null; // Assuming Piece has a method clonePiece()
        this.blackKing = other.blackKing != null ? other.blackKing.clonePiece() : null; // Assuming Piece has a method clonePiece()

        // Deep copy of enPassantInPast
        this.enPassantInPast = new ArrayList<>();
        for (Piece piece : other.enPassantInPast) {
            this.enPassantInPast.add(piece.clonePiece()); // Assuming Piece has a method clonePiece()
        }

        // Context and GameActivity are typically not cloned as they are tied to the current state of the application
        this.context = other.context;
        this.gameActivity = other.gameActivity;
    }

    void start(String m_mode, String m_category) {
        this.mode = m_mode;
        this.category = m_category;
        state = Const.STATE_SELECT;
        previousState = state;
        pieces = new ArrayList<>();
        capturedPieces = new ArrayList<>();
        movePointers = new ArrayList<>();
        attackPointers = new ArrayList<>();
        enPassantInPast = new ArrayList<>();

        activeColor = Const.WHITE;

        for (byte color = Const.WHITE; color <= Const.BLACK; color++){
            for(int i = 0; i < 8; i++) pieces.add(new Pawn(context, color));
            pieces.add(new Bishop(context, color));
            pieces.add(new Bishop(context, color));
            pieces.add(new Knight(context, color));
            pieces.add(new Knight(context, color));
            pieces.add(new Rook(context, color));
            pieces.add(new Rook(context, color));
            pieces.add(new Queen(context, color));
            pieces.add(new King(context, color));
            if(color == Const.WHITE) whiteKing = pieces.get(pieces.size()-1);
            else blackKing = pieces.get(pieces.size()-1);
        }

        gameActivity.changeTurn(activeColor);

    }

    void end(byte w){
        state = Const.STATE_END;
        gameActivity.endOfTheGame(w);
    }

    private void changeTurn() {
        gameActivity.redrawBoard();

        for(Piece i : pieces) if(i.enPassant) {
            if(enPassantInPast.contains(i)) {
                i.enPassant = false;
                enPassantInPast.remove(i);
            }
            else enPassantInPast.add(i);
        }

        Piece king;
        // change to BLACK
        if (activeColor == Const.WHITE) {
            activeColor = Const.BLACK;
            king = blackKing;
        }
        // change to WHITE
        else {
            activeColor = Const.WHITE;
            king = whiteKing;
        }

        gameActivity.redrawBoard();
        gameActivity.changeTurn(activeColor);

        if(!mayCheckBeAvoided(king)) {
            if (activeColor == Const.WHITE) end(Const.BLACK);
            else end(Const.WHITE);
        }
        else if(isSquareAttacked(king.position, king)) {
            gameActivity.vibrate(200);
            GameManagement.makeToast(R.string.toast_check, GameManagement.switchColor(activeColor), gameActivity);
        }

        if (!"AI".equals(category)) return;
        if (activeColor == Const.WHITE) return;
        Game currentGame = new Game(this);
        Move bestMove = GameAI.findBestMove(currentGame);
//        getPieceOn(bestMove.getPiece().position).moveTo(bestMove.getNewPosition());
        GameAI.makeMove(this, bestMove, true);
        changeTurn();
        gameActivity.redrawBoard();
    }

    void processMoveCompetitor(Position oldP, Position newP) {
        for (Piece chosenPiece : pieces)
            if (Position.areEqual(chosenPiece.position, oldP)){
                chosenPiece.moveTo(newP);
            }
        changeTurn();
        gameActivity.redrawBoard();

        JSONObject socketMessage = gameActivity.socket.getMessage();
        try {
            socketMessage.put("yourTurn", true);
            gameActivity.socket.setMessage(socketMessage);
        }catch (Exception e) {
            System.out.println("Error in game.java " + e);
        }

    }

    void processTouch(MotionEvent event, Position touchPosition, Boolean yourTurn){
        System.out.println("yourTurn " + yourTurn);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (state) {
                    case Const.STATE_SELECT:
                        if(yourTurn != null && !yourTurn) break;
                        for (Piece i : pieces) {
                            if (category == "AI" && activeColor == Const.BLACK) break;
                            if (i.color == activeColor) {
                                if (Position.areEqual(i.position, touchPosition)) {
                                    activePiece = i;
                                    movePointers = getMovePointers(activePiece);
                                    attackPointers = getAttackPointers(activePiece);
                                    if(activePiece instanceof King){
                                        movePointers = removeAttacked(movePointers, activePiece);
                                        attackPointers = removeAttacked(attackPointers, activePiece);
                                    }
                                    else{
                                        movePointers = makeKingSafe(activePiece, movePointers);
                                        attackPointers = makeKingSafe(activePiece, attackPointers);
                                    }
                                    state = Const.STATE_MOVE_ATTACK;
                                    break;
                                }
                            }
                        }
                        break;

                    case Const.STATE_MOVE_ATTACK:
                        boolean isPromotion = false;
                        if(yourTurn != null && !yourTurn) break;
                        state = Const.STATE_SELECT; // here because of possible change to STATE_END
                        if (Position.areEqual(touchPosition, activePiece.position)) break;
                        if(yourTurn != null) {

                            // check touch event in movePointer or attackPointer

                            boolean isValid = false;

                            for (Position i : movePointers)
                                if (Position.areEqual(i, touchPosition)) {
                                    isValid = true;
                                    break;
                                }
                            for (Position i : attackPointers)
                                if (Position.areEqual(i, touchPosition)) {
                                    isValid = true;
                                    break;
                                }

                            if(isValid) {
                                try{
                                    JSONObject socketMessage = gameActivity.socket.getMessage();
                                    socketMessage.put("fromX", activePiece.position.x);
                                    socketMessage.put("fromY", activePiece.position.y);
                                    socketMessage.put("toX", touchPosition.x);
                                    socketMessage.put("toY", touchPosition.y);
                                    gameActivity.socket.sendMessage("move",socketMessage);
                                    socketMessage.put("yourTurn", false);
                                    gameActivity.socket.setMessage(socketMessage);
                                }catch(Exception e) {
                                    System.out.println("Error in game.java " + e);
                                }
                            }

                        }
                        for (Position i : movePointers)
                            if (Position.areEqual(i, touchPosition)) {
                                if (activePiece instanceof King)
                                    if(Math.abs(activePiece.position.x - touchPosition.x) > 1){
                                        getCloserRook(touchPosition.x, activePiece.color).moveTo(
                                                new Position((activePiece.position.x + touchPosition.x)/2, touchPosition.y));
                                }
                                activePiece.moveTo(touchPosition);
                                if (activePiece instanceof Pawn){ // check promotion possibility
                                    if (activePiece.color == Const.WHITE && activePiece.position.y == 7) {
                                        isPromotion = true;
                                        promotion(activePiece);
                                    }
                                    else if (activePiece.position.y == 0) {
                                        isPromotion = true;
                                        promotion(activePiece);
                                    }
                                }
                                if (!isPromotion) changeTurn();
                                break;
                            }
                        for (Position i : attackPointers)
                            if (Position.areEqual(i, touchPosition)){
                                if(activePiece instanceof Pawn){ // check promotion possibility
                                    if(activePiece.color == Const.WHITE
                                            && activePiece.position.y == 6) promotion(activePiece);
                                    else if(activePiece.color == Const.BLACK && activePiece.position.y == 1) {
                                        isPromotion = true;
                                        promotion(activePiece);
                                    }
                                }

                                if(activePiece instanceof Pawn) if(!pieceOnSquare(i)){
                                    if(activePiece.color == Const.WHITE)
                                        capture(getPieceOn(new Position(touchPosition.x, touchPosition.y - 1)));
                                    else
                                        capture(getPieceOn(new Position(touchPosition.x, touchPosition.y + 1)));
                                }
                                if(pieceOnSquare(touchPosition)) capture(getPieceOn(touchPosition));

                                activePiece.moveTo(touchPosition);
                                if (!isPromotion) changeTurn();
                                break;
                            }


                        movePointers = new ArrayList<>();
                        attackPointers = new ArrayList<>();
                        break;

                    case Const.STATE_END:
                    case Const.STATE_PAUSE:
                        break;
                }
        }
    }

    boolean pieceOnSquare(Position square){
        for(Piece p : pieces) if(p.position != null) // may be null because of getPieceOn()

            if (Position.areEqual(p.position, square)) return true;
        return false;
    }

    public Piece getPieceOn(Position p){
        for(Piece i : pieces) if(Position.areEqual(p, i.position)) return i;
        return new Pawn(context, (byte) 0); // protection for null pointer exception
    }

    private List<Position> getMovePointers(Piece p){
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
            if(pieceOnSquare(tempMovePointer)){
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
        if(p instanceof King)
            for(Piece rook : pieces) if(rook instanceof Rook && rook.color == p.color)
                tempMovePointers.addAll(castling(p, rook));

        return tempMovePointers;
    }

    private List<Position> getAttackPointers(Piece p){
        List<Position> tempAttackPointers = p.attackXY();
        ListIterator<Position> tempIterator = tempAttackPointers.listIterator();
        int tempX, tempY, sigX, sigY;

        Position tempAttackPointer;
        while (tempIterator.hasNext()) {
            tempAttackPointer = tempIterator.next();
            if (!pieceOnSquare(tempAttackPointer)) tempIterator.remove();
            else {
                if (getPieceOn(tempAttackPointer).color == p.color)
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

        if(p instanceof Pawn){
            Position enPosition = new Position(p.position.x+1, p.position.y);
            Piece enPiece;
            if(pieceOnSquare(enPosition)){
                enPiece = getPieceOn(enPosition);
                if (enPiece instanceof Pawn) if(enPiece.enPassant) {
                    if(p.color == Const.WHITE) enPosition.y++;
                    else enPosition.y--;
                    tempAttackPointers.add(enPosition);
                }
            }
            enPosition = new Position(p.position.x-1, p.position.y);
            if(pieceOnSquare(enPosition)){
                enPiece = getPieceOn(enPosition);
                if (enPiece instanceof Pawn) if(enPiece.enPassant) {
                    if(p.color == Const.WHITE) enPosition.y++;
                    else enPosition.y--;
                    tempAttackPointers.add(enPosition);
                }
            }
        }

        return tempAttackPointers;
    }

    private List<Position> castling(Piece king, Piece rook){
        List<Position> castling = new ArrayList<>();
        if(king.firstMove) if(rook.firstMove) {
            int step = (int) Math.signum(rook.position.x - king.position.x);
            boolean possible = true;
            Position square;
            for(int x = king.position.x+step; x != rook.position.x; x += step){
                square = new Position(x, king.position.y);
                if(pieceOnSquare(square)){
                    possible = false;
                    break;
                } else if(x != rook.position.x+step) if(isSquareAttacked(square, king)){
                    possible = false;
                    break;
                }
            }
            if(possible) castling.add(new Position(king.position.x + 2*step, king.position.y));
        }
        return castling;
    }

    public Piece getCloserRook(int x, int color){
        Piece rook = null;
        int i;
        for(i = 0; i < pieces.size(); i++)
            if(pieces.get(i).color == color) if(pieces.get(i) instanceof Rook){
            rook = pieces.get(i);
            break;
        }
        for(; i < pieces.size(); i++)
            if(pieces.get(i).color == color) if(pieces.get(i) instanceof Rook) {
                assert rook != null;
                if(Math.abs(x-rook.position.x) > Math.abs(x-pieces.get(i).position.x)){
                    rook = pieces.get(i);
                    break;
                }
            }

        return rook;
    }

    private boolean mayCheckBeAvoided(Piece king){
        List<Piece> tempPieces = new ArrayList<>(pieces);
        for(Piece i : tempPieces) { // needs to be like this, see README.md -> code tricks
            if (i.color == king.color) {
                if (i instanceof King) {
                    if (!removeAttacked(getMovePointers(i), i).isEmpty()) return true;
                    if (!removeAttacked(getAttackPointers(i), i).isEmpty()) return true;
                } else {
                    if (!makeKingSafe(i, getMovePointers(i)).isEmpty()) return true;
                    if (!makeKingSafe(i, getAttackPointers(i)).isEmpty()) return true;
                }
            }
        }
        return false;
    }

    private List<Position> makeKingSafe(Piece movingPiece, List<Position> squares){
        Piece king;
        if(movingPiece.color == Const.WHITE) king = whiteKing;
        else king = blackKing;
        return removeAttacked(squares, king, movingPiece);
    }

    private boolean isSquareAttacked(Position square, Piece protectedPiece){
        Piece capturedPiece = null;
        if(pieceOnSquare(square)) if(getPieceOn(square) != protectedPiece){
            capturedPiece = getPieceOn(square);
            pieces.remove(capturedPiece);
        }
        Position protectedPiecePosition = protectedPiece.position;
        protectedPiece.position = square;
        for(Piece i : pieces) if(i.color != protectedPiece.color) {
            for (Position attackedSquare : getAttackPointers(i))
                if (Position.areEqual(square, attackedSquare)) {
                    if (capturedPiece != null) pieces.add(capturedPiece);
                    protectedPiece.position = protectedPiecePosition;
                    return true;
                }
        }
        protectedPiece.position = protectedPiecePosition;
        if (capturedPiece != null) pieces.add(capturedPiece);
        return false;
    }

    // deleting attacked squares from kings moves
    private List<Position> removeAttacked(List<Position> squares, Piece protectedPiece){
        ListIterator<Position> squaresIterator = squares.listIterator();
        Position tempSquare;
        while(squaresIterator.hasNext()) {
            tempSquare = squaresIterator.next();
            if(isSquareAttacked(tempSquare, protectedPiece)) squaresIterator.remove();
        }
        return squares;
    }

    // overloaded for making King safe when other pieces move
    private List<Position> removeAttacked(List<Position> squares, Piece protectedPiece/*king*/, Piece movingPiece){
        ListIterator<Position> squaresIterator = squares.listIterator();
        Position movingPiecePosition = movingPiece.position, tempSquare;
        Piece deletedPiece = null;
        while(squaresIterator.hasNext()) {
            tempSquare = squaresIterator.next();
            if(pieceOnSquare(tempSquare)){
                deletedPiece = getPieceOn(tempSquare);
                pieces.remove(deletedPiece);
            }
            movingPiece.position = tempSquare;
            if(isSquareAttacked(protectedPiece.position, protectedPiece)) squaresIterator.remove();
            if(deletedPiece != null){
                pieces.add(deletedPiece);
                deletedPiece = null;
            }
        }
        movingPiece.position = movingPiecePosition;
        return squares;
    }

    private void capture(Piece capturedPiece){
        gameActivity.captureAnimation(activePiece, capturedPiece, "transformer".equals(mode));

        capturedPieces.add(capturedPiece);
        pieces.remove(capturedPiece);
        gameActivity.updatePads(capturedPiece);
        gameActivity.redrawBoard();
        if (activePiece instanceof King || !"transformer".equals(mode)) return;

        if(activePiece instanceof Pawn){ // check promotion possibility
            if(activePiece.color == Const.WHITE
                    && activePiece.position.y == 6) return;
            else if(activePiece.color == Const.BLACK && activePiece.position.y == 1) return;
        }

        Class<?> capturedPieceType = capturedPiece.getClass();
        Piece newPiece = createNewPiece(capturedPieceType, activePiece.color, activePiece.position);
        pieces.add(newPiece);
        pieces.remove(activePiece);
        activePiece = newPiece;
        gameActivity.redrawBoard();
    }

    private Piece createNewPiece(Class<?> pieceType, byte color, Position position) {
        if (pieceType == Pawn.class) {
            return new Pawn(context, color, position);
        }
        else if (pieceType == Rook.class) {
            return new Rook(context, color, position);
        } else if (pieceType == Knight.class) {
            return new Knight(context, color, position);
        } else if (pieceType == Bishop.class) {
            return new Bishop(context, color, position);
        } else if (pieceType == Queen.class) {
            return new Queen(context, color, position);
        } else {
            throw new IllegalArgumentException("Unknown piece type: " + pieceType);
        }
    }


    private void promotion(Piece promotedPawn){
        pause();
        gameActivity.openPromotionFragment(promotedPawn.color);
    }

    void promotionAddPiece(int type){
        switch (type){
            case Const.KNIGHT:
                pieces.add(new Knight(context, activePiece.color, activePiece.position));
                break;

            case Const.BISHOP:
                pieces.add(new Bishop(context, activePiece.color, activePiece.position));
                break;

            case Const.ROOK:
                pieces.add(new Rook(context, activePiece.color, activePiece.position));
                break;

            case Const.QUEEN:
                pieces.add(new Queen(context, activePiece.color, activePiece.position));
                break;
            default:
                Log.v(Const.DEBUG_TAG, "game, openPromotionFragment - error int");
        }

        pieces.remove(activePiece);
        gameActivity.redrawBoard();

        if ("AI".equals(category)) changeTurn();
    }

    void pause(){
        previousState = state;
        state = Const.STATE_PAUSE;
        gameActivity.pauseGame();
    }

    void unpause(){
        if(state == Const.STATE_PAUSE) state = previousState;
        gameActivity.unpauseGame();
    }
}
