package com.example.luisosorio.snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

    private int comestibleVenenosoX;
    private int comestibleVenenosoY;

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

        //spawnMouse();
        TimerTask timerTask = new TimerTask(){
            public void run() {
                spawnMouse();
            }

        };

        TimerTask timerTaskVn = new TimerTask() {
            @Override
            public void run() {
                spawnMouseVn();
            }
        };

        Timer timer = new Timer();
        Timer timerVn = new Timer();
        timerVn.scheduleAtFixedRate(timerTaskVn,0,10000);
        timer.scheduleAtFixedRate(timerTask, 0, 15000);

        puntuacion = 0;
        nextFrame = System.currentTimeMillis();
    }

    public void spawnMouse() {
        Random random = new Random();
        comestibleX = random.nextInt(ANCHO - 1) + 1;
        comestibleY = random.nextInt(ALTO - 1) + 1;
    }

    public void spawnMouseVn() {
        Random random = new Random();
        comestibleVenenosoX = random.nextInt(ANCHO - 1) + 1;
        comestibleVenenosoY = random.nextInt(ALTO - 1) + 1;
    }


    private void eatMouse(){
        //Se incrementa longitud de la serpiente cuando come
        longitudSerpiente++;
        spawnMouse();
        //sumar puntuacion
        puntuacion = puntuacion + 1;
    }

    //Como la serpiente muere por comer comida en mal estado reiniciamos
    private void eatMouseVn(){
        drawScore();
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
        if (SnakeXs[0] == comestibleVenenosoX && SnakeYs[0] == comestibleVenenosoY) {
            eatMouseVn();
        }

        moveSnake();
        //si se ha muerto, reiniciamos el juego
        if (detectDeath()) {
            /*int [] ranking=new int[5];

            for(int i=0;i<5;i++){
                ranking[i]=0;
            }

            try {
                File file= new File("/raw/puntuaciones.txt");
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String linea;
                int i=0;
                while ((linea = br.readLine()) != null) {
                    int l = Integer.parseInt(linea);
                    ranking[i]=l;
                    i++;

                }
            }catch (IOException ex){
                System.err.println(ex.getMessage());
            }

            //ESCRIBIMOS PUNTUACION
            try{
                FileWriter fw= new FileWriter("puntuaciones.txt");
                BufferedWriter bw= new BufferedWriter(fw);
                PrintWriter pw= new PrintWriter(bw);

                for(int i=0;i<5;i++){
                    if(puntuacion>=ranking[i]){
                        pw.println(puntuacion);
                        pw.flush();
                    }
                }



            }catch (IOException ex){
                System.err.println(ex.getMessage());
            }*/

            drawScore();

        }

    }
    public void drawScore() {

        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            paint.setTextSize(150);
            paint.setColor(Color.argb(255, 255, 255, 255));
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            canvas.drawText("RANKING", 60, 150, paint);
            canvas.drawText("1 -  12 puntos", 100, 450, paint);
            canvas.drawText("2 -   9 puntos", 100, 650, paint);
            canvas.drawText("3 -   7 puntos", 100, 850, paint);
            canvas.drawText("4 -   6 puntos", 100, 1050, paint);
            canvas.drawText("5 -   2 puntos", 100, 1250, paint);
            /*canvas.drawText("1 -  " + ranking[0], 100, 450, paint);
            canvas.drawText("2 -  " + ranking[1], 100, 650, paint);
            canvas.drawText("3 -  " + ranking[2], 100, 850, paint);
            canvas.drawText("4 -  " + ranking[3], 100, 1050, paint);
            canvas.drawText("5 -  " + ranking[4], 100, 1250, paint);*/
            holder.unlockCanvasAndPost(canvas);

        }
        try {
            Thread.sleep(5000);
        }catch(Exception ex) {
            System.err.println(ex.getMessage());
        }
            startGame();
    }



        public void drawGame(){

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

            //dibujar el comestible venenoso
            canvas.drawRect(comestibleVenenosoX * BlockSize,
                    (comestibleVenenosoY * BlockSize),
                    (comestibleVenenosoX * BlockSize) + BlockSize,
                    (comestibleVenenosoY * BlockSize) + BlockSize,
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
