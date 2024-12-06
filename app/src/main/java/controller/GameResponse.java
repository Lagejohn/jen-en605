package controller;

class GameResponse {
    private String output;
    private String displayMessage;

    public GameResponse(StringBuilder output, StringBuilder displayMessage) {
        this.output = String.valueOf(output);
        this.displayMessage = String.valueOf(displayMessage);
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public static GameResponse returnErrorResponse(String errorMessage) {
        return new GameResponse(new StringBuilder(), new StringBuilder("Failed:" + errorMessage));
    }
}