package controller;

class GameResponse {
    private String output;

    public GameResponse(StringBuilder output) {
        this.output = String.valueOf(output);
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}