package chess.engine;

import chess.ChessController;
import chess.ChessView;
import chess.PlayerColor;
import chess.engine.pieces.*;

import java.awt.*;

/* ---------------------------
Laboratoire 	: 09
Fichier 		: engine/ChessGame.java
Auteur(s) 	    : Eric Bousbaa, Ilias Goujgali
Date			: 14.01.2020

But 			: La classe ChessGame fait le lien entre la GUI, l'utilisateur et l'échiquier (via ChessBoard).
                  La classe permet de démarrer une partie, d'effectuer des mouveemnets et de gérer les cours.

Remarque(s) 	: - 

Compilateur	 : javac 11.0.4
--------------------------- */
public class ChessGame implements ChessController {
    private ChessView view;
    private ChessBoard chessBoard;

    private Player turn;
    private Player player1;
    private Player player2;

    /**
     * Initialise la vue.
     * @param view La vue à utiliser.
     * @throws RuntimeException Si la vue est une référence null.
     */
    @Override
    public void start(ChessView view) {
        if(view == null){
            throw new RuntimeException("Qu'est-ce qu'is this view.");
        }
        view.startView();
        this.view = view;
    }

    /**
     * Affiche un message (texte) à l'utilsiateur. Il ne s'agit pas d'un pop-up, càd
     * que l'utilisateur peut continuer à jouer en ayant le message affiché.
     * @param message Message à afficher.
     */
    protected void displayMessage(String message){
        view.displayMessage(message);
    }

    /**
     * Effectue le mouvement sur l'échiquier si ce dernier est légal. Un mouvement légal
     * doit respecter les règles de déplacement de la pièce sélectionné et la pièce bougeant
     * doit être de la même couleur que le joueur jouant le tour.
     * Les tours d'une partie d'un jeu s'effectuentent en alternance entre les 2 joueurs.
     * Le tour change lors-ce qu'une pièce a été bougé sur l'échiquier.
     * @param fromX Abscisse (x) de la position initiale de la pièce à bouger.
     * @param fromY Ordonnée (y) de la position initiale de la pièce à bouger.
     * @param toX Abscisse (x) de la position cible où devrait aller la pièce.
     * @param toY Ordonnée (y) de la position cible où devrait aller la pièce.
     * @return Vrai si le mouvement c'est correctement effectué. Faux dans le cas contraire.
     */
    @Override
    public boolean move(int fromX, int fromY, int toX, int toY) {
        Point from = new Point(fromX, fromY);
        Point to = new Point(toX ,toY);
        if(chessBoard.isCellEmpty(from)){
            return false;
        }
        Piece toMove = chessBoard.getCellAt(from);

        if(isItsTurn(toMove)){
            for(Move move : toMove.getMoves(from, false)){
                if(move.equals(to)){
                    removePieceAt(from);
                    placePieceAt(toMove, to);

                    if(move.getSpecialMove() != null) {
                        switch (move.getSpecialMove()) {
                            case PAWN_EN_PASSANT:
                                // todo redondant voir plus bas => faire fonction ? library class Utils ?
                                int deltaPlayer = toMove.getSide() == Side.BOTTOM ? 1 : -1;
                                removePieceAt(new Point(toX, toY - deltaPlayer));
                                break;

                            case PAWN_PROMOTION:
                                PieceColor pc = new PieceColor(toMove.getColor(), toMove.getSide());
                                ChessView.UserChoice promoPiece = view.askUser("Vous êtes promu, soldat !", "Quel grade souhaitez-vous avoir ?",
                                        new Queen(pc, chessBoard), new Bishop(pc, chessBoard), new Rook(pc, chessBoard), new Knight(pc, chessBoard));
                                if (promoPiece != null) {
                                    removePieceAt(to);
                                    placePieceAt((Piece) promoPiece, to);
                                }
                                break;
                                
                            case KING_LONG_CASTLED:
                                Rook rRook = (Rook) chessBoard.getCellAt(new Point(toX - 2, toY));
                                removePieceAt(new Point(toX - 2, toY));
                                placePieceAt(rRook, new Point(toX + 1, toY));
                                break;

                            case KING_SHORT_CASTLED:
                                Rook lRook = (Rook) chessBoard.getCellAt(new Point(toX + 1, toY));
                                removePieceAt(new Point(toX + 1, toY));
                                placePieceAt(lRook, new Point(toX - 1, toY));
                                break;
                        }
                    }

                    chessBoard.setLastMove(move);
                    endTurn();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Informe si la pièce sélectionnée appartient au joueur jouant le tour actuel.
     * @param piece Pièce appartenant à un des deux joueurs.
     * @return Vrai si la pièce appartient au joueur pouvant jouer. Faux si elle
     *         elle appartient à l'autre joueur.
     */
    private boolean isItsTurn(Piece piece){
        return piece.getColor() == turn.getColor();
    }

    /**
     * Passe la main au joueur adverse pour un nouveau tour.
     */
    private void endTurn(){
        Color color = turn == player1 ? Color.BLACK : Color.WHITE;
        view.setCurrentPlayerColor(color);
        turn = turn == player1 ? player2 : player1;
    }

    /**
     * Enlève une pièce de l'échiquier.
     * Met à jour la vue.
     * @param pos Coordonnée (x,y) de la pièce à enlever.
     * @throws RuntimeException Si la cellule à supprimer est vide.
     */
    private void removePieceAt(Point pos){
        if(!chessBoard.removePieceAt(pos)){
            throw new RuntimeException("Piece is introuvable");
        }
        view.removePiece(pos.x, pos.y);
    }

    /**
     * Place une pièce sur l'échiquier.
     * Met à jour la vue.
     * @param piece Pièce à placer.
     * @param pos Coordonnée de la cellule (x,y) où doit aller la pièce.
     */
    private void placePieceAt(Piece piece, Point pos){
        chessBoard.placePieceAt(piece, pos);
        // On marque la pièce comme bougée si nécessaire.
        if (SpecialFirstMove.class.isAssignableFrom(piece.getClass())) {
            SpecialFirstMove spePiece = (SpecialFirstMove) piece;
            spePiece.hasMoved();
        }
        view.putPiece(piece.getType(), piece.getColor(), pos.x, pos.y);
    }

    /**
     * Initialisation d'une nouvelle partie.
     */
    @Override
    public void newGame() {
        ChessBoard chessBoard = new ChessBoard(this);
        int nCote = chessBoard.getDimension();

        Player player1 = new Player(PlayerColor.WHITE);
        Player player2 = new Player(PlayerColor.BLACK);

        chessBoard.setUpTeam(player1, Side.BOTTOM);
        chessBoard.setUpTeam(player2, Side.TOP);

        for(int x = 0; x < nCote; ++x){
            for(int y = 0; y < nCote; ++y){
                Point pos = new Point(x, y);
                if(!chessBoard.isCellEmpty(pos)){
                    Piece piece = chessBoard.getCellAt(pos);
                    view.putPiece(piece.getType(), piece.getColor(), x, y);
                }
            }
        }

        this.player1 = player1;
        this.player2 = player2;
        this.chessBoard = chessBoard;
        this.turn = player1; // Le blanc commence.
    }
}