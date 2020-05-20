/*
 * Copyright (c) 2008 University of Szeged
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package game.engine;

/**
 * Describes games for the specified type of player and action.
 * 
 * @param <P> type of players
 * @param <A> type of actions
 */
public interface Game<P extends Player<A>, A extends Action> {
  /**
   * Returns the players of the game.
   * @return the players of the game
   */
  public P[] getPlayers();
  /**
   * Returns the next player of the turn.
   * @return the next player
   */
  public P getNextPlayer();
  /**
   * Checks the correctness of the specified action.
   * @param action to be checked
   * @return true, if the specified action is correct, false otherwise
   */
  public boolean isValid(A action);
  /**
   * Sets the specified action of the specified player for the game and the 
   * elapsed time, was taken to compute the action.
   * @param player action belongs to
   * @param action to be set
   * @param time computation time
   */
  public void setAction(P player, A action, long time);
  /**
   * Returns the remaining game-time of the specified player.
   * @param player to be checked
   * @return remaining game-time
   */
  public long getRemainingTime(P player);
  /**
   * Checks the end of the game.
   * @return true, if the game has been finished, false otherwise.
   */
  public boolean isFinished();
  /**
   * Returns the score of the specified player.
   * @param player to be checked
   * @return game-score
   */
  public double getScore(P player);
}
