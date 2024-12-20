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
        try {
            return gameLogicController.processCommand(gameCommand.getCommand());
        } catch (Exception e) {
            return GameResponse.returnErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/restart")
    public GameResponse restartGame() {
        // Reset the game state in GameLogicController
        System.out.println("[GameApiController] Received restart signal from frontend");
        return gameLogicController.restartGame();
    }

//    @PostMapping("/accusation")
//    public GameResponse handleAccusation(@RequestBody GameCommand gameCommand) {
//        System.out.println("[GameApiController] Received accusation command from frontend: " + gameCommand.getCommand());
//        StringBuilder output = gameLogicController.processCommand(gameCommand.getCommand());
//        return new GameResponse(output);
//    }

//    @PostMapping("/suggestion")
//    public GameResponse handleSuggestion(@RequestBody GameCommand gameCommand) {
//        System.out.println("[GameApiController] Received suggestion command from frontend: " + gameCommand.getCommand());
//        StringBuilder output = gameLogicController.processCommand(gameCommand.getCommand());
//        return new GameResponse(output);
//    }

    @GetMapping("/getBoardPositions")
    public GameResponse getBoardPositions() {
        System.out.println("[GameApiController] Received getBoardPositions request from frontend");
        return gameLogicController.getBoardPositions();
    }
}

