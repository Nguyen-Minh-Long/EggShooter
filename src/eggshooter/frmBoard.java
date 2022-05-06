package eggshooter;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Group Javascript Leader: Nguyen Vuong Khang Hy Tester: Nguyen Minh
 * Long Designer: Nguyen Tan Vu Class IA1401 Game Egg Shooter
 */
public class frmBoard extends javax.swing.JFrame {

    // declare new Random object
    protected static Random random = new Random();
    // declare total shooting ball
    private final int totalBallShoot = Commons.TOTAL_BALL_SHOOT;
    // declare maximum dropping ball
    private final int maxBall = Commons.MAX_BALL;
    // declare properties file store game score
    private final static File FILE = new File("score.txt");
    // declare game mode
    private final int mode;
    // declare game level
    private final int level;
    // declare game mode
    private final String supermode;
    // declare dropping ball speed
    private int speed;
    // declare dropping ball color
    private int numColor;
    // declare time term
    private int term;

    // declare new Gun object 
    private Gun gun;
    // declare new 2-d array Ball object store dropping ball
    private Ball[][] ball;
    // declare new array Ball object store shooting ball
    private Ball[] ballshoot;
    // declare new array Ball object store dropping ball color
    private int[][] ballColor;

    // declare time count
    private int timecnt;
    // declare shoot count
    private int cntShoot;
    // declare current score
    private int score;
    // declare high score
    private int highscore;

    // declare Thread of timing
    private Thread timer = null;
    // declare Thread of shooting ball
    private Thread tballshoot = null;
    // declare Thread of dropping ball
    private Thread tballseq = null;
    // declare Thread of check loose game
    private Thread tcheck = null;
    // declare check suspended thread 
    private boolean isSuspended;

    /**
     * Declare all components in panel pnlGame
     */
    private JPanel pnlGameOver;
    private JPanel pnlGamePause;
    private JPanel pnlScoreBoard;
    private JLabel lblTime;
    private JLabel lblScore;
    private JLabel lblHighScore;
    private JLabel lblPause;
    private JLabel lblReset;
    private JLabel lblLose;
    private JLabel lblNavbar;
    private JLabel lblOverScore;
    private JLabel lblOverHighScore;
    private JLabel lblPauseScore;
    private JLabel lblPauseHighScore;
    private JLabel lblScoreBoard;

    // declare new ChainBall object 
    private final ChainBall chain = new ChainBall(maxBall, 9);
    // declare new frame frmMenu
    private frmMenu menu = new frmMenu();

    private final String user;

    ArrayList<String> lsHead;
    ArrayList<ArrayList<String>> lsRow;

    private DBManagement db;
    private Connection conn;
    private Statement st;
    private ResultSet rs;
    private String sql;

    /**
     * Creates new form GunMain
     *
     * @param home
     * @param level
     * @param mode
     * @param supermode
     * @param user
     */
    public frmBoard(frmMenu home, int level, int mode, String supermode, String user) {
        this.menu = home;
        this.level = level;
        this.mode = mode;
        this.supermode = supermode;
        this.user = user;
        this.setIconImage(new ImageIcon("src/img/EggShooter.png").getImage());
        initComponents();
        this.setLocationRelativeTo(null);       // centering the frame
        this.addKeyListener(new TAdapter());    // add KeyListener
        this.setFocusable(true);
        this.setLayout(null);                   // set layout to null

        initConnectDB();
        initMain();     // initialize main components
        initGun();      // initialize gun
        initBall();     // initialize ball
        initThread();   // initialize thread

    }

    private void initConnectDB() {
        try {
            lsHead = new ArrayList<>();
            lsRow = new ArrayList<>();
            lsHead.add("Rank");
            lsHead.add("User");
            lsHead.add("Score");
            db = new DBManagement();
            conn = db.getConnection();
            st = conn.createStatement();

            sql = "SELECT * FROM `" + supermode + "` WHERE user='" + user + "'";
            System.out.println(sql);
            rs = st.executeQuery(sql);
            if (rs.first()) {
                this.highscore = rs.getInt(2);
                System.out.println(rs.getString(1) + "  " + rs.getInt(2));
            } else {
                sql = "INSERT INTO `" + supermode + "`(`user`, `score`) VALUES ('" + user + "', 0)";
                System.out.println(sql);
                st.executeUpdate(sql);
            }
            sql = "SELECT * FROM `" + supermode + "` ORDER BY score DESC LIMIT 10";
            System.out.println(sql);
            rs = st.executeQuery(sql);
            int index = 1;
            while (rs.next()) {
                ArrayList<String> enity = new ArrayList<>();
                enity.add(String.valueOf(index++));
                enity.add(rs.getString(1));
                enity.add(rs.getString(2));
                lsRow.add(enity);
                System.out.println(rs.getString(1) + "  " + rs.getInt(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize main components
     */
    private void initMain() {
        this.isSuspended = false;
        this.speed = Commons.SPEED[level];
        this.numColor = Commons.NUMBER_COLOR[level];
        this.timecnt = Commons.TIME[mode];
        this.term = Commons.TIME_TERM[mode];

        pnlGame.setSize(338, 710);

        pnlScoreBoard = new JPanel(null);
        pnlScoreBoard.setSize(338, 710);
        pnlScoreBoard.setOpaque(true);
        pnlScoreBoard.setBackground(new Color(0, 0, 0, 150));
        JLabel bgScore = new JLabel("", new ImageIcon("src/img/ScoreBoard.png"), JLabel.CENTER);
        bgScore.setBounds(22, 120, 294, 404);
        JLabel lblClose = new JLabel("", new ImageIcon("src/img/btnClose.png"), JLabel.CENTER);
        lblClose.setBounds(270, 120, 36, 36);
        lblClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                pnlScoreBoard.setVisible(false);
                resumeGame();
            }

        });
        DefaultTableModel dtm = new DefaultTableModel(lsHead.toArray(), 0);
        for (ArrayList<String> arrayList : lsRow) {
            dtm.addRow(arrayList.toArray());
        }
        JTable tb = new JTable(dtm);
        tb.setOpaque(false);
        tb.setRowHeight(30);
        tb.setFont(new Font("", 1, 13));
        tb.setForeground(Color.white);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tb.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer) tb.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((DefaultTableCellRenderer) tb.getDefaultRenderer(Object.class)).setOpaque(false);
        tb.setBounds(39, 170, 260, 330);
        tb.setEnabled(false);

        JLabel ico1st = new JLabel("", new ImageIcon("src/img/1st.png"), JLabel.CENTER);
        ico1st.setBounds(70, 180, 24, 22);
        JLabel ico2nd = new JLabel("", new ImageIcon("src/img/2nd.png"), JLabel.CENTER);
        ico2nd.setBounds(70, 210, 24, 22);
        JLabel ico3rd = new JLabel("", new ImageIcon("src/img/3rd.png"), JLabel.CENTER);
        ico3rd.setBounds(70, 240, 24, 22);
        pnlScoreBoard.add(ico1st);
        pnlScoreBoard.add(ico2nd);
        pnlScoreBoard.add(ico3rd);
        pnlScoreBoard.add(tb);
        pnlScoreBoard.add(lblClose);
        pnlScoreBoard.add(bgScore);
        pnlScoreBoard.setVisible(false);

        pnlGameOver = new JPanel(null);
        pnlGameOver.setSize(338, 710);
        pnlGameOver.setOpaque(true);
        pnlGameOver.setBackground(new Color(0, 0, 0, 150));
        JLabel bgOver = new JLabel("", new ImageIcon("src/img/GameOverBoard.png"), JLabel.CENTER);
        bgOver.setBounds(58, 120, 222, 246);
        lblOverScore = new JLabel("0", JLabel.CENTER);
        lblOverScore.setForeground(Color.white);
        lblOverScore.setBounds(170, 175, 60, 10);
        lblOverHighScore = new JLabel(String.valueOf(this.highscore), JLabel.CENTER);
        lblOverHighScore.setForeground(Color.white);
        lblOverHighScore.setBounds(170, 203, 60, 10);
        JLabel lblMenuOver = new JLabel("", new ImageIcon("src/img/btnMenu.png"), JLabel.CENTER);
        lblMenuOver.setBounds(93, 240, 152, 40);
        // back to main menu
        lblMenuOver.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                lblMenuOver.setIcon(new ImageIcon("src/img/btnMenu1.png"));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                lblMenuOver.setIcon(new ImageIcon("src/img/btnMenu.png"));
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                backMenu();
            }
        });
        JLabel lblRestartOver = new JLabel("", new ImageIcon("src/img/btnRestart.png"), JLabel.CENTER);
        lblRestartOver.setBounds(93, 290, 152, 40);
        // restart the game
        lblRestartOver.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                lblRestartOver.setIcon(new ImageIcon("src/img/btnRestart1.png"));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                lblRestartOver.setIcon(new ImageIcon("src/img/btnRestart.png"));
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                restartPnlGame();
            }
        });

        pnlGameOver.add(lblOverScore);
        pnlGameOver.add(lblOverHighScore);
        pnlGameOver.add(lblMenuOver);
        pnlGameOver.add(lblRestartOver);
        pnlGameOver.add(bgOver);
        pnlGameOver.setVisible(false);

        pnlGamePause = new JPanel(null);
        pnlGamePause.setSize(338, 710);
        pnlGamePause.setOpaque(true);
        pnlGamePause.setBackground(new Color(0, 0, 0, 150));
        JLabel bgPause = new JLabel("", new ImageIcon("src/img/PauseBoard.png"), JLabel.CENTER);
        bgPause.setBounds(63, 120, 212, 292);
        lblPauseScore = new JLabel("0", JLabel.CENTER);
        lblPauseScore.setForeground(Color.white);
        lblPauseScore.setBounds(170, 171, 60, 10);
        lblPauseHighScore = new JLabel(String.valueOf(this.highscore), JLabel.CENTER);
        lblPauseHighScore.setForeground(Color.white);
        lblPauseHighScore.setBounds(170, 198, 60, 10);
        JLabel lblMenu = new JLabel("", new ImageIcon("src/img/btnMenu.png"), JLabel.CENTER);
        lblMenu.setBounds(93, 240, 152, 40);
        // back to main menu
        lblMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                lblMenu.setIcon(new ImageIcon("src/img/btnMenu1.png"));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                lblMenu.setIcon(new ImageIcon("src/img/btnMenu.png"));
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                backMenu();
            }
        });
        JLabel lblRestart = new JLabel("", new ImageIcon("src/img/btnRestart.png"), JLabel.CENTER);
        lblRestart.setBounds(93, 290, 152, 40);
        // restart the game
        lblRestart.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                lblRestart.setIcon(new ImageIcon("src/img/btnRestart1.png"));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                lblRestart.setIcon(new ImageIcon("src/img/btnRestart.png"));
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                restartPnlGame();

            }
        });
        JLabel lblResume = new JLabel("", new ImageIcon("src/img/btnResume.png"), JLabel.CENTER);
        lblResume.setBounds(93, 340, 152, 40);
        // resume the game
        lblResume.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                lblResume.setIcon(new ImageIcon("src/img/btnResume1.png"));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                lblResume.setIcon(new ImageIcon("src/img/btnResume.png"));
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                resumeGame();
            }
        });

        pnlGamePause.add(lblPauseScore);
        pnlGamePause.add(lblPauseHighScore);
        pnlGamePause.add(lblMenu);
        pnlGamePause.add(lblRestart);
        pnlGamePause.add(lblResume);
        pnlGamePause.add(bgPause);

        pnlGamePause.setVisible(false);

        lblTime = new JLabel("00:00:00", JLabel.CENTER);
        lblTime.setForeground(Color.white);
        lblTime.setBounds(120, 10, 100, 10);

        lblScore = new JLabel("0", JLabel.LEFT);
        lblScore.setForeground(Color.white);
        lblScore.setBounds(80, 10, 100, 10);

        lblHighScore = new JLabel(String.valueOf(this.highscore), JLabel.LEFT);
        lblHighScore.setForeground(Color.white);
        lblHighScore.setBounds(230, 10, 100, 10);

        lblPause = new JLabel("", new ImageIcon("src/img/btnPause.png"), JLabel.CENTER);
        lblPause.setBounds(4, 16, 42, 39);
        // pause the game
        lblPause.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPauseScore.setText(lblScore.getText());
                lblPauseHighScore.setText(lblHighScore.getText());
                tballseq.suspend();
                timer.suspend();
                isSuspended = true;
                pnlGamePause.setVisible(true);
            }
        });

        lblScoreBoard = new JLabel("", new ImageIcon("src/img/btnScoreBoard.png"), JLabel.CENTER);
        lblScoreBoard.setBounds(290, 16, 42, 39);
        // pause the game
        lblScoreBoard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tballseq.suspend();
                timer.suspend();
                isSuspended = true;
                pnlScoreBoard.setVisible(true);
            }
        });

        lblReset = new JLabel("", new ImageIcon("src/img/reset.png"), JLabel.CENTER);
        lblReset.setBounds(230, 595, 35, 24);
        // resset shooting ball color
        lblReset.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resetBallShoot();
            }
        });

        lblLose = new JLabel("Loose");
        lblLose.setBounds(0, 530, 338, 10);

        lblNavbar = new JLabel("", new ImageIcon("src/img/navbar.png"), JLabel.CENTER);
        lblNavbar.setBounds(0, 0, 338, 53);

        pnlGame.add(pnlScoreBoard);
        pnlGame.add(pnlGameOver);
        pnlGame.add(pnlGamePause);
        pnlGame.add(lblTime);
        pnlGame.add(lblScore);
        pnlGame.add(lblHighScore);
        pnlGame.add(lblPause);
        pnlGame.add(lblScoreBoard);
        pnlGame.add(lblReset);
        pnlGame.add(lblLose);
        pnlGame.add(lblNavbar);
    }

    /**
     * Initialize new gun
     */
    private void initGun() {
        gun = new Gun();
        gun.setBounds(94, 572, 150, 150);
        pnlGame.add(gun);
    }

    /**
     * Initialize dropping ball and shooting ball
     */
    private void initBall() {
        ballshoot = new Ball[totalBallShoot];
        ballColor = new int[maxBall][9];
        // create array shooting ball
        for (int i = 0; i < totalBallShoot; ++i) {
            String color = randColor(numColor);
            ballshoot[i] = new Ball(getBall(color));
            ballshoot[i].setBounds(151, 629, 36, 36);
            ballshoot[i].setColor(Integer.parseInt(color.substring(9, 10)));
            pnlGame.add(ballshoot[i]);
        }

        ball = new Ball[maxBall][9];
        // create 2-d array dropping ball
        for (int i = 0; i < maxBall; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (i > maxBall * 4 / 5) {
                    ball[i][j] = new Ball();
                    ball[i][j].setColor(0);
                } else {
                    String color = randColor(numColor);
                    ball[i][j] = new Ball(getBall(color));
                    ball[i][j].setColor(Integer.parseInt(color.substring(9, 10)));
                }

                ball[i][j].setVisible(false);
                pnlGame.add(ball[i][j]);
            }
        }
    }

    /**
     * Initialize thread
     */
    private void initThread() {
        cntShoot = 0;
        score = 0;
        // Thread of timing
        timer = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        lblTime.setText(secondtoTime(timecnt));
                        timecnt += term;
                        sleep(1000);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };
        // Thread of dropping ball
        tballseq = new Thread() {
            @Override
            public void run() {
                try {
                    int demo = 0;
                    int k;
                    while (demo < (maxBall * 4 / 5) * 36 + 36) {
                        for (int i = 0; i < maxBall; ++i) {
                            for (int j = 0; j < 9; ++j) {
                                ballColor[i][j] = ball[i][j].getColor();
                            }
                        }
                        for (int x = maxBall - 1; x >= 0; x--) {
                            if (x % 2 == 0) {
                                k = 18;
                            } else {
                                k = 0;
                            }
                            for (int j = 0; j < 9; j++) {
                                ball[x][j].setBounds(j * 36 + k, -(maxBall * 4 / 5 - x) * 36 + demo, 36, 36);
                                if (x > 0 && chain.getCountArea() > 2) {
                                    if (x % 2 == 0) {
                                        if (j == 8 && ball[x - 1][j].getColor() == 0) {
                                            ball[x][j].setColor(0);
                                            ball[x][j].setVisible(false);
                                        }
                                        if (j < 8 && ball[x - 1][j].getColor() == 0 && ball[x - 1][j + 1].getColor() == 0) {
                                            ball[x][j].setColor(0);
                                            ball[x][j].setVisible(false);
                                        }
                                    }
                                    if (x % 2 == 1) {
                                        if (j == 0 && ball[x - 1][j].getColor() == 0) {
                                            ball[x][j].setColor(0);
                                            ball[x][j].setVisible(false);
                                        }
                                        if (j > 0 && ball[x - 1][j].getColor() == 0 && ball[x - 1][j - 1].getColor() == 0) {
                                            ball[x][j].setColor(0);
                                            ball[x][j].setVisible(false);
                                        }
                                    }
                                    
                                }
                                
                                if (ball[x][j].getColor() == 0) {
                                    ball[x][j].setVisible(false);
                                } else {
                                    ball[x][j].setVisible(true);
                                }
                                if (ball[x][j].isVisible() && intersects(ball[x][j], lblLose)) {
                                    gameOver();
                                }
                            }
                        }
                        sleep(150);

                        demo += speed;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        // Thread of check loose game
        tcheck = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (timecnt <= 0 && supermode.equals("timerush")) {
                            gameOver();
                        }
                        sleep(1000);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };
        // start 3 thread
        timer.start();
        tballseq.start();
        tcheck.start();
        // add background to panel pnlGame
        JLabel lblBackground = new JLabel("", new ImageIcon("src/img/MainGame.png"), JLabel.CENTER);
        lblBackground.setBounds(0, 0, 338, 710);
        pnlGame.add(lblBackground);
    }

    /**
     * Convert second to formatted time
     *
     * @param second second
     * @return formatted time
     */
    private String secondtoTime(int second) {
        int h = second / 3600;
        int m = (second / 60) % 60;
        int s = second % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Get new BufferedImage of Ball object
     *
     * @param path
     * @return
     */
    private BufferedImage getBall(String path) {
        try {
            return ImageIO.read(getClass().getResourceAsStream(path));
        } catch (IOException ex) {
            Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Keyboard event shooting ball
     *
     * @param ke
     */
    public void keyShootPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_SPACE && !gun.isIsShooted()) {
            gun.setIsShooted(true);
            double angle = gun.getCurrentAngle();

            tballshoot = new Thread() {

                @Override
                public void run() {
                    try {
                        int x = 0;
                        int tmp = 0;
                        while (x < 1000) {
                            sleep(1);
                            double i = x - Math.tan(Math.toRadians(angle));
                            double j = i * Math.tan(Math.toRadians(angle));
                            for (int k = 0; k < 5; ++k) {
                                if (156 + (int) j >= pnlGame.getWidth() - 36) {
                                    j = (pnlGame.getWidth() - 36 - j);
                                    tmp = -1;
                                } else if (156 + (int) j <= 0) {
                                    j = -(j + pnlGame.getWidth() + 36);
                                    tmp = 1;
                                }

                                ballshoot[cntShoot].setBounds(151 + (int) j, 629 - (int) x, 36, 36);
                                checkCollision(ballshoot[cntShoot], tmp);
                            }
                            x += 7;
                        }

                    } catch (InterruptedException ex) {
                        Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        cntShoot++;
                        gun.setIsShooted(false);
                    }
                }

            };
            tballshoot.start();
        }
        if (ke.getKeyCode() == KeyEvent.VK_R) {
            resetBallShoot();
        }
    }

    /**
     * Check collision between shooting ball and dropping ball
     *
     * @param ballshoot object Ball shooting ball
     * @param t thread of shooting ball
     */
    private void checkCollision(Ball ballshoot, int angle) {
        try {
            for (int i = maxBall-1; i >= 0; --i) {
                for (int j = 0; j < 9; ++j) {
                    if (ball[i][j].isVisible() && intersects(ballshoot, ball[i][j]) && ballshoot.isVisible()) {
                        if (j == 0 && !ball[i + 1][j].isVisible()) {
                            System.out.println("bang1");
                            ball[i + 1][j].setImage(ballshoot.getImage());
                            ball[i + 1][j].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i + 1][j].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i + 1, j);
                            break;
                        }
                        if (j == 8 && !ball[i + 1][j].isVisible()) {
                            System.out.println("bang2");
                            ball[i + 1][j].setImage(ballshoot.getImage());
                            ball[i + 1][j].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i + 1][j].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i + 1, j);
                            break;
                        }
                        if (i%2==0 && !ball[i + 1][j+1].isVisible()) {
                            System.out.println("bang3");
                            ball[i + 1][j+1].setImage(ballshoot.getImage());
                            ball[i + 1][j+1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i + 1][j+1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i + 1, j+1);
                            break;
                        }
                        if (i%2==1 && !ball[i + 1][j-1].isVisible()) {
                            System.out.println("bang4");
                            ball[i + 1][j-1].setImage(ballshoot.getImage());
                            ball[i + 1][j-1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i + 1][j-1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i + 1, j-1);
                            break;
                        }
                        if (!ball[i + 1][j-1].isVisible() && ball[i + 1][j].isVisible()) {
                            System.out.println("bang5");
                            ball[i + 1][j-1].setImage(ballshoot.getImage());
                            ball[i + 1][j-1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i + 1][j-1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i + 1, j-1);
                            break;
                        }

                        if (!ball[i + 1][j].isVisible()) {
                            System.out.println("bang6");
                            ball[i + 1][j].setImage(ballshoot.getImage());
                            ball[i + 1][j].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i + 1][j].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i + 1, j);
                            break;
                        }
                        if (j > 0 && !ball[i][j - 1].isVisible() && angle > 0) {
                            System.out.println("bang7");
                            ball[i][j - 1].setImage(ballshoot.getImage());
                            ball[i][j - 1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i][j - 1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i, j - 1);
                            break;
                        }
                        if (!ball[i][j + 1].isVisible() && angle > 0) {
                            System.out.println("bang8");
                            ball[i][j + 1].setImage(ballshoot.getImage());
                            ball[i][j + 1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i][j + 1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i, j + 1);
                            break;
                        }
                        if (!ball[i][j + 1].isVisible() && angle < 0) {
                            System.out.println("bang9");
                            ball[i][j + 1].setImage(ballshoot.getImage());
                            ball[i][j + 1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i][j + 1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i, j + 1);
                            break;
                        }
                        if (j > 0 && !ball[i][j - 1].isVisible() && angle < 0) {
                            System.out.println("bang10");
                            ball[i][j - 1].setImage(ballshoot.getImage());
                            ball[i][j - 1].setVisible(true);
                            ballshoot.setVisible(false);
                            ball[i][j - 1].setColor(ballshoot.getColor());
                            removeChain(ballshoot, i, j - 1);
                            break;
                        }

                    }
                }
            }
        } catch (Exception e) {
            ballshoot.setVisible(false);
        }

    }

    /**
     * Remove the chaining ball
     *
     * @param b object Ball
     * @param x row index
     * @param y column index
     */
    private void removeChain(Ball b, int x, int y) {
        ballColor[x][y] = b.getColor();
        // marked ball
        int[][] tmp = chain.countIslands(ballColor, b.getColor(), x, y);
        // if more than 2 chaining ball, calculate score
        if (chain.getCountArea() > 2) {
            score += 10 * chain.getCountArea();
            lblScore.setText(String.valueOf(score));
            if (Integer.parseInt(lblHighScore.getText()) <= Integer.parseInt(lblScore.getText())) {
                lblHighScore.setText(String.valueOf(score));
                this.highscore = score;
            }
        }
        for (int i = 0; i < maxBall; ++i) {
            for (int j = 0; j < 9; ++j) {
                // if more than 2 chaining ball and ball is marked , remove them
                if (tmp[i][j] == 1 && chain.getCountArea() > 2) {
                    ball[i][j].setColor(0);
                }
            }
        }

    }

    /**
     * Check intersect between 2 label
     *
     * @param testa first label
     * @param testb second label
     * @return true if intersect else false
     */
    private boolean intersects(JLabel testa, JLabel testb) {
        Area areaA = new Area(testa.getBounds());
        Area areaB = new Area(testb.getBounds());

        return areaA.intersects(areaB.getBounds2D());
    }

    /**
     * Get ball color
     *
     * @param n range random ball color
     * @return path of ball color
     */
    private String randColor(int n) {
        int x = random.nextInt(n) + 1;
        String path = "/img/ball" + x + ".png";
        return path;
    }

    /**
     * Back to menu
     */
    private void backMenu() {
        dispose();
        menu.getBtnArcade().setEnabled(true);
        menu.getBtnTimeRush().setEnabled(true);
        menu.setVisible(true);
    }

    /**
     * Restart the game
     */
    private void restartPnlGame() {
        pnlGame.setVisible(false);
        initConnectDB();
        initComponents();
        initMain();
        initGun();
        initBall();
        initThread();
    }

    /**
     * Resume the game
     */
    private void resumeGame() {
        pnlGamePause.setVisible(false);
        tballseq.resume();
        timer.resume();
        isSuspended = false;
    }

    /**
     * Store score when game over
     */
    private void gameOver() {
        try {
            sql = "UPDATE `" + supermode + "` SET `score`='" + highscore + "' WHERE user='" + user + "'";
            st = conn.prepareStatement(sql);
            st.executeUpdate(sql);

            pnlGameOver.setVisible(true);
            lblOverScore.setText(lblScore.getText());
            lblOverHighScore.setText(lblHighScore.getText());
            // Stop 3 thread
            timer.stop();
            tballseq.stop();
            tcheck.stop();
        } catch (SQLException ex) {
            Logger.getLogger(frmBoard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reset shooting ball color
     *
     * @throws NumberFormatException
     */
    private void resetBallShoot() throws NumberFormatException {
        String color = randColor(numColor);
        ballshoot[cntShoot].setImage(getBall(color));
        ballshoot[cntShoot].setColor(Integer.parseInt(color.substring(9, 10)));
    }

    /**
     * Adapter of keyboard event
     */
    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent ke) {
            if (tballseq.isAlive() && !isSuspended) {
                gun.keyPressed(ke);
                keyShootPressed(ke);
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlGame = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Egg Shooter");
        setResizable(false);
        setSize(new java.awt.Dimension(338, 710));

        pnlGame.setLayout(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlGame, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlGame, javax.swing.GroupLayout.PREFERRED_SIZE, 710, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmBoard(new frmMenu(), 0, 1, "timerush", "hy").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlGame;
    // End of variables declaration//GEN-END:variables
}
