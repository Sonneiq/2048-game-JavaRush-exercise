package task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    int score = 0;
    int maxTile = 0;

    private Stack<Tile[][]> previousStates;
    private Stack<Integer> previousScores;
    private boolean isSaveNeeded = true;

    private Tile[][] gameTiles;
    
    public Model() {
        resetGameTiles();
        previousStates = new Stack<>();
        previousScores = new Stack<>();
    }

    private void saveState(Tile[][] tile) {
        Tile[][] tiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < FIELD_WIDTH; i++) {
            for(int j = 0; j < FIELD_WIDTH; j++) {
                tiles[i][j] = new Tile(tile[i][j].value);
            }
        }
        previousStates.push(tiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if(!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void addTile() {
        List<Tile> tiles = getEmptyTiles();
        if(!tiles.isEmpty()) {
            int index = (int) (Math.random() * tiles.size()) % tiles.size();
            Tile tile = tiles.get(index);
            tile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> result = new ArrayList<>();
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 0; j < gameTiles[0].length; j++) {
                if(gameTiles[i][j].isEmpty()) {
                    result.add(gameTiles[i][j]);
                }
            }
        }
        return result;
    }

    public void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 0; j < gameTiles[0].length; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        int insertPosition = 0;
        boolean result = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    result = true;
                }
                insertPosition++;
            }
        }
        return result;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean result = false;
        LinkedList<Tile> tilesList = new LinkedList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }

            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updatedValue = tiles[i].value * 2;
                if (updatedValue > maxTile) {
                    maxTile = updatedValue;
                }
                score += updatedValue;
                tilesList.addLast(new Tile(updatedValue));
                tiles[i + 1].value = 0;
                result = true;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }

        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }

        return result;
    }

    private Tile[][] rotateClockwise(Tile[][] tiles) {
        final int N = tiles.length;
        Tile[][] result = new Tile[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                result[c][N - 1 - r] = tiles[r][c];
            }
        }
        return result;
    }

    public void left() {
        if(isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean moveFlag = false;
        for(int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                moveFlag = true;
            }
        }
        if(moveFlag) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public void up() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    public void down() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private boolean isFull() {
        return getEmptyTiles().size() == 0;
    }

    public boolean canMove() {
        if(!isFull()) {
            return true;
        }

        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < FIELD_WIDTH; y++) {
                Tile t = gameTiles[x][y];
                if ((x < FIELD_WIDTH - 1 && t.value == gameTiles[x + 1][y].value)
                        || ((y < FIELD_WIDTH - 1) && t.value == gameTiles[x][y + 1].value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        int value1 = 0;
        int value2 = 0;
        Tile[][] lastGameTiles = previousStates.peek();
        for(int i = 0; i < FIELD_WIDTH; i++) {
            for(int j = 0; j < FIELD_WIDTH; j++) {
                value1 += gameTiles[i][j].value;
                value2 += lastGameTiles[i][j].value;
            }
        }
        return value1 != value2;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if(hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.add(getMoveEfficiency(this::left));
        queue.add(getMoveEfficiency(this::right));
        queue.add(getMoveEfficiency(this::up));
        queue.add(getMoveEfficiency(this::down));
        queue.peek().getMove().move();
    }
}
