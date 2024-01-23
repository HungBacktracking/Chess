package com.backtracking.chess.Pieces;


import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.backtracking.chess.Const;
import com.backtracking.chess.Position;
import com.backtracking.chess.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class King extends Piece implements Serializable {

    @Override
    public Piece clonePiece() {
        King copy = new King(this.color, new Position(this.position));
        copy.firstMove = this.firstMove;
        copy.enPassant = this.enPassant;
        copy.type = this.type;
        return copy;
    }

    public King(Context c, byte _color) {
        super(c, _color);
        type = Const.KING;
        strokeColor = ContextCompat.getColor(context, R.color.colorKing);
        if (color == Const.WHITE) image = context.getResources().getDrawable(R.drawable.white_king);
        else image = context.getResources().getDrawable(R.drawable.black_king);
    }

    public King(byte _color, Position initPos) {
        super(_color, initPos);
        type = Const.KING;
    }

    @Override
    public Position initialPosition() {
        if(color == Const.WHITE) return new Position(4,0);
        return new Position(4, 7);
    }

    @Override
    public List<Position> moveXY() {
        List<Position> moves = new ArrayList<>();
        for (int x = position.x-1; x <= position.x+1; x++)
            for(int y = position.y-1; y <= position.y+1; y++)
                if(!(x == position.x && y == position.y)) moves.add(new Position(x, y)); // NAND
        return moves;
    }

    @Override
    public List<Position> attackXY() {
        List<Position> attacks = new ArrayList<>();
        for (int x = position.x-1; x <= position.x+1; x++)
            for(int y = position.y-1; y <= position.y+1; y++)
                if(!(x == position.x && y == position.y)) attacks.add(new Position(x, y)); // NAND
        return attacks;
    }

}
