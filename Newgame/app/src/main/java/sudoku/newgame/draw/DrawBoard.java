package sudoku.newgame.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import sudoku.newgame.Event;
import sudoku.newgame.History;
import sudoku.newgame.sudoku.Board;

/**
 * Created by sanya on 13.01.2018.
 */

public class DrawBoard {
    public Board bd;
    public DrawCell[][] board;
    public float startX;
    public float startY;
    public byte[] structure;
    public History gameHistory;
    Paint p;
    int n;
    public DrawBoard(float startX, float startY, float length, byte[] structure, Board bd, int n){
        p = new Paint();
        this.gameHistory = new History();
        this.n = n;
        this.startX = startX;
        this.startY = startY;
        this.bd = bd;
        this.structure = structure;
        float sizeY = startY;
        board = new DrawCell[n][n];
        for(int i = 0; i < n; ++i){
            float sizeX = startX;
            for(int z = 0; z < n;++z){
                board[i][z] = new DrawCell(new Border(z%n==0 ||
                        structure[n*i+z-1]!=structure[n*i+z],
                        z%n==(n-1) || structure[n*i+z+1]!=structure[n*i+z],
                        i%n==0 || structure[n*i+z-n]!=structure[n*i+z],
                        i%n==(n-1) || structure[n*i+z+n]!=structure[n*i+z]),
                        sizeX,sizeY,length);
                sizeX+=length;
            }
            sizeY+=length;
        }
    }
    public DrawBoard(float startX, float startY, float length, byte[] structure, int n) {
        p = new Paint();
        this.n = n;
        this.structure = structure;
        float sizeY = startY;
        board = new DrawCell[n][n];
        for(int i = 0; i < n; ++i){
            float sizeX = startX;
            for(int z = 0; z < n;++z){
                board[i][z] = new DrawCell(new Border(z%n==0 ||
                        structure[n*i+z-1]!=structure[n*i+z],
                        z%n==(n-1) || structure[n*i+z+1]!=structure[n*i+z],
                        i%n==0 || structure[n*i+z-n]!=structure[n*i+z],
                        i%n==(n-1) || structure[n*i+z+n]!=structure[n*i+z]),
                        sizeX,sizeY,length);
                sizeX+=length;
            }
            sizeY+=length;
        }
    }
    public void changeLength(float length){
        float sizeY = startY;
        for(int i = 0; i < n; ++i){
            float sizeX = startX;
            for(int z = 0; z < n;++z){
                board[i][z].changeLength(sizeX,sizeY,length);
                sizeX+=length;
            }
            sizeY+=length;
        }
    }

    public void draw(Canvas canvas, Paint paint){

        for(int i = 0; i < n; ++i)
            for(int z = 0; z < n; ++z)
                board[i][z].draw(paint,canvas);

        for(int i = 0; i < n; ++i){
            for(int z = 0; z < n;++z){
                board[i][z].drawBoard(paint,canvas);
                if(bd.cells[i][z].isInput) {
                    board[i][z].setTextColor(Color.BLACK);
                    board[i][z].writeText(paint, canvas, bd.cells[i][z].value);
                }
                else if(bd.cells[i][z].value!=-1) {
                    if(isCorrect(i,z))
                        board[i][z].setTextColor(Color.BLUE);
                    else
                        board[i][z].setTextColor(Color.RED);
                    board[i][z].writeText(paint, canvas, bd.cells[i][z].value);
                }
                else
                    board[i][z].writePossibleValues(paint, canvas, bd.cells[i][z].possibleValues);
            }
        }
    }
    public void drawBitmap(Canvas canvas, Paint paint) {
        for(int i = 0; i < n; ++i)
            for(int z = 0; z < n; ++z)
                board[i][z].draw(paint,canvas);

        for(int i = 0; i < n; ++i)
            for(int z = 0; z < n;++z)
                board[i][z].drawBoard(paint,canvas);
    }

    public void focusOnCell(float x, float y, int w, int color, int highlightColor){
        x -= startX;
        y -= startY;
        float length = board[0][0].length;
        int posx = (int)(x/(length));
        int posy = (int)(y/(length));
        if(posy < n && posx < n) {
            board[posy][posx].setFillColor(color);
            highlightCell(posx,posy,highlightColor);
        }
    }
    public void hint(float x, float y) {
        x -= startX;
        y -= startY;
        float length = board[0][0].length;
        int posx = (int)(x/(length));
        int posy = (int)(y/(length));
        if(posy < n && posx < n) {
            bd.cells[posy][posx].writeCorrectValue();
        }
    }
    public void undo() {
        Event last = gameHistory.getLastEvent();
        if(last == null)
            return;
        Log.d("Undo","Value: " + last.getValue() + " x: " + last.getX() + " y: " + last.getY());
        int posx = last.getX();
        int posy = last.getY();
        if(last.isEnter()) {
            if(last.isPen()) {
                bd.cells[posy][posx].value = -1;
                highlightCell(posx,posy,Color.rgb(153,204,255));
            }
            else {
                bd.cells[posy][posx].possibleValues[last.getValue()-1] =
                        !bd.cells[posy][posx].possibleValues[last.getValue()-1];
                highlightCell(posx,posy,Color.rgb(153,204,255));
            }
        }
        else if(last.isPen()) {
            bd.cells[posy][posx].value = (byte)last.getValue();
            highlightCell(posx,posy,Color.rgb(153,204,255));
        }
        else {
            bd.cells[posy][posx].possibleValues[last.getValue()-1] =
                    !bd.cells[posy][posx].possibleValues[last.getValue()-1];
            highlightCell(posx,posy,Color.rgb(153,204,255));
        }
    }
    public void refreshAll(){
        for(int i = 0; i < n; ++i)
            for(int j = 0; j < n; ++j)
                    board[i][j].setFillColor(Color.WHITE);
    }
    void highlightCell(int x, int y, int highlightColor){
        int value = bd.cells[y][x].value;
        if(value == -1)
            return;
        for(int i = 0; i < n; ++i)
            for(int j = 0; j < n; ++j) {
                if(j == x || i == y || bd.areas[n * y + x] == bd.areas[n * i + j])
                    bd.cells[i][j].possibleValues[value-1] = false;
                if (bd.cells[i][j].value == value) {
                    if(!(i==y && j==x))
                        board[i][j].setFillColor(Color.GREEN);
                    if ((i == y && j != x) || (i != y && j == x) ||
                            (i != y && j != x && bd.areas[n * y + x] == bd.areas[n * i + j]))
                        board[y][x].setFillColor(Color.rgb(255, 204, 204));
                }
                else if(i==y || j==x || bd.areas[n * y + x] == bd.areas[n * i + j])
                    board[i][j].setFillColor(highlightColor);
            }
    }
    public void setBasicPencilValues() {
        for(int i = 0; i < n; ++i) {
            for(int j = 0; j < n; ++i) {
                if(!bd.cells[i][j].isInput) {
                    for(int q = 0; q < n; ++q) {
                        bd.cells[i][j].possibleValues[q] = true;
                    }
                }
            }
        }
        removeAllPencilValues();
    }
    private void removeAllPencilValues() {
        for(int x = 0; x < n; ++x) {
            for(int y = 0; y < n; ++y) {
                if(bd.cells[x][y].isInput) {
                    for(int i = 0; i < n; ++i) {
                        for(int j = 0; j < n; ++j) {
                            if(j == x || i == y || bd.areas[n * y + x] == bd.areas[n * i + j])
                                bd.cells[i][j].possibleValues[bd.cells[x][y].value-1] = false;
                        }
                    }
                }
            }
        }
    }
    private boolean isCorrect(int x, int y){
        for(int i = 0; i < n; ++i)
            for(int j = 0; j < n; ++j)
                if(bd.cells[i][j].value == bd.cells[x][y].value){
                    if(!(x==i&&y==j)) {
                        if (bd.areas[n * x + y] == bd.areas[n * i + j])
                            return false;
                        if (x == i || y == j)
                            return false;
                    }
                }
        return true;
    }
    public void setValue(float x, float y, String value, int w){
        x -= startX;
        y -= startY;
        float length = board[0][0].length;
        int posx = (int)(x/(length));
        int posy = (int)(y/(length));
        if(posy < n && posx < n && !bd.cells[posy][posx].isInput) {
            bd.cells[posy][posx].value = Byte.valueOf(value);
            gameHistory.addEvent(Integer.valueOf(value),true,true, posx, posy);
            highlightCell(posx,posy,Color.rgb(153,204,255));
        }
    }
    public void clearPencil(float x, float y){
        x -= startX;
        y -= startY;
        float length = board[0][0].length;
        int posx = (int)(x/(length));
        int posy = (int)(y/(length));
        if(posy < n && posx < n && !bd.cells[posy][posx].isInput) {
            for(int i = 0; i < bd.cells[posy][posx].possibleValues.length; ++i) {
                if(bd.cells[posy][posx].possibleValues[i]) {
                    gameHistory.addEvent(i + 1, false, false, posx, posy);
                }
                bd.cells[posy][posx].possibleValues[i] = false;
            }
        }
    }
    public void setPencilValue(float x, float y, String value){
        x -= startX;
        y -= startY;
        float length = board[0][0].length;
        int posx = (int)(x/(length));
        int posy = (int)(y/(length));
        if(posy < n && posx < n && !bd.cells[posy][posx].isInput) {
            Log.d("setPencilValue","Setting pencil value");
            if(!bd.cells[posy][posx].possibleValues[Byte.valueOf(value)-1]){
                gameHistory.addEvent(Integer.valueOf(value), false, true, posx, posy);
            }
            else {
                gameHistory.addEvent(Integer.valueOf(value), false, false, posx, posy);
            }
            bd.cells[posy][posx].possibleValues[Byte.valueOf(value)-1] =
                    !bd.cells[posy][posx].possibleValues[Byte.valueOf(value)-1];
            highlightCell(posx,posy,Color.rgb(153,204,255));
        }
    }
}
