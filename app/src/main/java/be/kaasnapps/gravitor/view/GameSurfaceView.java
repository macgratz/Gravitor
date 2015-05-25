package be.kaasnapps.gravitor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import be.kaasnapps.gravitor.R;
import be.kaasnapps.gravitor.model.asteroid.Asteroid;
import be.kaasnapps.gravitor.model.Game;
import be.kaasnapps.gravitor.model.GravityField;
import be.kaasnapps.gravitor.model.util.Point;
import be.kaasnapps.gravitor.model.util.Vector;

public class GameSurfaceView extends SurfaceView {

    private static final float XRANGE = 500;
    private static final float YRANGE = 500;


    private final Paint gravityFieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint planetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint asteroidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Point lastMotionLocation;
    private Point translation = new Point(0, 0);

    private SurfaceHolder surfaceHolder;
    private GameThread gameThread;
    private Game game;

    private Bitmap pngAsteroid;
    private Bitmap pngPlanet;

    public GameSurfaceView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        gravityFieldPaint.setColor(Color.RED);
        gravityFieldPaint.setStyle(Paint.Style.STROKE);
        gravityFieldPaint.setAntiAlias(true);

        planetPaint.setColor(Color.BLUE);
        planetPaint.setStyle(Paint.Style.FILL);
        planetPaint.setAntiAlias(true);

        asteroidPaint.setColor(Color.YELLOW);
        asteroidPaint.setStyle(Paint.Style.STROKE);

        scorePaint.setColor(Color.WHITE);
        scorePaint.setStyle(Paint.Style.FILL);
        scorePaint.setTextSize(50);

        pngAsteroid = BitmapFactory.decodeResource(getResources(), R.drawable.football);
        pngPlanet = BitmapFactory.decodeResource(getResources(),R.drawable.football);

        init();
    }

    private void startGame() {
        if (!gameThread.isRunning()) {
            gameThread.setRunning(true);
            gameThread.start();
        }
    }


    private void stopGame() {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    private void init() {
        gameThread = new GameThread(this);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startGame();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopGame();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point location = new Point(event.getX(), event.getY());
            location.setX(location.getX() - translation.getX());
            location.setY(location.getY() - translation.getY());
            game.addGravityWell(new GravityField(location, 0));
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            game.closeLastOpenedGravityWell();
            lastMotionLocation = null;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Point currentMotionLocation = new Point(event.getX(), event.getY());
            if (lastMotionLocation == null) {
                lastMotionLocation = new Point(event.getX(), event.getY());
            } else {
                moveToPoint(currentMotionLocation);
            }
        }
        return true;
    }

    private void moveToPoint(Point newLocation) {
        Vector vector = new Vector(lastMotionLocation, newLocation);

        //check dat canvas niet buiten onze vooraf bepaalde grenzen kan gaan
        if (translation.getX() + vector.getX() > XRANGE || translation.getX() + vector.getX() < -XRANGE) {
            vector.setX(0);
        }

        if (translation.getY() + vector.getY() > YRANGE || translation.getY() + vector.getY() < -YRANGE) {
            vector.setY(0);
        }

        if (vector.getLength() > 10) {
            game.closeLastOpenedGravityWell();
        }

        translation.add(vector);
        lastMotionLocation = newLocation;
    }

    protected void drawSomething(Canvas canvas) {
        canvas.translate((float) translation.getX(), (float) translation.getY());


        if (game == null) {
            game = new Game(canvas.getWidth(), canvas.getHeight());
        }
        canvas.drawColor(Color.BLACK);
        game.tick();

        canvas.drawCircle(
                (float) game.getPlanet().getLocation().getX(),
                (float) game.getPlanet().getLocation().getY(),
                (float) game.getPlanet().getRadius(),
                planetPaint);
        canvas.drawCircle(
                (float) game.getPlanet().getGravityField().getLocation().getX(),
                (float) game.getPlanet().getGravityField().getLocation().getY(),
                (float) game.getPlanet().getGravityField().getRadius(),
                gravityFieldPaint);

        for (GravityField gravityWell : game.getGravityFields()) {
            canvas.drawCircle(
                    (float) gravityWell.getLocation().getX(),
                    (float) gravityWell.getLocation().getY(),
                    (float) gravityWell.getRadius(),
                    gravityFieldPaint);
        }
        for (Asteroid asteroid : game.getAsteroids()) {
            //canvas.drawCircle(
            //        (float) asteroid.getLocation().getX(),
            //        (float) asteroid.getLocation().getY(),
            //        (float) asteroid.getRadius(),
            //        asteroidPaint);
            canvas.drawBitmap(
                    pngAsteroid,
                    (float) asteroid.getLocation().getX(),
                    (float) asteroid.getLocation().getY(),
                    asteroidPaint
            );
        }

        canvas.drawText(
                ""+game.getScore(),
                (float) (-translation.getX()+80),
                (float) (-translation.getY()+80),
                scorePaint);
    }
}

