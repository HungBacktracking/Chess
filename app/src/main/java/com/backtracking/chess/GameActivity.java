package com.backtracking.chess;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backtracking.chess.Pieces.Bishop;
import com.backtracking.chess.Pieces.King;
import com.backtracking.chess.Pieces.Knight;
import com.backtracking.chess.Pieces.Pawn;
import com.backtracking.chess.Pieces.Piece;
import com.backtracking.chess.Pieces.Queen;
import com.backtracking.chess.Pieces.Rook;
import com.backtracking.chess.Views.Board;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Date;

import io.socket.emitter.Emitter;

public class GameActivity extends AppCompatActivity implements GameManagement, Serializable {
    String mode;
    String category;
    SocketImpl socket;
    String typePlayer;
    private Game game;

    private byte drawState;
    private Board board;
    private FrameLayout fragmentFrame;
    private SparseArray<PlayerPadFragment> pads;
    private GameEndFragment gameEndFragment;
    private Button menuButton;
    ImageView animation;
    private ImageView brandingImage;

    private byte displayMode;
    private boolean vibrations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting up the activity

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        animation = findViewById(R.id.animation);
        menuButton = findViewById(R.id.game_menu_button);
        brandingImage = findViewById(R.id.branding_image);
        brandingImage.setVisibility(View.INVISIBLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // preparing spot for fragments
        fragmentFrame = findViewById(R.id.game_fragment_frame);
        fragmentFrame.bringToFront();
        fragmentFrame.setVisibility(View.GONE);

        // getting settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean timerOn = preferences.getBoolean(getResources().getString(R.string.timer_on_key), false);
        vibrations = preferences.getBoolean(getResources().getString(R.string.vibrations_on_key), false);

        mode = getIntent().getStringExtra("mode");
        category = getIntent().getStringExtra("category");

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if ("online".equals(category)) {
            socket = SocketImpl.getInstance();

            try {
                JSONObject content = new JSONObject();
                content.put("yourTurn",  false);
                socket.setMessage(content);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!socket.isConnected()) {
                socket.connect();
            }

            socket.findMatch();

            socket.on("found_match", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject content = (JSONObject) args[0];
                    System.out.println("found match: " + content);
                    socket.setMessage(content);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            TextView findingTextView = findViewById(R.id.finding);
                            findingTextView.setVisibility(View.GONE);
                            backButton.setVisibility(View.GONE);

                            // setting up timers
                            int beginningTime = 0;
                            int addingTime = 0;

                            if (timerOn) {

                                menuButton.setVisibility(View.GONE);

                                SetTimerFragment setTimerFragment = new SetTimerFragment();
                                getSupportFragmentManager().beginTransaction().add(R.id.game_fragment_frame, setTimerFragment).commit();
                                fragmentFrame.setVisibility(View.VISIBLE);
                                fragmentFrame.bringToFront();
                            }
                            else initGame(beginningTime, addingTime);

                        }
                    });

                }
            });

            socket.on("move", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Object content = args[0];
                    if (content instanceof JSONObject) {
                        JSONObject data = (JSONObject) content;
                        try {
                            game.processMoveCompetitor(
                                    new Position(data.getInt("fromX"), data.getInt("fromY")),
                                    new Position(data.getInt("toX"), data.getInt("toY"))
                            );
                            redrawBoard();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


        }


        if ("hidden".equals(mode)) displayMode = Const.MODERN_MODE;
        else displayMode = Const.CLASSIC_MODE;

        if ("online".equals(category)) return;

        TextView findingTextView = findViewById(R.id.finding);
        findingTextView.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);

        // setting up timers
        int beginningTime = 0;
        int addingTime = 0;

        if (timerOn) {
            menuButton.setVisibility(View.GONE);

            SetTimerFragment setTimerFragment = new SetTimerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.game_fragment_frame, setTimerFragment).commit();
            fragmentFrame.setVisibility(View.VISIBLE);
            fragmentFrame.bringToFront();
        }
        else initGame(beginningTime, addingTime);

    }

    void changeTurn(int activeColor){
        pads.get(Const.WHITE).changeTurn(activeColor);
        pads.get(Const.BLACK).changeTurn(activeColor);
    }

    public void endOfTheGame(final byte winner){
        long[] pattern = {50, 200, 300, 200, 300, 800};
        vibratePattern(pattern,-1);
        pads.get(Const.WHITE).end(winner);
        pads.get(Const.BLACK).end(winner);
        gameEndFragment = new GameEndFragment();
        Bundle winnerCode = new Bundle();
        winnerCode.putByte("winner", winner);
        gameEndFragment.setArguments(winnerCode);
        getSupportFragmentManager().beginTransaction().add(R.id.game_fragment_frame, gameEndFragment).commit();
        fragmentFrame.bringToFront();
        fragmentFrame.setVisibility(View.VISIBLE);
    }

    void redrawBoard(){
        board.redraw(game.pieces, game.movePointers, game.attackPointers, displayMode);
    }


    void captureAnimation(Piece activePiece,  Piece capturedPiece, boolean isTransform) {
        String resourceName = "";
        if (!isTransform) resourceName += "classic_";
        if (activePiece.color == Const.WHITE) resourceName += "white_";
        else resourceName += "black_";

        if (activePiece instanceof King) resourceName += "king_";
        else if (activePiece instanceof Queen) resourceName += "queen_";
        else if (activePiece instanceof Bishop) resourceName += "bishop_";
        else if (activePiece instanceof Knight) resourceName += "knight_";
        else if (activePiece instanceof Rook) resourceName += "rook_";
        else if (activePiece instanceof Pawn) resourceName += "pawn_";

        if (capturedPiece instanceof King) resourceName += "king";
        else if (capturedPiece instanceof Queen) resourceName += "queen";
        else if (capturedPiece instanceof Bishop) resourceName += "bishop";
        else if (capturedPiece instanceof Knight) resourceName += "knight";
        else if (capturedPiece instanceof Rook) resourceName += "rook";
        else if (capturedPiece instanceof Pawn) resourceName += "pawn";

        int resourceId = this.getResources().getIdentifier(resourceName, "raw", this.getPackageName());
        animation.setVisibility(View.VISIBLE);
        System.out.println(resourceName + resourceId);
        Glide.with(this).load(resourceId).asGif().into(new CustomGifDrawable(animation));
    }

    public void updatePads(Piece capturedPiece){
        switch (capturedPiece.color){
            case Const.WHITE:
                pads.get(Const.BLACK).capturedPad.addPiece(capturedPiece);
                break;
            case Const.BLACK:
                pads.get(Const.WHITE).capturedPad.addPiece(capturedPiece);
                break;
        }
    }

    private void openGameMenu(){
        game.pause();
        GameMenuFragment gameMenuFragment = new GameMenuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.game_fragment_frame, gameMenuFragment).commit();
        fragmentFrame.bringToFront();
        fragmentFrame.setVisibility(View.VISIBLE);
    }

    public void openPromotionFragment(byte color){
        fragmentFrame.bringToFront();
//        if (color == Const.BLACK) fragmentFrame.setRotation(180);
//        if (game.activeColor == Const.WHITE)
        fragmentFrame.setRotation(0);
        PromotionFragment promotionFragment = new PromotionFragment();
        Bundle activeColor = new Bundle();
        activeColor.putByte("color", color);
        promotionFragment.setArguments(activeColor);
        getSupportFragmentManager().beginTransaction().add(R.id.game_fragment_frame, promotionFragment).commit();
        fragmentFrame.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); in order not to close activity immediately
        leaveGame();
    }

    // GameManagement interface

    @Override
    public Game getGame() {
        return game;
    }

    @SuppressLint("ClickableViewAccessibility") // necessary for onTouch event listener
    public void initGame(int beginningTime, int addingTime){
        // players pads
        RelativeLayout whitePadLayout = findViewById(R.id.white_pad);
        FrameLayout blackPadFrame = findViewById(R.id.black_pad_frame); // instead of linLay, because of rotation
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        LinearLayout.LayoutParams padParams = new LinearLayout.LayoutParams(
                (int)(.9 * displayMetrics.widthPixels), (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, displayMetrics));
        whitePadLayout.setLayoutParams(padParams);
        blackPadFrame.setLayoutParams(padParams);

        pads = new SparseArray<>();
        PlayerPadFragment whitePlayer = PlayerPadFragment.newInstance(Const.WHITE);
        PlayerPadFragment blackPlayer = PlayerPadFragment.newInstance(Const.BLACK);

        pads.put(Const.WHITE, (PlayerPadFragment) getSupportFragmentManager().findFragmentById(R.id.white_pad));
        pads.get(Const.WHITE).setUp(Const.WHITE, beginningTime, addingTime);
        pads.put(Const.BLACK, (PlayerPadFragment) getSupportFragmentManager().findFragmentById(R.id.black_pad));
        pads.get(Const.BLACK).setUp(Const.BLACK, beginningTime, addingTime);

        pads.get(Const.WHITE).capturedPad.setDisplayMode(displayMode);
        pads.get(Const.BLACK).capturedPad.setDisplayMode(displayMode);

        // setting up the game
        resetGame();

        // setting up board
        board = findViewById(R.id.board_view);
        drawState = Const.NO_DRAW;
        board.redraw(game.pieces, game.movePointers, game.attackPointers, displayMode);
        findViewById(R.id.board_layout).bringToFront();

        board.setOnTouchListener((view, event) -> {
            Position p = board.getSquare(new Position((int) event.getX(), (int) event.getY()));
            try {
                JSONObject message = socket.getMessage();
                if(message != null)
                    game.processTouch(event, p, socket.getMessage().getBoolean("yourTurn"));
                else
                    game.processTouch(event, p, null);
            } catch (Exception e) {
                game.processTouch(event, p, null);
            }

            redrawBoard();
            return true;
        });

        // menu button
        menuButton.setVisibility(View.VISIBLE);
        menuButton.setOnClickListener(view -> {
            switch (game.state){
                case Const.STATE_END:
                    fragmentFrame.setVisibility(View.VISIBLE);
                    break;
                case Const.STATE_MOVE_ATTACK:
                case Const.STATE_SELECT:
                    openGameMenu();
                    break;
                case Const.STATE_PAUSE:
                    break;
            }
        });
    }

    @Override
    public void resetGame(){
        Piece.resetAll();
        game = new Game(this, this);
        game.start(mode, category);
        drawState = Const.NO_DRAW;
        pads.get(Const.WHITE).reset();
        pads.get(Const.BLACK).reset();
        if(fragmentFrame.getVisibility() == View.VISIBLE) {
            redrawBoard();
            getSupportFragmentManager().beginTransaction().remove(gameEndFragment).commit();
            fragmentFrame.setVisibility(View.GONE);
        }
    }

    @Override
    public void leaveGame(){
        if (game.state == Const.STATE_PAUSE) return;
        game.pause();
        fragmentFrame.bringToFront();
        fragmentFrame.setRotation(0);
        getSupportFragmentManager().beginTransaction().add(R.id.game_fragment_frame, new GameLeaveFragment()).commit();
        fragmentFrame.setVisibility(View.VISIBLE);
    }

    @Override
    public void closeFragment(Fragment fragmentToClose) {
        getSupportFragmentManager().beginTransaction().remove(fragmentToClose).commit();
        fragmentFrame.setRotation(0);
        fragmentFrame.setVisibility(View.GONE);
        if(!(fragmentToClose instanceof SetTimerFragment)) game.unpause();
    }

    @Override
    public void endGame() {
        finish();
    }

    @Override
    public void pauseGame() {
        pads.get(game.activeColor).stopTimer();
    }

    @Override
    public void unpauseGame() {
        pads.get(game.activeColor).startTimer();
    }

    @Override
    public void showBoard() {
        fragmentFrame.setVisibility(View.GONE);
    }

    @Override
    public void shareBoard(){
        // check for permissions
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            GameManagement.makeToast(R.string.toast_board_sharing_failed, Const.BLACK, this);
        }
        else{
            // prepare layout
            fragmentFrame.setVisibility(View.GONE);
            menuButton.setVisibility(View.GONE);
            brandingImage.bringToFront();
            brandingImage.setVisibility(View.VISIBLE);

            Date now = new Date();
            android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

            try {
                View view = getWindow().getDecorView().getRootView();
                view.setDrawingCacheEnabled(true);
                Bitmap screenshot = view.getDrawingCache();

                String path = Environment.getExternalStorageDirectory().toString() + "/Lets_Chess_" + now + ".jpg";
                File screenFile = new File(path);

                FileOutputStream outputStream = new FileOutputStream(screenFile);
                screenshot.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                view.setDrawingCacheEnabled(false);

                // making the screenshot show-up in gallery
                ContentValues valuesForGallery = new ContentValues();
                valuesForGallery.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                valuesForGallery.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                valuesForGallery.put(MediaStore.MediaColumns.DATA, path);
                this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valuesForGallery);


            } catch (Throwable e) {
                e.printStackTrace();
            }

            // restore layout
            fragmentFrame.bringToFront();
            fragmentFrame.setVisibility(View.VISIBLE);
            menuButton.setVisibility(View.VISIBLE);
            brandingImage.setVisibility(View.INVISIBLE);

            GameManagement.makeToast(R.string.toast_board_sharing, Const.BLACK, this);
        }
    }

    @Override
    public void manageDraw(byte color) {
        switch (drawState){
            case Const.NO_DRAW:
                drawState = color;
                pads.get(color).setDrawButtonBackground(R.drawable.draw_icon_accepted);
                pads.get(GameManagement.switchColor(color)).setDrawButtonBackground(R.drawable.draw_icon_ready);

                GameManagement.makeToast(R.string.draw_toast_text, color, this);

                break;
            case Const.WHITE:
            case Const.BLACK:
                if(color == drawState){
                    drawState = Const.NO_DRAW;
                    pads.get(Const.WHITE).setDrawButtonBackground(R.drawable.draw_icon);
                    pads.get(Const.BLACK).setDrawButtonBackground(R.drawable.draw_icon);
                }
                else{
                    pads.get(color).setDrawButtonBackground(R.drawable.draw_icon_accepted);
                    game.end(Const.DRAW);
                }
                break;
        }
    }

    @Override
    public void proceedFailure(byte color) {
        game.end(GameManagement.switchColor(color));
    }

    @Override
    public void vibrate(int time) {
        if(vibrations) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(time);
        }
    }

    @Override
    public void vibratePattern(long[] pattern, int select) {
        if(vibrations) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(pattern, select);
        }
    }
}
