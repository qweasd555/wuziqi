package com.skillfive.backend.controller;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.enums.GameMode;
import com.skillfive.backend.enums.GameType;
import com.skillfive.backend.service.AiService;
import com.skillfive.backend.service.GameService;
import com.skillfive.backend.service.GameFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final AiService aiService;
    private final GameFlowService gameFlowService;

    public GameController(GameService gameService, AiService aiService, GameFlowService gameFlowService) {
        this.gameService = gameService;
        this.aiService = aiService;
        this.gameFlowService = gameFlowService;
    }

    /**
     * 创建新游戏
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody Map<String, Object> request) {
        try {
            // 添加空值检查
            if (request == null || request.get("userId") == null || request.get("mode") == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "缺少必要参数");
                return ResponseEntity.badRequest().body(error);
            }
            
            Long userId = Long.valueOf(request.get("userId").toString());
            GameMode mode = GameMode.valueOf(request.get("mode").toString());
            
            // 检查是否指定了游戏类型（人机对战或联机对战）
            GameType type = GameType.ONLINE_PVP; // 默认为联机对战
            if (request.containsKey("type") && request.get("type") != null) {
                type = GameType.valueOf(request.get("type").toString());
            }
            
            Game game = gameService.createGame(userId, mode, type);
            
            // 如果是人机对战，AI自动加入游戏
            if (type == GameType.VS_AI) {
                game = gameService.joinGame(game.getId(), null); // null表示AI玩家
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gameId", game.getId());
            response.put("status", game.getStatus());
            response.put("type", game.getType());
            response.put("message", "游戏创建成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "创建游戏失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 加入游戏
     */
    @PostMapping("/{gameId}/join")
    public ResponseEntity<Game> joinGame(@PathVariable Long gameId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Game game = gameService.joinGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    /**
     * 获取游戏信息
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGameById(@PathVariable Long gameId) {
        try {
            return gameService.findById(gameId)
                    .map(game -> ResponseEntity.ok().body(game))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("获取游戏信息失败 - gameId: " + gameId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 执行移动（支持行列坐标）
     */
    @PostMapping("/{gameId}/move")
    public ResponseEntity<?> makeMove(@PathVariable Long gameId, @RequestBody Map<String, Object> request) {
        try {
            // 添加空值检查
            if (request == null || request.get("userId") == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "缺少必要参数");
                return ResponseEntity.badRequest().body(error);
            }
            
            Long userId = Long.valueOf(request.get("userId").toString());
            
            // 支持行列坐标或位置索引
            Game game;
            if (request.get("row") != null && request.get("col") != null) {
                // 使用行列坐标
                int row = ((Number) request.get("row")).intValue();
                int col = ((Number) request.get("col")).intValue();
                game = gameFlowService.makeMove(gameId, row, col, userId);
            } else if (request.get("position") != null) {
                // 使用位置索引（兼容旧版本）
                Integer position = Integer.valueOf(request.get("position").toString());
                game = gameService.makeMove(gameId, userId, position);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "缺少移动坐标");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gameId", game.getId());
            response.put("status", game.getStatus());
            response.put("currentPlayer", game.getCurrentPlayer());
            response.put("boardState", game.getBoardState());
            response.put("winner", game.getWinner());
            response.put("isAiTurn", game.getType() == GameType.VS_AI && game.getCurrentPlayer() == 2);
            response.put("message", "移动成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "移动失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 使用技能
     */
    @PostMapping("/{gameId}/skill")
    public ResponseEntity<Game> useSkill(@PathVariable Long gameId, @RequestBody Map<String, Object> request) {
        // 添加空值检查
        if (request == null || request.get("userId") == null || request.get("skillId") == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Long userId = Long.valueOf(request.get("userId").toString());
        Long skillId = Long.valueOf(request.get("skillId").toString());
        Game game = gameService.useSkill(gameId, userId, skillId);
        return ResponseEntity.ok(game);
    }

    /**
     * 放弃游戏
     */
    @PostMapping("/{gameId}/giveup")
    public ResponseEntity<Game> giveUp(@PathVariable Long gameId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Game game = gameService.giveUpGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    /**
     * 获取AI移动（用于人机对战）
     */
    @PostMapping("/ai-move")
    public ResponseEntity<Game> makeAiMove(@RequestBody Map<String, Object> request) {
        // 添加空值检查
        if (request == null || request.get("gameId") == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Long gameId = Long.valueOf(request.get("gameId").toString());
        
        // 检查是否需要AI移动
        if (!aiService.shouldAiMove(gameId)) {
            return ResponseEntity.badRequest().body(null);
        }
        
        // 执行AI移动
        Game game = aiService.makeAiMove(gameId);
        return ResponseEntity.ok(game);
    }
    
    /**
     * 设置AI难度
     */
    @PostMapping("/ai-difficulty")
    public ResponseEntity<String> setAiDifficulty(@RequestBody Map<String, Object> request) {
        // 添加空值检查
        if (request == null || request.get("difficulty") == null) {
            return ResponseEntity.badRequest().body("缺少difficulty参数");
        }
        
        int difficulty = Integer.parseInt(request.get("difficulty").toString());
        aiService.setDifficulty(difficulty);
        return ResponseEntity.ok("AI难度已设置为: " + difficulty);
    }

    /**
     * 获取用户活跃游戏
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Game>> getUserActiveGames(@PathVariable Long userId) {
        List<Game> games = gameService.findActiveGamesByUserId(userId);
        return ResponseEntity.ok(games);
    }

    /**
     * 获取可加入的游戏
     */
    @GetMapping("/available")
    public ResponseEntity<List<Game>> getAvailableGames(@RequestParam GameMode mode) {
        List<Game> games = gameService.findAvailableGames(mode);
        return ResponseEntity.ok(games);
    }

    /**
     * 获取游戏记录
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<Object> getGameHistory(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        // 这里可以实现游戏历史记录查询
        return ResponseEntity.ok("游戏历史记录功能待实现");
    }
}