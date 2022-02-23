import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    int score;
    int maxTile;
    private Stack<Tile[][]> previousStates;
    private Stack<Integer> previousScores;
    private boolean isSaveNeeded = true;


    public Model() {
        resetGameTiles();
        this.score = 0;
        this.maxTile = 0;
        previousStates = new Stack<>();
        previousScores = new Stack<>();
    }

    private boolean hasBoardChanged() {
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[0].length; j++) {
                if (gameTiles[i][j].value != previousStates.peek()[i][j].value)
                    return true;
            }
        }
        return false;
    }

    MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }

    void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.peek().getMove().move();
    }

    void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                up();
                break;
            case 2:
                right();
                break;
            case 3:
                down();
                break;
        }
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] tilesss = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                tilesss[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tilesss);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = (Tile[][]) previousStates.pop();
            score = (int) previousScores.pop();
        }
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    boolean canMove() {
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[0].length; j++) {
                if (j > 0 && j < gameTiles[0].length - 1 && (gameTiles[i][j].value == gameTiles[i][j - 1].value || gameTiles[i][j].value == gameTiles[i][j + 1].value))
                    return true;
                if (i > 0 && i < gameTiles.length - 1 && (gameTiles[i][j].value == gameTiles[i - 1][j].value || gameTiles[i][j].value == gameTiles[i + 1][j].value))
                    return true;
            }
        }
        if (!getEmptyTiles().isEmpty()) return true;
        return false;
    }

    private void addTile() {
        if (getEmptyTiles().size() != 0)
            getEmptyTiles().get((int) (Math.random() * getEmptyTiles().size())).value = (Math.random() < 0.9 ? 2 : 4);
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> list = new ArrayList<>();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].isEmpty()) {
                    list.add(gameTiles[i][j]);
                }
            }
        }
        return list;
    }

    void resetGameTiles() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                this.gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean isEdit = false;
        for (int i = 0; i < tiles.length; i++) {
            int index = 0;
            while(index < tiles.length - 1) {
                if (tiles[index].value == 0) {
                    if (tiles[index + 1].value == 0) {
                        index++;
                        continue;
                    }
                    tiles[index].value = tiles[index + 1].value;
                    tiles[index + 1].value = 0;
                    index++;
                    isEdit = true;

                }else {
                    index++;
                    continue;
                }
            }
        }
        return isEdit;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isEdit = false;
        compressTiles(tiles);
        int index = 0;
        while(index < tiles.length - 1) {
            if (tiles[index].value == tiles[index + 1].value) {
                tiles[index].value *= 2;
                tiles[index + 1] = new Tile();
                if (tiles[index].value > maxTile)
                    maxTile = tiles[index].value;
                score += tiles[index].value;
                compressTiles(tiles);
                if (!tiles[index].isEmpty())
                    isEdit = true;
            }
            index++;
        }
        return isEdit;
    }

    void left() {
        if (isSaveNeeded)
            saveState(gameTiles);
        boolean isEdit = false;
        for (int i = 0; i < gameTiles.length; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                isEdit = true;
            }
        }
        if (isEdit) addTile();
        isSaveNeeded = true;
    }

    void right() {
        saveState(gameTiles);
        rotateRight();
        rotateRight();
        left();
        rotateRight();
        rotateRight();
    }

    void up() {
        saveState(gameTiles);
        rotateRight();
        rotateRight();
        rotateRight();
        left();
        rotateRight();
    }

    void down() {
        saveState(gameTiles);
        rotateRight();
        left();
        rotateRight();
        rotateRight();
        rotateRight();
    }

    private void rotateRight() {
        Tile[][] resultArray = new Tile[gameTiles[0].length][gameTiles.length];
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                resultArray[j][gameTiles.length - i - 1] = gameTiles[i][j];
            }
        }
        gameTiles = Arrays.copyOf(resultArray, resultArray.length);
    }
}