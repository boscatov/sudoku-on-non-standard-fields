package sudoku.newgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import sudoku.newgame.dancinglinks.Algorithm;
import sudoku.newgame.dancinglinks.Structure;
import sudoku.newgame.draw.Border;
import sudoku.newgame.draw.DrawBoard;
import sudoku.newgame.draw.DrawCell;
import sudoku.newgame.sudoku.Board;

/**
 * Created by sanya on 13.01.2018.
 */
public class DrawBoardGeneratorView extends View {
    public DrawCell[][] board;
    public byte[] prpr;
    public Canvas canvas;
    int w;
    int h;
    int currentSize;
    boolean[] possibleCells;
    CellPosition[] currentHighlighted;
    int counter;
    byte currentArea = 0;
    float startY = 40;
    float startX = 10;
    Paint p;
    Context context;
    boolean[][] isVisited;
    public DrawBoardGeneratorView(Context context){
        super(context);
        this.context = context;
        p = new Paint();

    }
    public DrawBoardGeneratorView(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.context = context;
        p = new Paint();
    }

    public DrawBoardGeneratorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        p = new Paint();
    }

    public DrawBoardGeneratorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        p = new Paint();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        p.setColor(Color.BLACK);
        if(board == null)
            creation();
        int n = 9;
        for(int i = 0; i < n; ++i)
            for(int z = 0; z < n; ++z)
                board[i][z].draw(p,canvas);
        for(int i = 0; i < n; ++i)
            for(int z = 0; z < n; ++z)
                board[i][z].drawBoard(p,canvas);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }
    public void creation(){
        int n = 9;
        isVisited = new boolean[n][n];
        currentHighlighted = new CellPosition[n];
        currentSize = 0;
        possibleCells = new boolean[n*n];
        prpr = new byte[n*n];
        for(int i = 0; i < n*n;++i) {
            prpr[i] = -1;
            possibleCells[i] = false;
        }
        prpr = new byte[n*n];
        for(int i = 0; i < n*n;++i)
            prpr[i] = -1;
        float sizeY = startY;
        Point size = new Point();
        getDisplay().getSize(size);
        w = size.x;
        float length = (w-2*10)/n;
        board = new DrawCell[n][n];
        for(int i = 0; i < n; ++i) {
            float sizeX = startX;
            for (int z = 0; z < n; ++z) {
                board[i][z] = new DrawCell(new Border(z == 0,
                        z == (n - 1),
                        i == 0,
                        i == (n - 1)),
                        sizeX, sizeY, length);
                sizeX += length;
            }
            sizeY += length;
        }
    }
    void declineArea(){
        currentArea--;
        currentSize = 0;
        for(int i = 0; i < board.length * board.length; ++i)
            if(prpr[i] == currentArea) {
                prpr[i] = -1;
                board[i/board.length][i%board.length].setFillColor(Color.WHITE);
            }
        refreshBorders();
        if(currentArea == 0) {
            Log.d("Decline area", "Button invisible");
            getRootView().findViewById(R.id.button51).setVisibility(INVISIBLE);
        }
        invalidate();
    }
    void refreshBorders(){
        for(int i = 0; i < board.length; ++i) {
            for (int z = 0; z < board.length; ++z) {
                board[i][z].border.left = z == 0 || prpr[board.length*i+z-1] != prpr[board.length*i+z];
                board[i][z].border.right = z == (board.length - 1) || prpr[board.length*i+z+1]!=prpr[board.length*i+z];
                board[i][z].border.up = i == 0 || prpr[board.length*i+z-board.length]!=prpr[board.length*i+z];
                board[i][z].border.down = i == (board.length - 1) || prpr[board.length*i+z+board.length]!=prpr[board.length*i+z];
            }
        }
    }
    public void focusOnCell(float x, float y, int color){
        x -= startX;
        y -= startY;
        int n = 9;
        int posx = (int)x/((w-2*10)/n);
        int posy = (int)y/((w-2*10)/n);
        if(currentSize == n) {
            getRootView().findViewById(R.id.button50).setBackgroundColor(Color.RED);
            return;
        }
        if(posy < n && posx < n)
        {
            if(board[posy][posx].getFillColor()==Color.YELLOW)
                return;
            if(board[posy][posx].getFillColor()==color) {
                deleteFromSequence(posx, posy);
                refreshPossibleCells();
                invalidate();
                return;
            }
            if(!(possibleCells[board.length*posy+posx]||(currentSize==0 && board[posy][posx].getFillColor()==Color.WHITE))){
                Toast.makeText(context, "Не в ту степь", Toast.LENGTH_LONG).show();
                return;
            }
            currentHighlighted[currentSize++] = new CellPosition(posx,posy,board[posy][posx]);
            board[posy][posx].setFillColor(color);
            refreshPossibleCells();
        }
        if(currentSize == n) {
            if(!checkCell())
                Toast.makeText(context, "Такая себе поляна", Toast.LENGTH_LONG).show();
            else
                getRootView().findViewById(R.id.button50).setVisibility(VISIBLE);

        }
        invalidate();
    }
    void refreshPossibleCells(){
        for(int i = 0; i < board.length*board.length; ++i)
            possibleCells[i] = false;
        for(int i = 0; i < currentSize; ++i){
            if(currentHighlighted[i].y-1>-1)
                possibleCells[board.length*(currentHighlighted[i].y-1)+currentHighlighted[i].x] = true;
            if(currentHighlighted[i].x-1>-1)
                possibleCells[board.length*(currentHighlighted[i].y)+currentHighlighted[i].x-1] = true;
            if(currentHighlighted[i].y+1<board.length)
                possibleCells[board.length*(currentHighlighted[i].y+1)+currentHighlighted[i].x] = true;
            if(currentHighlighted[i].x+1<board.length)
                possibleCells[board.length*(currentHighlighted[i].y)+currentHighlighted[i].x+1] = true;
        }
    }
    boolean saveArea(){
        for(int i = 0; i < board.length;++i){
            currentHighlighted[i].drawCell.setFillColor(Color.YELLOW);
            prpr[currentHighlighted[i].y*board.length + currentHighlighted[i].x] = currentArea;
        }
        currentArea++;
        currentSize = 0;
        refreshPossibleCells();
        refreshBorders();
        if(currentArea > 0) {
            Log.d("Save area","Button visible");
            getRootView().findViewById(R.id.button51).setVisibility(VISIBLE);
        }
        invalidate();
        return currentArea == board.length;
    }

    void deleteFromSequence(int x, int y){
        board[y][x].setFillColor(Color.WHITE);
        if(!checkDeleteFromSequence()){
            Log.d("Delete highlighted", "Chain gap");
            board[y][x].setFillColor(Color.BLUE);
            return;
        }
        for(int i = 0; i < currentSize; ++i)
            if(currentHighlighted[i].drawCell.getFillColor()==Color.WHITE) {
                currentHighlighted[i] = currentHighlighted[--currentSize];
                return;
            }
    }
    boolean checkDeleteFromSequence(){
        int x = currentHighlighted[0].x;
        int y = currentHighlighted[0].y;
        for(int i = 0; i < board.length; ++i)
            for(int z = 0; z < board.length; ++z)
                isVisited[i][z] = board[z][i].getFillColor()!=Color.BLUE;
        for(int i = 1; i < currentSize; ++i)
            if(currentHighlighted[i].drawCell.getFillColor()==Color.BLUE) {
                x = currentHighlighted[i].x;
                y = currentHighlighted[i].y;
                break;
            }

        counter = 1;
        isFullHighlightedArea(x, y);
        Log.d("Deleting from sequence",counter+"");
        return counter == currentSize-1;
    }
    void isFullHighlightedArea(int x, int y){
        isVisited[x][y] = true;
        if(x-1 >= 0 && !isVisited[x-1][y]){
            counter++;
            isFullHighlightedArea(x-1, y);
        }
        if(x+1 < board.length && !isVisited[x+1][y]){
            counter++;
            isFullHighlightedArea(x+1, y);
        }
        if(y-1 >= 0 && !isVisited[x][y-1]){
            counter++;
            isFullHighlightedArea(x, y-1);
        }
        if(y+1 < board.length && !isVisited[x][y+1]){
            counter++;
            isFullHighlightedArea(x, y+1);
        }
    }
    boolean checkCell(){
        for(int i = 0; i < board.length; ++i)
            for(int z = 0; z < board.length; ++z)
                isVisited[i][z] = board[z][i].getFillColor()==Color.BLUE;
        for(int i = 0; i < board.length; ++i)
            for(int z = 0; z < board.length; ++z){
                if(!isVisited[i][z]&&board[z][i].getFillColor()==Color.WHITE) {
                    counter = 1;
                    isFullArea(i, z);
                    if(counter % board.length != 0)
                        return false;
                }
            }
        return true;
    }
    void isFullArea(int x, int y){
        isVisited[x][y] = true;
        if(x-1 >= 0 && board[y][x-1].getFillColor()==Color.WHITE && !isVisited[x-1][y]){
            counter++;
            isFullArea(x-1, y);
        }
        if(x+1 < board.length && board[y][x+1].getFillColor()==Color.WHITE && !isVisited[x+1][y]){
            counter++;
            isFullArea(x+1, y);
        }
        if(y-1 >= 0 && board[y-1][x].getFillColor()==Color.WHITE && !isVisited[x][y-1]){
            counter++;
            isFullArea(x, y-1);
        }
        if(y+1 < board.length && board[y+1][x].getFillColor()==Color.WHITE && !isVisited[x][y+1]){
            counter++;
            isFullArea(x, y+1);
        }
    }
}
