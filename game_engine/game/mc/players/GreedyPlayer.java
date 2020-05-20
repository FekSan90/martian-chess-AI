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
package game.mc.players;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import game.engine.utils.Pair;
import game.mc.MCAction;
import game.mc.MCGame;
import game.mc.MCPlayer;

/**
 * Greedy player for the Matrian Chess game.
 * Measures the scores of the possible steps and chooses that has the maximal 
 * score, or if there exists more chooses one uniformly.
 */
public class GreedyPlayer extends MCPlayer {
  
  private MCAction prevAction;
  private int myScore;
  private int enemyScore;

  public GreedyPlayer(int color, int[][] board, Random r) {
    super(color, board, r);
    myScore = 0;
    enemyScore = 0;
  }

  @Override
  public MCAction getAction(List<Pair<Integer, MCAction>> prevActions) {
    int prevScore = 0;
    for (Pair<Integer, MCAction> action : prevActions) {
      if (action.second == null) {
        continue;
      }
      prevAction = action.second;
      boolean samePart = (action.second.x1 < 4 && action.second.x2 < 4) || (4 <= action.second.x1 && 4 <= action.second.x2);
      if (samePart) {
        // merge / move
        board[action.second.x2][action.second.y2] += board[action.second.x1][action.second.y1];
      } else {
        // capture / move
        prevScore = board[action.second.x2][action.second.y2];
        board[action.second.x2][action.second.y2] = board[action.second.x1][action.second.y1];
      }
      board[action.second.x1][action.second.y1] = MCGame.empty;
    }
    enemyScore += prevScore;
    int maxScore = 0;
    List<MCAction> actions = new LinkedList<MCAction>();
    // generate actions
    for (int i = 4 * color; i < 4 * (color + 1); i++) {
      for (int j = 0; j < 4; j++) {
        if (board[i][j] != MCGame.empty) {
          for (int i2 = 0; i2 < 8; i2++) {
            for (int j2 = 0; j2 < 4; j2++) {
              MCAction action = new MCAction(i, j, i2, j2);
              int score = -1;
              try {
                score = score(board, prevAction, prevScore, color, action, myScore - enemyScore);
              } catch (Exception e) {
                System.out.println("ACTION: " + action);
                e.printStackTrace();
                System.exit(1);
              }
              if (maxScore < score) {
                maxScore = score;
                actions.clear();
              }
              if (maxScore == score) {
                actions.add(action);
              }
            }
          }
        }
      }
    }
    MCAction action = actions.size() == 0 ? null : actions.get(r.nextInt(actions.size()));
    if (maxScore == 0 && board[action.x2][action.y2] != MCGame.empty) {
      board[action.x2][action.y2] += board[action.x1][action.y1];
    } else {
      board[action.x2][action.y2] = board[action.x1][action.y1];
    }
    board[action.x1][action.y1] = MCGame.empty;
    myScore += maxScore;
    return action;
  }
  
  /**
   * Returns the score the specified action corresponds to the specified state.
   * @param board current board
   * @param prevAction previous action
   * @param prevScore score of the previous action
   * @param color color of the current player
   * @param action action of the current player
   * @param scoreDiff difference of current player's and the enemy's score
   * @return score of the action.
   */
  private int score(int[][] board, MCAction prevAction, int prevScore, int color, MCAction action, int scoreDiff) {
    int[][] figures = new int[2][3];
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        if (board[i][j] != MCGame.empty) {
          if (i < board.length / 2) {
            figures[0][board[i][j] - 1]++;
          } else {
            figures[1][board[i][j] - 1]++;
          }
        }
      }
    }
    int numFigures = figures[color][0] + figures[color][1] + figures[color][2];
    int score = MCGame.score(board, figures, prevAction, prevScore, color, action);
    boolean samePart = (action.x1 < 4 && action.x2 < 4) || (4 <= action.x1 && 4 <= action.x2);
    if (0 <= score && !samePart && numFigures == 1) {
      if (scoreDiff < 0) {
        score = -1;
      } else {
        score = 10;
      }
    }
    return score;
  }

}
