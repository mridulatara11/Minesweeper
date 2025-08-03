import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.util.prefs.Preferences;

public class MinesweeperGUI extends JFrame {
    private int rows = 9, cols = 9, mines = 10;
    private JButton[][] buttons;
    private char[][] board;
    private boolean[][] revealed;
    private boolean gameOver = false, gameStarted = false;

    // UI & Game state
    private ImageIcon bombIcon, flagIcon;
    private javax.swing.Timer gameTimer;
    private JLabel timeLabel, mineLabel;
    private JButton restartButton;
    private JPanel gamePanel;
    private int elapsedSeconds = 0, flagsPlaced = 0;
    private final int TIME_LIMIT = 300;

    // High score storage
    private Preferences prefs = Preferences.userNodeForPackage(MinesweeperGUI.class);
    private static final String HS_KEY = "Minesweeper_HighScore";

    public MinesweeperGUI() {
        super("Minesweeper");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadIcons();
        setupMenu();
        setupTimer();
        createUI();
        startNewGame();
    }

    private void setupMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu diff = new JMenu("Difficulty");
        JMenuItem easy = new JMenuItem("Easy 9×9,10"),
                  medium = new JMenuItem("Medium 16×16,40"),
                  hard = new JMenuItem("Hard 16×30,99");
        diff.add(easy).addActionListener(e -> { setDifficulty(9,9,10); startNewGame(); });
        diff.add(medium).addActionListener(e -> { setDifficulty(16,16,40); startNewGame(); });
        diff.add(hard).addActionListener(e -> { setDifficulty(16,30,99); startNewGame(); });
        mb.add(diff);

        JMenu stats = new JMenu("Stats");
        JMenuItem showHs = new JMenuItem("Show High Score");
        showHs.addActionListener(e -> {
            int hs = prefs.getInt(HS_KEY, Integer.MAX_VALUE);
            String text = (hs == Integer.MAX_VALUE)
                ? "No high score yet"
                : String.format("Best time: %d seconds", hs);
            JOptionPane.showMessageDialog(this, text, "High Score", JOptionPane.INFORMATION_MESSAGE);
        });
        stats.add(showHs);
        mb.add(stats);

        setJMenuBar(mb);
    }

    private void setDifficulty(int r, int c, int m) {
        rows = r; cols = c; mines = m;
    }

    private void setupTimer() {
        timeLabel = new JLabel("Time: 5:00", SwingConstants.CENTER);
        mineLabel = new JLabel("Mines Left: " + mines, SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mineLabel.setFont(timeLabel.getFont());
        timeLabel.setOpaque(true); mineLabel.setOpaque(true);
        timeLabel.setBackground(Color.LIGHT_GRAY); mineLabel.setBackground(Color.LIGHT_GRAY);
        timeLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        mineLabel.setBorder(timeLabel.getBorder());
        gameTimer = new javax.swing.Timer(1000, e -> {
            elapsedSeconds++;
            int rem = TIME_LIMIT - elapsedSeconds;
            if (rem <= 0) timeOut();
            else {
                timeLabel.setText(String.format("Time: %d:%02d", rem / 60, rem % 60));
                timeLabel.setBackground(rem <= 30 ? Color.RED : rem <= 60 ? Color.ORANGE : Color.LIGHT_GRAY);
                timeLabel.setForeground(rem <= 30 ? Color.WHITE : Color.BLACK);
            }
        });
    }

    private void loadIcons() {
        bombIcon = new ImageIcon(new ImageIcon("icons/bomb.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH));
        flagIcon = new ImageIcon(new ImageIcon("icons/red-flag.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH));
    }

    private void createUI() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        JPanel info = new JPanel(new GridLayout(1,2));
        info.add(timeLabel); info.add(mineLabel);
        top.add(info, BorderLayout.CENTER);

        restartButton = new JButton("New Game");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.addActionListener(e -> startNewGame());
        top.add(restartButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        gamePanel = new JPanel();
        add(gamePanel, BorderLayout.CENTER);
    }

    private void startNewGame() {
        if (gameTimer.isRunning()) gameTimer.stop();
        elapsedSeconds = flagsPlaced = 0;
        gameOver = gameStarted = false;
        timeLabel.setText("Time: 5:00");
        timeLabel.setBackground(Color.LIGHT_GRAY);
        timeLabel.setForeground(Color.BLACK);
        mineLabel.setText("Mines Left: " + mines);

        board = new char[rows][cols];
        revealed = new boolean[rows][cols];
        buttons = new JButton[rows][cols];
        initBoard();

        gamePanel.removeAll();
        gamePanel.setLayout(new GridLayout(rows, cols));
        for (int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                JButton btn = new JButton();
                btn.setFont(new Font("Arial", Font.BOLD, 18));
                int r=i, c=j;
                btn.addMouseListener(new MouseAdapter(){
                    @Override public void mouseClicked(MouseEvent e){
                        if (gameOver) return;
                        if (!gameStarted){
                            gameStarted = true;
                            gameTimer.start();
                        }
                        if (SwingUtilities.isRightMouseButton(e)){
                            if (!revealed[r][c]){
                                if (btn.getIcon()==null){
                                    btn.setIcon(flagIcon); flagsPlaced++;
                                } else {
                                    btn.setIcon(null); flagsPlaced--;
                                }
                                mineLabel.setText("Mines Left: " + (mines - flagsPlaced));
                            }
                        } else revealCell(r, c);
                    }
                });
                buttons[i][j] = btn;
                gamePanel.add(btn);
            }
        }
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    private void initBoard(){
        for(int i=0;i<rows;i++) Arrays.fill(board[i],' ');
        int placed=0;
        Random rng = new Random();
        while(placed<mines){
            int r=rng.nextInt(rows), c=rng.nextInt(cols);
            if(board[r][c] != '*'){ board[r][c] = '*'; placed++; }
        }
        for(int i=0;i<rows;i++) for(int j=0;j<cols;j++){
            if(board[i][j] != '*'){
                int cnt = countMines(i,j);
                board[i][j] = cnt==0? ' ': (char)('0'+cnt);
            }
        }
    }

    private int countMines(int r, int c){
        int cnt=0;
        for(int dr=-1;dr<=1;dr++) for(int dc=-1;dc<=1;dc++){
            int nr=r+dr, nc=c+dc;
            if(nr>=0 && nr<rows && nc>=0 && nc<cols && board[nr][nc]=='*') cnt++;
        }
        return cnt;
    }

    private void revealCell(int r,int c){
        if(r<0||r>=rows||c<0||c>=cols||revealed[r][c]) return;
        revealed[r][c]=true;
        JButton btn = buttons[r][c];
        btn.setEnabled(false); btn.setIcon(null);
        if(board[r][c]=='*'){ btn.setIcon(bombIcon); gameOver(); return; }
        if(board[r][c] != ' ') btn.setText(String.valueOf(board[r][c]));
        else for(int dr=-1;dr<=1;dr++) for(int dc=-1;dc<=1;dc++) if(dr!=0||dc!=0) revealCell(r+dr,c+dc);
        checkWin();
    }

    private void checkWin(){
        int shown=0;
        for(boolean[] row: revealed) for(boolean b: row) if(b) shown++;
        if(shown==rows*cols-mines){
            gameOver = true;
            gameTimer.stop();
            int finalTime = elapsedSeconds;
            int best = prefs.getInt(HS_KEY, Integer.MAX_VALUE);
            if(finalTime < best){
                prefs.putInt(HS_KEY, finalTime);
                best = finalTime;
            }
            JOptionPane.showMessageDialog(this,
                String.format("You win! Time: %d sec\nBest: %d sec", finalTime, best),
                "Victory", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void gameOver(){
        gameOver = true;
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, "Boom! You hit a mine!", "Game Over", JOptionPane.ERROR_MESSAGE);
        revealAllMines();
    }

    private void timeOut(){
        gameOver=true; gameTimer.stop();
        JOptionPane.showMessageDialog(this, "Time's up!", "Time Out", JOptionPane.WARNING_MESSAGE);
        revealAllMines();
    }

    private void revealAllMines(){
        for(int i=0;i<rows;i++) for(int j=0;j<cols;j++)
            if(board[i][j]=='*'){
                buttons[i][j].setIcon(bombIcon);
                buttons[i][j].setEnabled(false);
            }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new MinesweeperGUI().setVisible(true));
    }
}
