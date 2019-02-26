package com.example.luisosorio.snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

@SuppressLint("ViewConstructor")
class SnakeView extends SurfaceView implements Runnable {

    private Thread th = null;
    private volatile boolean jugando;
    private Canvas canvas;
    private SurfaceHolder holder;
    private Paint paint;
    private Context context;

    // variables de movimiento
    public enum Direction {UP, RIGHT, DOWN, LEFT}
    private Direction dir = Direction.RIGHT;

    // Resolucion del movil
    private int anchoPantalla;
    private int largoPantalla;

    private long nextFrame;
    private final long FPS = 10;
    private final long MILLIS_IN_A_SECOND = 1000;
    private int puntuacion;

    // Posicion de todas las partes de la serpiente
    private int[] SnakeXs;
    private int[] SnakeYs;

    // Longitud de la serpiente
    private int longitudSerpiente;

    private int comestibleX;
    private int comestibleY;

    private int BlockSize;

    private final int ANCHO = 40;
    private int ALTO; // se determina la altura en funcion del movil

    public SnakeView(Context context, Point size) {
        super(context);

        this.context = context;

        anchoPantalla = size.x;
        largoPantalla = size.y;

        //determinar el tablero de juego
        BlockSize = anchoPantalla / ANCHO;
        // altura a la que aparecen los comestibles
        ALTO = ((largoPantalla -15)) / BlockSize;


        // se inicializan los drawables
        holder = getHolder();
        paint = new Paint();

        // Puntuacion máxima.
        SnakeXs = new int[200];
        SnakeYs = new int[200];

        startGame();
    }

    @Override
    public void run() {

        while (jugando) {

            if(checkForUpdate()) {
                updateGame();
                drawGame();
            }

        }
    }


    public void startGame() {

        longitudSerpiente = 10;
        SnakeXs[0] = ANCHO / 2;
        SnakeYs[0] = ALTO / 2;

        spawnMouse();

        puntuacion = 0;
        nextFrame = System.currentTimeMillis();
    }

    public void spawnMouse() {
        Random random = new Random();
        comestibleX = random.nextInt(ANCHO - 1) + 1;
        comestibleY = random.nextInt(ALTO - 1) + 1;
    }

    private void eatMouse(){
        //Se incrementa longitud de la serpiente cuando come
        longitudSerpiente++;
        spawnMouse();
        //sumar puntuacion
        puntuacion = puntuacion + 1;
    }

    private void moveSnake(){

        for (int i = longitudSerpiente; i > 0; i--) {

            SnakeXs[i] = SnakeXs[i - 1];
            SnakeYs[i] = SnakeYs[i - 1];

        }

        // mover la serpiente en funcion de la dirección
        switch (dir) {
            case UP:
                SnakeYs[0]--;
                break;

            case RIGHT:
                SnakeXs[0]++;
                break;

            case DOWN:
                SnakeYs[0]++;
                break;

            case LEFT:
                SnakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath(){

        boolean dead = false;

        // detectar colision con el muro
        if (SnakeXs[0] == -1) dead = true;
        if (SnakeXs[0] >= ANCHO) dead = true;
        if (SnakeYs[0] == -1) dead = true;
        if (SnakeYs[0] == ALTO) dead = true;

        // Detectar si se ha chocado consigo misma
        for (int i = longitudSerpiente - 1; i > 0; i--) {
            if ((i > 4) && (SnakeXs[0] == SnakeXs[i]) && (SnakeYs[0] == SnakeYs[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void updateGame() {
        //si ha comido
        if (SnakeXs[0] == comestibleX && SnakeYs[0] == comestibleY) {
            eatMouse();
        }

        moveSnake();
        //si se ha muerto, reiniciamos el juego
        if (detectDeath()) {
            startGame();
        }
    }

    public void drawGame() {

        //Dibujar el tablero de juego
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();

            // color del tablero
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            // color de la serpiente y comestible
            paint.setColor(Color.argb(255, 255, 255, 255));

            //tamaño y posicion de la puntuacion
            paint.setTextSize(60);
            canvas.drawText("Puntos:" + puntuacion, 20, 60, paint);


            //Dibujar la serpiente
            for (int i = 0; i < longitudSerpiente; i++) {
                canvas.drawRect(SnakeXs[i] * BlockSize,
                        (SnakeYs[i] * BlockSize),
                        (SnakeXs[i] * BlockSize) + BlockSize,
                        (SnakeYs[i] * BlockSize) + BlockSize,
                        paint);
            }

            //dibujar el comestible
            canvas.drawRect(comestibleX * BlockSize,
                    (comestibleY * BlockSize),
                    (comestibleX * BlockSize) + BlockSize,
                    (comestibleY * BlockSize) + BlockSize,
                    paint);

            // Dibujar el frame
            holder.unlockCanvasAndPost(canvas);
        }

    }

    public boolean checkForUpdate() {

        // refresco de pantalla por segundo
        if(nextFrame <= System.currentTimeMillis()){
            nextFrame =System.currentTimeMillis() + MILLIS_IN_A_SECOND / FPS;

            return true;
        }

        return false;
    }

    public void resume() {
        jugando = true;
        th = new Thread(this);
        th.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= anchoPantalla / 2) {
                    switch(dir){
                        case UP:
                            dir = Direction.RIGHT;
                            break;
                        case RIGHT:
                            dir = Direction.DOWN;
                            break;
                        case DOWN:
                            dir = Direction.LEFT;
                            break;
                        case LEFT:
                            dir = Direction.UP;
                            break;
                    }
                } else {
                    switch(dir){
                        case UP:
                            dir = Direction.LEFT;
                            break;
                        case LEFT:
                            dir = Direction.DOWN;
                            break;
                        case DOWN:
                            dir = Direction.RIGHT;
                            break;
                        case RIGHT:
                            dir = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }
}
