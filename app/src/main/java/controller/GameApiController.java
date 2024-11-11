package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameApiController {

    @Autowired
    private GameLogicController gameLogicController;

    @PostMapping("/command")
    public GameResponse handleCommand(@RequestBody GameCommand gameCommand) {
        System.out.println("[GameApiController] Received command from frontend: " + gameCommand.getCommand());
        StringBuilder output = gameLogicController.processCommand(gameCommand.getCommand());
        return new GameResponse(output);
    }

    @PostMapping("/restart")
    public GameResponse restartGame() {
        // Reset the game state in GameLogicController
        System.out.println("[GameApiController] Received restart signal from frontend");
        StringBuilder initialInstructions = new StringBuilder(gameLogicController.restartGame());
        return new GameResponse(initialInstructions);
    }
}

