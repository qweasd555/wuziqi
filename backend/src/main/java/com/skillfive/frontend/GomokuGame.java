package com.skillfive.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 五子棋游戏主界面
 */
public class GomokuGame extends JFrame {
    
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int BOARD_WIDTH = BOARD_SIZE * CELL_SIZE;
    private static final int BOARD_HEIGHT = BOARD_SIZE * CELL_SIZE;
    
    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private String gameId;
    private Long userId;
    private Long opponentId;
    private boolean isPlayer1;
    
    private final String baseUrl = "http://localhost:8080/api";  // 连接到后端服务端口
    
    // 技能按钮
    private JButton resetSkillBtn;
    private JButton extraTurnSkillBtn;
    private JButton removePieceSkillBtn;
    
    // 用户界面组件
    private JLabel userLabel;
    
    public GomokuGame() {
        setTitle("五子棋游戏 - SkillFive");
        setSize(BOARD_WIDTH + 200, BOARD_HEIGHT + 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeBoard();
        createUI();
        
        // 显示界面，等待用户手动登录
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
    
    private void initializeBoard() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = '.';
            }
        }
        currentPlayer = 'X';
        gameOver = false;
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // 棋盘面板
        BoardPanel boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);
        
        // 控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(8, 1, 5, 5));  // 增加行数
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 用户登录区域
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 1, 3, 3));
        loginPanel.setBorder(BorderFactory.createTitledBorder("用户登录"));
        
        JButton loginBtn = new JButton("登录/注册");
        JLabel userLabel = new JLabel("未登录");
        userLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        loginBtn.addActionListener(e -> showLoginDialog());
        
        loginPanel.add(loginBtn);
        loginPanel.add(userLabel);
        
        // 保存用户标签引用，用于更新
        this.userLabel = userLabel;
        
        // 游戏控制按钮
        JButton newGameBtn = new JButton("新游戏");
        JButton joinGameBtn = new JButton("加入游戏");
        JButton refreshBtn = new JButton("刷新棋盘");
        
        // 技能按钮
        resetSkillBtn = new JButton("重置棋盘 (技能1)");
        extraTurnSkillBtn = new JButton("额外回合 (技能2)");
        removePieceSkillBtn = new JButton("移除棋子 (技能3)");
        
        // 设置技能按钮初始状态
        updateSkillButtons(false);
        
        // 添加按钮事件
        newGameBtn.addActionListener(e -> createNewGame());
        joinGameBtn.addActionListener(e -> joinExistingGame());
        refreshBtn.addActionListener(e -> refreshBoard());
        resetSkillBtn.addActionListener(e -> useSkill(1L));
        extraTurnSkillBtn.addActionListener(e -> useSkill(2L));
        removePieceSkillBtn.addActionListener(e -> useSkill(3L));
        
        controlPanel.add(loginPanel);
        controlPanel.add(new JSeparator());
        controlPanel.add(newGameBtn);
        controlPanel.add(joinGameBtn);
        controlPanel.add(refreshBtn);
        controlPanel.add(new JSeparator());
        controlPanel.add(resetSkillBtn);
        controlPanel.add(extraTurnSkillBtn);
        controlPanel.add(removePieceSkillBtn);
        
        add(controlPanel, BorderLayout.EAST);
        
        // 状态栏
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void updateSkillButtons(boolean enabled) {
        resetSkillBtn.setEnabled(enabled);
        extraTurnSkillBtn.setEnabled(enabled);
        removePieceSkillBtn.setEnabled(enabled);
    }
    
    private void showLoginDialog() {
        // 创建登录对话框
        JDialog loginDialog = new JDialog(this, "用户登录", true);
        loginDialog.setSize(300, 200);
        loginDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("请输入登录信息");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JTextField nicknameField = new JTextField("玩家" + System.currentTimeMillis() % 1000);
        nicknameField.setBorder(BorderFactory.createTitledBorder("昵称"));
        
        JPanel buttonPanel = new JPanel();
        JButton loginBtn = new JButton("登录");
        JButton cancelBtn = new JButton("取消");
        
        loginBtn.addActionListener(e -> {
            String nickname = nicknameField.getText().trim();
            if (nickname.isEmpty()) {
                JOptionPane.showMessageDialog(loginDialog, "请输入昵称！");
                return;
            }
            loginDialog.dispose();
            loginUser(nickname);
        });
        
        cancelBtn.addActionListener(e -> loginDialog.dispose());
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);
        
        panel.add(titleLabel);
        panel.add(nicknameField);
        panel.add(buttonPanel);
        
        loginDialog.add(panel);
        loginDialog.setVisible(true);
    }
    
    private void loginUser(String nickname) {
        try {
            // 使用微信登录接口，自动生成openId
            String openId = "user_" + System.currentTimeMillis();
            
            URL url = new URL(baseUrl + "/wechat/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String jsonInputString = String.format("{\"openId\":\"%s\",\"nickname\":\"%s\"}", openId, nickname);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // 解析响应获取用户ID
                String responseStr = response.toString();
                if (responseStr.contains("\"id\":")) {
                    int idIndex = responseStr.indexOf("\"id\":") + 5;
                    int idEnd = responseStr.indexOf(",", idIndex);
                    if (idEnd == -1) idEnd = responseStr.indexOf("}", idIndex);
                    userId = Long.parseLong(responseStr.substring(idIndex, idEnd).trim());
                    
                    // 更新用户标签
                    if (userLabel != null) {
                        userLabel.setText("用户: " + nickname);
                    }
                    
                    JOptionPane.showMessageDialog(this, "登录成功！欢迎 " + nickname + "，用户ID: " + userId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "登录失败: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "登录错误: " + e.getMessage());
        }
    }
    
    private void createNewGame() {
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "请先登录！");
            return;
        }
        
        try {
            URL url = new URL(baseUrl + "/game/create");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String jsonInputString = String.format("{\"userId\":%d,\"mode\":\"REGULAR\",\"type\":\"ONLINE_PVP\"}", userId);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // 解析游戏ID
                String responseStr = response.toString();
                if (responseStr.contains("\"id\":")) {
                    int idIndex = responseStr.indexOf("\"id\":") + 5;
                    int idEnd = responseStr.indexOf(",", idIndex);
                    if (idEnd == -1) idEnd = responseStr.indexOf("}", idIndex);
                    gameId = responseStr.substring(idIndex, idEnd).trim();
                    isPlayer1 = true;
                    updateSkillButtons(true);
                    JOptionPane.showMessageDialog(this, "游戏创建成功！游戏ID: " + gameId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "创建游戏失败: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "创建游戏错误: " + e.getMessage());
        }
    }
    
    private void joinExistingGame() {
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "请先登录！");
            return;
        }
        
        try {
            // 获取可用游戏列表
            URL url = new URL(baseUrl + "/game/available?mode=REGULAR");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String gamesList = response.toString();
                if (gamesList.contains("\"id\":")) {
                    // 简单获取第一个游戏的ID
                    int idIndex = gamesList.indexOf("\"id\":") + 5;
                    int idEnd = gamesList.indexOf(",", idIndex);
                    if (idEnd == -1) idEnd = gamesList.indexOf("}", idIndex);
                    String availableGameId = gamesList.substring(idIndex, idEnd).trim();
                    
                    // 加入游戏
                    joinGame(availableGameId);
                } else {
                    JOptionPane.showMessageDialog(this, "没有可用的游戏！");
                }
            } else {
                JOptionPane.showMessageDialog(this, "获取游戏列表失败: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加入游戏错误: " + e.getMessage());
        }
    }
    
    private void joinGame(String gameId) {
        try {
            URL url = new URL(baseUrl + "/game/" + gameId + "/join");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String jsonInputString = String.format("{\"userId\":%d}", userId);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                this.gameId = gameId;
                isPlayer1 = false;
                updateSkillButtons(true);
                JOptionPane.showMessageDialog(this, "成功加入游戏！");
                refreshBoard();
            } else {
                JOptionPane.showMessageDialog(this, "加入游戏失败: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加入游戏错误: " + e.getMessage());
        }
    }
    
    private void makeMove(int x, int y) {
        if (gameOver || gameId == null) return;
        
        try {
            int position = x * BOARD_SIZE + y;
            URL url = new URL(baseUrl + "/game/" + gameId + "/move");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String jsonInputString = String.format("{\"userId\":%d,\"position\":%d}", userId, position);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                refreshBoard();
            } else {
                JOptionPane.showMessageDialog(this, "移动失败: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "移动错误: " + e.getMessage());
        }
    }
    
    private void useSkill(Long skillId) {
        if (gameId == null) {
            JOptionPane.showMessageDialog(this, "请先加入游戏！");
            return;
        }
        
        try {
            URL url = new URL(baseUrl + "/game/" + gameId + "/skill");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String jsonInputString = String.format("{\"userId\":%d,\"skillId\":%d}", userId, skillId);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                refreshBoard();
                JOptionPane.showMessageDialog(this, "技能使用成功！");
            } else {
                JOptionPane.showMessageDialog(this, "技能使用失败: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "技能使用错误: " + e.getMessage());
        }
    }
    
    private void refreshBoard() {
        if (gameId == null) return;
        
        try {
            URL url = new URL(baseUrl + "/game/" + gameId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // 解析棋盘状态
                String gameData = response.toString();
                parseBoardState(gameData);
                repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void parseBoardState(String gameData) {
        // 简单解析JSON中的boardState
        if (gameData.contains("\"boardState\":\"")) {
            int start = gameData.indexOf("\"boardState\":\"") + 14;
            int end = gameData.indexOf("\"", start);
            String boardState = gameData.substring(start, end);
            
            // 将一维字符串转换为二维数组
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    int pos = i * BOARD_SIZE + j;
                    if (pos < boardState.length()) {
                        char c = boardState.charAt(pos);
                        board[i][j] = (c == '-' || c == '.') ? '.' : c;
                    }
                }
            }
        }
        
        // 解析当前玩家
        if (gameData.contains("\"currentPlayer\":")) {
            int start = gameData.indexOf("\"currentPlayer\":") + 16;
            int end = gameData.indexOf(",", start);
            if (end == -1) end = gameData.indexOf("}", start);
            int currentPlayerNum = Integer.parseInt(gameData.substring(start, end).trim());
            
            // 判断是否是当前用户的回合
            boolean isMyTurn = (isPlayer1 && currentPlayerNum == 1) || (!isPlayer1 && currentPlayerNum == 2);
            updateSkillButtons(isMyTurn);
        }
    }
    
    /**
     * 棋盘面板
     */
    class BoardPanel extends JPanel {
        public BoardPanel() {
            setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
            setBackground(new Color(255, 220, 150)); // 木色背景
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (gameOver) return;
                    
                    int x = e.getX() / CELL_SIZE;
                    int y = e.getY() / CELL_SIZE;
                    
                    if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                        if (board[x][y] == '.') {
                            makeMove(x, y);
                        }
                    }
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // 绘制棋盘网格
            g.setColor(Color.BLACK);
            for (int i = 0; i <= BOARD_SIZE; i++) {
                g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_HEIGHT);
                g.drawLine(0, i * CELL_SIZE, BOARD_WIDTH, i * CELL_SIZE);
            }
            
            // 绘制棋子
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] != '.') {
                        int x = i * CELL_SIZE + CELL_SIZE / 2;
                        int y = j * CELL_SIZE + CELL_SIZE / 2;
                        int diameter = CELL_SIZE - 10;
                        
                        if (board[i][j] == 'X') {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.WHITE);
                        }
                        
                        g.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
                        g.setColor(Color.BLACK);
                        g.drawOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
                    }
                }
            }
            
            // 绘制当前玩家指示
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String status = gameOver ? "游戏结束" : ("当前玩家: " + currentPlayer);
            g.drawString(status, 10, getHeight() - 10);
        }
    }
    
    public static void main(String[] args) {
        // 设置Windows风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                GomokuGame game = new GomokuGame();
                game.setVisible(true);
                System.out.println("五子棋游戏界面已启动！");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "启动游戏失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}