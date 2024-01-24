package com.backtracking.chess.Pieces;

import android.content.Context;
import android.support.v4.content.ContextCompat;


import com.backtracking.chess.Const;
import com.backtracking.chess.Position;
import com.backtracking.chess.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Rock extends Piece implements Serializable {

    @Override
    public Piece clonePiece() {
        Rook copy = new Rook(this.color, new Position(this.position));
        copy.firstMove = this.firstMove;
        copy.enPassant = this.enPassant;
        copy.type = this.type;
        return copy;
    }

    public Rock(Context c, byte _color) {
        super(c, _color);
        type = Const.ROCK;
        strokeColor = ContextCompat.getColor(context, R.color.colorRook);
        image = context.getResources().getDrawable(R.drawable.rocks);
    }

    public Rock(byte _color, Position initPos) {
        super(_color, initPos);
        type = Const.ROCK;
    }

    public Rock(Context c, byte _color, Position initPos) {
        super(c, _color, initPos);
        type = Const.ROCK;
        strokeColor = ContextCompat.getColor(context, R.color.colorRook);
        image = context.getResources().getDrawable(R.drawable.rocks);
    }

    @Override
    public Position initialPosition() {
        Random random = new Random();

        int x = random.nextInt(8);
        int y = 2 + random.nextInt(4);
        return new Position(x, y);
    }

    @Override
    public List<Position> moveXY() {
        List<Position> moves = new ArrayList<>();
        return moves;
    }

    @Override
    public List<Position> attackXY() {
        List<Position> attacks = new ArrayList<>();
        return attacks;
    }

}
