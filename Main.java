import java.util.ArrayList;
import java.util.Stack;
import java.util.Scanner;
import java.util.HashMap;
class Piece {
    String type;
    String color;
    boolean hasMoved;
    public Piece(String type, String color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }
    public String getType() {
        return type;
    }
    public String getColor() {
        return color;
    }
    public boolean hasMoved() {
        return hasMoved;
    }
    public void setMoved() {
        this.hasMoved = true;
    }
    public void resetMoved() {
        this.hasMoved = false;
    }
    public String getSymbol() {
        String symbol;
        switch (type) {
            case "King":   symbol = "K"; break;
            case "Queen":  symbol = "Q"; break;
            case "Rook":   symbol = "R"; break;
            case "Bishop": symbol = "B"; break;
            case "Knight": symbol = "N"; break;
            case "Pawn":   symbol = "P"; break;
            default:       symbol = "?"; break;
        }
        if (color.equals("black")) {
            symbol = symbol.toLowerCase();
        }
        return symbol;
    }
    @Override
    public String toString() {
        return color + " " + type;
    }
    static HashMap<String, Integer> pieceValues = new HashMap<>();
    static {
        pieceValues.put("Pawn", 1);
        pieceValues.put("Knight", 3);
        pieceValues.put("Bishop", 3);
        pieceValues.put("Rook", 5);
        pieceValues.put("Queen", 9);
        pieceValues.put("King", 0);
    }
    public int getValue() {
        return pieceValues.get(type);
    }
}
class Board {
    Piece[][] grid;
    public Board() {
        grid = new Piece[8][8];
        setupBoard();
    }
    void setupBoard() {
        for (int col = 0; col < 8; col++) {
            grid[1][col] = new Piece("Pawn", "black");
            grid[6][col] = new Piece("Pawn", "white");
        }
        grid[0][0] = new Piece("Rook", "black");
        grid[0][1] = new Piece("Knight", "black");
        grid[0][2] = new Piece("Bishop", "black");
        grid[0][3] = new Piece("Queen", "black");
        grid[0][4] = new Piece("King", "black");
        grid[0][5] = new Piece("Bishop", "black");
        grid[0][6] = new Piece("Knight", "black");
        grid[0][7] = new Piece("Rook", "black");
        grid[7][0] = new Piece("Rook", "white");
        grid[7][1] = new Piece("Knight", "white");
        grid[7][2] = new Piece("Bishop", "white");
        grid[7][3] = new Piece("Queen", "white");
        grid[7][4] = new Piece("King", "white");
        grid[7][5] = new Piece("Bishop", "white");
        grid[7][6] = new Piece("Knight", "white");
        grid[7][7] = new Piece("Rook", "white");
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col] = null;
            }
        }
    }
    public Piece getPiece(int row, int col) {
        return grid[row][col];
    }
    public void setPiece(int row, int col, Piece piece) {
        grid[row][col] = piece;
    }
    public boolean isInsideBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    public void printBoard() {
        System.out.println("\n   a  b  c  d  e  f  g  h");
        System.out.println("  ------------------------");
        printRow(0);
        System.out.println("  ------------------------");
        System.out.println("   a  b  c  d  e  f  g  h\n");
    }

    void printRow(int row) {
        if (row >= 8) {
            return;
        }
        System.out.print((8 - row) + " |");
        for (int col = 0; col < 8; col++) {
            Piece p = grid[row][col];
            if (p == null) {
                System.out.print(" . ");
            } else {
                System.out.print(" " + p.getSymbol() + " ");
            }
        }
        System.out.println("| " + (8 - row));
        printRow(row + 1);
    }
}
class MoveGenerator {
    Board board;
    Game game;
    public MoveGenerator(Board board, Game game) {
        this.board = board;
        this.game = game;
    }
    public ArrayList<int[]> getLegalMoves(int row, int col) {
        ArrayList<int[]> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);
        if (piece == null) {
            return moves;
        }
        switch (piece.getType()) {
            case "Pawn":
                moves = getPawnMoves(row, col, piece);
                break;
            case "Rook":
                moves = getRookMoves(row, col, piece);
                break;
            case "Knight":
                moves = getKnightMoves(row, col, piece);
                break;
            case "Bishop":
                moves = getBishopMoves(row, col, piece);
                break;
            case "Queen":
                moves.addAll(getRookMoves(row, col, piece));
                moves.addAll(getBishopMoves(row, col, piece));
                break;
            case "King":
                moves = getKingMoves(row, col, piece);
                break;
        }
        moves = filterMovesThatExposeKing(row, col, moves, piece);
        return moves;
    }
    ArrayList<int[]> getPawnMoves(int row, int col, Piece piece) {
        ArrayList<int[]> moves = new ArrayList<>();
        int direction = piece.getColor().equals("white") ? -1 : 1;
        int startRow = piece.getColor().equals("white") ? 6 : 1;
        int oneStep = row + direction;
        if (board.isInsideBoard(oneStep, col) && board.getPiece(oneStep, col) == null) {
            moves.add(new int[]{oneStep, col});
            int twoStep = row + (2 * direction);
            if (row == startRow && board.getPiece(twoStep, col) == null) {
                moves.add(new int[]{twoStep, col});
            }
        }
        int[] cols = {col - 1, col + 1};
        for (int c : cols) {
            if (board.isInsideBoard(oneStep, c)) {
                Piece target = board.getPiece(oneStep, c);
                if (target != null && !target.getColor().equals(piece.getColor())) {
                    moves.add(new int[]{oneStep, c});
                }
            }
        }
        addEnPassantMoves(row, col, piece, direction, moves);
        return moves;
    }
    void addEnPassantMoves(int row, int col, Piece piece, int direction, ArrayList<int[]> moves) {
        int[] lastMove = game.getLastPawnTwoStepMove();
        if (lastMove == null) {
            return;
        }
        int lastRow = lastMove[0];
        int lastCol = lastMove[1];
        boolean sameRow = (lastRow == row);
        boolean adjacentCol = (Math.abs(lastCol - col) == 1);
        if (sameRow && adjacentCol) {
            int targetRow = row + direction;
            moves.add(new int[]{targetRow, lastCol});
        }
    }
    ArrayList<int[]> getRookMoves(int row, int col, Piece piece) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            addSlidingMoves(row, col, piece, dir[0], dir[1], moves);
        }
        return moves;
    }
    ArrayList<int[]> getBishopMoves(int row, int col, Piece piece) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] dir : directions) {
            addSlidingMoves(row, col, piece, dir[0], dir[1], moves);
        }
        return moves;
    }
    void addSlidingMoves(int row, int col, Piece piece, int rowDir, int colDir, ArrayList<int[]> moves) {
        int r = row + rowDir;
        int c = col + colDir;
        if (!board.isInsideBoard(r, c)) {
            return;
        }
        Piece target = board.getPiece(r, c);
        if (target == null) {
            moves.add(new int[]{r, c});
            addSlidingMoves(r, c, piece, rowDir, colDir, moves);
        } else {
            if (!target.getColor().equals(piece.getColor())) {
                moves.add(new int[]{r, c});
            }
        }
    }
    ArrayList<int[]> getKnightMoves(int row, int col, Piece piece) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[][] jumps = {
                {-2, -1}, {-2, 1}, {2, -1}, {2, 1},
                {-1, -2}, {-1, 2}, {1, -2}, {1, 2}
        };
        checkKnightJump(row, col, piece, jumps, 0, moves);
        return moves;
    }

    void checkKnightJump(int row, int col, Piece piece, int[][] jumps, int index, ArrayList<int[]> moves) {
        if (index >= jumps.length) {
            return;
        }
        int r = row + jumps[index][0];
        int c = col + jumps[index][1];
        if (board.isInsideBoard(r, c)) {
            Piece target = board.getPiece(r, c);
            if (target == null || !target.getColor().equals(piece.getColor())) {
                moves.add(new int[]{r, c});
            }
        }
        checkKnightJump(row, col, piece, jumps, index + 1, moves);
    }
    ArrayList<int[]> getKingMoves(int row, int col, Piece piece) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            if (board.isInsideBoard(r, c)) {
                Piece target = board.getPiece(r, c);
                if (target == null || !target.getColor().equals(piece.getColor())) {
                    moves.add(new int[]{r, c});
                }
            }
        }
        addCastlingMoves(row, col, piece, moves);
        return moves;
    }
    void addCastlingMoves(int row, int col, Piece piece, ArrayList<int[]> moves) {
        if (piece.hasMoved()) {
            return;
        }
        if (isKingInCheck(piece.getColor())) {
            return;
        }
        String color = piece.getColor();
        Piece kingsideRook = board.getPiece(row, 7);
        if (kingsideRook != null && kingsideRook.getType().equals("Rook")
                && !kingsideRook.hasMoved() && kingsideRook.getColor().equals(color)) {
            if (board.getPiece(row, 5) == null && board.getPiece(row, 6) == null) {
                if (!isSquareAttacked(row, 5, color) && !isSquareAttacked(row, 6, color)) {
                    moves.add(new int[]{row, 6});
                }
            }
        }
        Piece queensideRook = board.getPiece(row, 0);
        if (queensideRook != null && queensideRook.getType().equals("Rook")
                && !queensideRook.hasMoved() && queensideRook.getColor().equals(color)) {
            if (board.getPiece(row, 1) == null && board.getPiece(row, 2) == null && board.getPiece(row, 3) == null) {
                if (!isSquareAttacked(row, 2, color) && !isSquareAttacked(row, 3, color)) {
                    moves.add(new int[]{row, 2});
                }
            }
        }
    }
    public boolean isKingInCheck(String color) {
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getType().equals("King") && p.getColor().equals(color)) {
                    kingRow = r;
                    kingCol = c;
                }
            }
        }
        if (kingRow == -1) {
            return false;
        }
        return isSquareAttacked(kingRow, kingCol, color);
    }
    boolean isSquareAttacked(int row, int col, String myColor) {
        String opponentColor = myColor.equals("white") ? "black" : "white";
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getColor().equals(opponentColor)) {
                    ArrayList<int[]> rawMoves = getRawMoves(r, c, p);
                    for (int[] m : rawMoves) {
                        if (m[0] == row && m[1] == col) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    ArrayList<int[]> getRawMoves(int row, int col, Piece piece) {
        switch (piece.getType()) {
            case "Pawn":
                return getPawnAttackSquares(row, col, piece);
            case "Rook":
                return getRookMoves(row, col, piece);
            case "Knight":
                return getKnightMoves(row, col, piece);
            case "Bishop":
                return getBishopMoves(row, col, piece);
            case "Queen": {
                ArrayList<int[]> m = new ArrayList<>();
                m.addAll(getRookMoves(row, col, piece));
                m.addAll(getBishopMoves(row, col, piece));
                return m;
            }
            case "King": {
                ArrayList<int[]> m = new ArrayList<>();
                int[][] directions = {
                        {-1, -1}, {-1, 0}, {-1, 1},
                        {0, -1},           {0, 1},
                        {1, -1},  {1, 0},  {1, 1}
                };
                for (int[] dir : directions) {
                    int r = row + dir[0];
                    int c = col + dir[1];
                    if (board.isInsideBoard(r, c)) {
                        m.add(new int[]{r, c});
                    }
                }
                return m;
            }
            default:
                return new ArrayList<>();
        }
    }
    ArrayList<int[]> getPawnAttackSquares(int row, int col, Piece piece) {
        ArrayList<int[]> moves = new ArrayList<>();
        int direction = piece.getColor().equals("white") ? -1 : 1;
        int[] cols = {col - 1, col + 1};
        for (int c : cols) {
            if (board.isInsideBoard(row + direction, c)) {
                moves.add(new int[]{row + direction, c});
            }
        }
        return moves;
    }
    ArrayList<int[]> filterMovesThatExposeKing(int row, int col, ArrayList<int[]> moves, Piece piece) {
        ArrayList<int[]> legalMoves = new ArrayList<>();
        for (int[] move : moves) {
            Piece captured = board.getPiece(move[0], move[1]);
            Piece original = board.getPiece(row, col);
            board.setPiece(move[0], move[1], piece);
            board.setPiece(row, col, null);
            boolean stillInCheck = isKingInCheck(piece.getColor());
            board.setPiece(row, col, original);
            board.setPiece(move[0], move[1], captured);
            if (!stillInCheck) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }
    public boolean hasAnyLegalMove(String color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getColor().equals(color)) {
                    ArrayList<int[]> moves = getLegalMoves(r, c);
                    if (!moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
class Game {
    Board board;
    MoveGenerator moveGen;
    String currentTurn;
    Stack<MoveRecord> history;
    int[] lastPawnTwoStepMove;
    int whiteScore = 0;
    int blackScore = 0;
    public Game() {
        board = new Board();
        moveGen = new MoveGenerator(board, this);
        currentTurn = "white";
        history = new Stack<>();
        lastPawnTwoStepMove = null;
    }
    public Board getBoard() {
        return board;
    }
    public MoveGenerator getMoveGenerator() {
        return moveGen;
    }
    public String getCurrentTurn() {
        return currentTurn;
    }
    public int[] getLastPawnTwoStepMove() {
        return lastPawnTwoStepMove;
    }
    static class MoveRecord {
        int fromRow, fromCol, toRow, toCol;
        Piece movedPiece;
        Piece capturedPiece;
        int[] previousLastPawnTwoStep;
        boolean wasCastling;
        boolean wasEnPassant;
        boolean wasPromotion;
        boolean movedPieceFirstMove;
        int rookFromRow, rookFromCol, rookToRow, rookToCol;
        Piece rookPiece;
    }
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) {
            System.out.println("There is no piece here!");
            return false;
        }
        if (!piece.getColor().equals(currentTurn)) {
            System.out.println("This is not your piece! It is currently " + currentTurn + "'s turn.");
            return false;
        }
        var legalMoves = moveGen.getLegalMoves(fromRow, fromCol);
        boolean isValid = false;
        for (int[] m : legalMoves) {
            if (m[0] == toRow && m[1] == toCol) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            System.out.println("This move is not legal!");
            return false;
        }
        MoveRecord record = new MoveRecord();
        record.fromRow = fromRow;
        record.fromCol = fromCol;
        record.toRow = toRow;
        record.toCol = toCol;
        record.movedPiece = piece;
        record.capturedPiece = board.getPiece(toRow, toCol);
        record.previousLastPawnTwoStep = lastPawnTwoStepMove;
        record.movedPieceFirstMove = !piece.hasMoved();
        if (record.capturedPiece != null) {
            int points = record.capturedPiece.getValue();
            if (piece.getColor().equals("white")) {
                whiteScore += points;
            } else {
                blackScore += points;
            }
        }
        boolean isCastling = piece.getType().equals("King") && Math.abs(toCol - fromCol) == 2;
        record.wasCastling = isCastling;
        boolean isEnPassant = false;
        if (piece.getType().equals("Pawn") && fromCol != toCol && board.getPiece(toRow, toCol) == null) {
            isEnPassant = true;
        }
        record.wasEnPassant = isEnPassant;
        boolean isPromotion = false;
        if (piece.getType().equals("Pawn")) {
            if ((piece.getColor().equals("white") && toRow == 0) ||
                    (piece.getColor().equals("black") && toRow == 7)) {
                isPromotion = true;
            }
        }
        record.wasPromotion = isPromotion;
        board.setPiece(toRow, toCol, piece);
        board.setPiece(fromRow, fromCol, null);
        piece.setMoved();
        if (isEnPassant) {
            int capturedPawnRow = fromRow;
            int capturedPawnCol = toCol;
            record.capturedPiece = board.getPiece(capturedPawnRow, capturedPawnCol);
            board.setPiece(capturedPawnRow, capturedPawnCol, null);
        }
        if (isCastling) {
            handleCastlingRookMove(fromRow, toCol, record);
        }
        if (isPromotion) {
            Piece promoted = new Piece("Queen", piece.getColor());
            promoted.setMoved();
            board.setPiece(toRow, toCol, promoted);
        }
        if (piece.getType().equals("Pawn") && Math.abs(toRow - fromRow) == 2) {
            lastPawnTwoStepMove = new int[]{toRow, toCol};
        } else {
            lastPawnTwoStepMove = null;
        }
        history.push(record);
        switchTurn();
        return true;
    }
    void handleCastlingRookMove(int row, int kingToCol, MoveRecord record) {
        if (kingToCol == 6) {
            record.rookFromCol = 7;
            record.rookToCol = 5;
        } else {
            record.rookFromCol = 0;
            record.rookToCol = 3;
        }
        record.rookFromRow = row;
        record.rookToRow = row;
        Piece rook = board.getPiece(row, record.rookFromCol);
        record.rookPiece = rook;
        board.setPiece(record.rookToRow, record.rookToCol, rook);
        board.setPiece(record.rookFromRow, record.rookFromCol, null);
        if (rook != null) {
            rook.setMoved();
        }
    }
    public boolean undoMove() {
        if (history.isEmpty()) {
            System.out.println("There is no move to undo!");
            return false;
        }
        MoveRecord record = history.pop();
        if (record.capturedPiece != null) {
            int points = record.capturedPiece.getValue();
            if (record.movedPiece.getColor().equals("white")) {
                whiteScore -= points;
            } else {
                blackScore -= points;
            }
        }
        Piece piece = record.movedPiece;
        if (record.wasPromotion) {
            board.setPiece(record.fromRow, record.fromCol, piece);
        } else {
            board.setPiece(record.fromRow, record.fromCol, piece);
        }
        if (record.wasEnPassant) {
            board.setPiece(record.toRow, record.toCol, null);
            board.setPiece(record.fromRow, record.toCol, record.capturedPiece);
        } else {
            board.setPiece(record.toRow, record.toCol, record.capturedPiece);
        }
        if (record.movedPieceFirstMove) {
            resetMovedFlag(piece);
        }
        if (record.wasCastling) {
            board.setPiece(record.rookFromRow, record.rookFromCol, record.rookPiece);
            board.setPiece(record.rookToRow, record.rookToCol, null);
            if (record.movedPieceFirstMove) {
                resetMovedFlag(record.rookPiece);
            }
        }
        lastPawnTwoStepMove = record.previousLastPawnTwoStep;
        switchTurn();
        return true;
    }
    void resetMovedFlag(Piece piece) {
        if (piece == null) return;
        piece.resetMoved();
    }
    void switchTurn() {
        currentTurn = currentTurn.equals("white") ? "black" : "white";
    }
    public String getGameStatus() {
        boolean inCheck = moveGen.isKingInCheck(currentTurn);
        boolean hasMoves = moveGen.hasAnyLegalMove(currentTurn);
        if (inCheck && !hasMoves) {
            return "CHECKMATE - " + (currentTurn.equals("white") ? "Black" : "White") + " jeet gaya!";
        } else if (!inCheck && !hasMoves) {
            return "STALEMATE - The match is a draw!";
        } else if (inCheck) {
            return "CHECK - " + currentTurn + "'s King is in danger!";
        } else {
            return "Normal - It is " + currentTurn + "'s turn";
        }
    }
    public boolean isGameOver() {
        return !moveGen.hasAnyLegalMove(currentTurn);
    }
}
public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        Scanner scanner = new Scanner(System.in);
        System.out.println("===========================================");
        System.out.println("       JAVA CHESS - 2 PLAYER GAME");
        System.out.println("===========================================");
        System.out.println("Move format: e2 e4  (from square, to square)");
        System.out.println("To undo: undo");
        System.out.println("To exit the game: exit");
        System.out.println("===========================================");
        while (true) {
            game.getBoard().printBoard();
            System.out.println("Score -> White: " + game.whiteScore + "   Black: " + game.blackScore);
            String status = game.getGameStatus();
            System.out.println(status);
            if (status.contains("CHECKMATE") || status.contains("STALEMATE")) {
                break;
            }
            System.out.print(game.getCurrentTurn() + "'s turn -> Enter move: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Game ended.");
                break;
            }
            if (input.equalsIgnoreCase("undo")) {
                game.undoMove();
                continue;
            }
            String[] parts = input.split(" ");
            if (parts.length != 2) {
                System.out.println("Invalid format! Correct format: e2 e4");
                continue;
            }
            int[] from = convertNotation(parts[0]);
            int[] to = convertNotation(parts[1]);
            if (from == null || to == null) {
                System.out.println("Invalid square name! Enter a value between a1 and h8.");
                continue;
            }
            boolean success = game.makeMove(from[0], from[1], to[0], to[1]);
            if (!success) {
                System.out.println("Move failed, try again.");
            }
        }
        scanner.close();
    }
    private static int[] convertNotation(String square) {
        if (square.length() != 2) {
            return null;
        }
        char colChar = square.charAt(0);
        char rowChar = square.charAt(1);
        int col = colChar - 'a';
        int row = 8 - (rowChar - '0');
        if (col < 0 || col > 7 || row < 0 || row > 7) {
            return null;
        }
        return new int[]{row, col};
    }
}
