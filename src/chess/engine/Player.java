package chess.engine;

import chess.PlayerColor;

public class Player {
    private PlayerColor color;

    public Player(PlayerColor color){
        if(color == null){
            throw new RuntimeException("We need a color !");
        }
        this.color = color;
    }

    public PlayerColor getColor() {
        return color;
    }
}
