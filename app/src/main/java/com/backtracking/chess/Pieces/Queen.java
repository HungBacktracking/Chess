package com.backtracking.chess.Pieces;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.backtracking.chess.Const;
import com.backtracking.chess.Position;
import com.backtracking.chess.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece implements Serializable {

    @Override
    public Piece clonePiece() {
        Queen copy = new Queen(this.color, new Position(this.position));
        copy.firstMove = this.firstMove;
        copy.enPassant = this.enPassant;
        copy.type = this.type;
        return copy;
    }

    public Queen(Context c, byte _color) {
        super(c, _color);
        type = Const.QUEEN;
        strokeColor = ContextCompat.getColor(context, R.color.colorQueen);
        if (color == Const.WHITE) image = context.getResources().getDrawable(R.drawable.white_queen);
        else image = context.getResources().getDrawable(R.drawable.black_queen);
    }

    public Queen(byte _color, Position initPos) {
        super(_color, initPos);
        type = Const.QUEEN;
    }

    public Queen(Context c, byte _color, Position initPos) {
        super(c, _color, initPos);
        type = Const.QUEEN;
        strokeColor = ContextCompat.getColor(context, R.color.colorQueen);
        if (color == Const.WHITE) image = context.getResources().getDrawable(R.drawable.white_queen);
        else image = context.getResources().getDrawable(R.drawable.black_queen);
    }

    @Override
    public Position initialPosition() {
        if(color == Const.WHITE) return new Position(3,0);
        return new Position(3, 7);
    }

    @Override
    public List<Position> moveXY() {
        List<Position> moves = new ArrayList<>();
        for (int x = position.x+1; x < 8; x++) moves.add(new Position(x, position.y));
        for (int x = position.x-1; x >= 0; x--) moves.add(new Position(x, position.y));
        for (int y = position.y+1; y < 8; y++) moves.add(new Position(position.x, y));
        for (int y = position.y-1; y >= 0; y--) moves.add(new Position(position.x, y));
        for (int x = position.x+1, y = position.y+1; x < 8 && y < 8; x++, y++ ) moves.add(new Position(x, y));
        for (int x = position.x-1, y = position.y+1; x >= 0 && y < 8; x--, y++ ) moves.add(new Position(x, y));
        for (int x = position.x+1, y = position.y-1; x < 8 && y >= 0; x++, y-- ) moves.add(new Position(x, y));
        for (int x = position.x-1, y = position.y-1; x >= 0 && y >= 0; x--, y-- ) moves.add(new Position(x, y));

        return moves;
    }

    @Override
    public List<Position> attackXY() {
        List<Position> attacks = new ArrayList<>();
        for (int x = position.x+1; x < 8; x++) attacks.add(new Position(x, position.y));
        for (int x = position.x-1; x >= 0; x--) attacks.add(new Position(x, position.y));
        for (int y = position.y+1; y < 8; y++) attacks.add(new Position(position.x, y));
        for (int y = position.y-1; y >= 0; y--) attacks.add(new Position(position.x, y));
        for (int x = position.x+1, y = position.y+1; x < 8 && y < 8; x++, y++ ) attacks.add(new Position(x, y));
        for (int x = position.x-1, y = position.y+1; x >= 0 && y < 8; x--, y++ ) attacks.add(new Position(x, y));
        for (int x = position.x+1, y = position.y-1; x < 8 && y >= 0; x++, y-- ) attacks.add(new Position(x, y));
        for (int x = position.x-1, y = position.y-1; x >= 0 && y >= 0; x--, y-- ) attacks.add(new Position(x, y));

        return attacks;
    }
}
