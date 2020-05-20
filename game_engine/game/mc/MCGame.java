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
package game.mc;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import game.engine.Engine;
import game.engine.Game;
import game.engine.ui.Drawable;
import game.engine.ui.GameApplication;
import game.engine.ui.GameObject;
import game.engine.utils.Pair;
import game.engine.utils.Utils;
import game.mc.players.DummyPlayer;
import game.mc.players.HumanPlayer;
import game.mc.ui.MCApplication;
import javafx.application.Application;
import javafx.scene.image.Image;

/**
 * Implements the two-player Martian Chess
 * @see <a href="http://www.looneylabs.com/rules/martian-chess">Martian Chess</a>
 */
public final class MCGame implements Game<MCPlayer, MCAction>, Drawable {

  /** value of an empty cell on the board */
  public static final int empty = 0;
  private static final String[] colors = {"White", "Black"};
  
  private final long seed;
  private final int[][] board;
  private final int[][] figures;
  private final int[] numFigures;
  private final MCPlayer[] players;
  private final long[] remainingTimes;
  private int numAlives;
  private final int[] scores;
  private int currentPlayer;
  private int round;
  
  private MCAction prevAction = null;
  private int prevScore = 0;
  
  private final int n, m;
  
  @SuppressWarnings("unchecked")
  public MCGame(PrintStream errStreamm, String[] params, boolean isReplay) throws Exception {
    if (params.length < 4) {
      errStreamm.println("required parameters for the game are:");
      errStreamm.println("\t- random seed");
      errStreamm.println("\t- timeout");
      errStreamm.println("\t- player classes...");
      System.exit(1);
    }
    
    // parse parameters
    seed = Long.parseLong(params[0]);
    long time = Long.parseLong(params[1]);
    players = new MCPlayer[params.length - 2];
    if (players.length != 2) {
      errStreamm.println("Two players are allowed only instead of " + players.length);
      System.exit(1);
    }
    
    remainingTimes = new long[players.length];
    scores = new int[players.length];
    numAlives = players.length;
    currentPlayer = 0;
    round = 0;
    
    n = 8;
    m = 4;
    board = new int[n][m];
    board[0][0] = 3; board[n - 1][m - 1] = 3;
    board[1][0] = 3; board[n - 2][m - 1] = 3;
    board[0][1] = 3; board[n - 1][m - 2] = 3;
    board[2][0] = 2; board[n - 3][m - 1] = 2;
    board[1][1] = 2; board[n - 2][m - 2] = 2;
    board[0][2] = 2; board[n - 1][m - 3] = 2;
    board[2][1] = 1; board[n - 3][m - 2] = 1;
    board[2][2] = 1; board[n - 3][m - 3] = 1;
    board[1][2] = 1; board[n - 2][m - 3] = 1;
    
    figures = new int[players.length][3];
    numFigures = new int[players.length];
    for (int i = 0; i < players.length; i++) {
      numFigures[i] = 9;
      for (int j = 0; j < 3; j++) {
        figures[i][j] = 3;
      }
    }
    
    // create players and measure construction time
    for (int i = 0; i < players.length; i++) {
      Random r = new Random(seed);
      int[][] playerBoard = new int[n][m];
      setBoard(board, playerBoard);
      // if we want to replay a match from log-file without the classes
      Class<MCPlayer> clazz = (Class<MCPlayer>) Class.forName(DummyPlayer.class.getName());
      if (isReplay) {
        errStreamm.println("Game is in replay mode, Player: " + i+ " is the DummyPlayer, but was: " + params[i + 2]);
      } else {
        clazz = (Class<MCPlayer>) Class.forName(params[i + 2]);
      }
      Constructor<MCPlayer> constructor = clazz.getConstructor(int.class, int[][].class, Random.class);
      Pair<MCPlayer, Long> created = Engine.construct(time, constructor, i, playerBoard, r);
      players[i] = created.first;
      if (players[i] instanceof HumanPlayer) {
        remainingTimes[i] = Long.MAX_VALUE - created.second - 10;
      } else {
        remainingTimes[i] = time - created.second;
      }
      
      // check color hacking
      if (players[i].color != i) {
        int color = players[i].color;
        remainingTimes[i] = 0;
        Field field = MCPlayer.class.getDeclaredField("color");
        field.setAccessible(true);
        field.set(players[i], i);
        field.setAccessible(false);
        errStreamm.println("Illegal color (" + color + ") was set for player: " + players[i]);
      }
      numAlives -= 0 < remainingTimes[i] ? 0 : 1;
    }
  }
  
  @Override
  public MCPlayer[] getPlayers() {
    return players;
  }

  @Override
  public MCPlayer getNextPlayer() {
    MCPlayer player = null;
    if (numAlives == players.length) {
      player = players[currentPlayer];
    }
    return player;
  }

  @Override
  public boolean isValid(MCAction action) {
    return -1 != score(board, figures, prevAction, prevScore, currentPlayer, action);
  }
  
  /**
   * Returns the score of the specified action corresponds to the specified state or -1 for an invalid action.
   * @param board current board
   * @param figures the number of available figures of the players, the array is 
   * indexed by: <br>
   * first, the color of a player;<br>
   * second, the value of a figure -1.
   * @param prevAction previous action
   * @param prevScore score of the previous action
   * @param color color of the current player
   * @param action action of the current player
   * @return score of the action.
   */
  public static int score(int[][] board, int[][] figures, MCAction prevAction, int prevScore, int color, MCAction action) {
    if (action == null) {
      return -1;
    }
    // can not be reversed a move, except it was a hit
    if (prevAction != null && prevScore == 0 &&
        prevAction.x1 == action.x2 &&
        prevAction.y1 == action.y2 &&
        prevAction.x2 == action.x1 &&
        prevAction.y2 == action.y1) {
      return -1;
    }
    if (action.x1 == action.x2 && action.y1 == action.y2) {
      return -1;
    }
    // check correct board side and edges
    if(action.x1 < (color + 1) * 4 &&
       color * 4 <= action.x1 && 
       0 <= action.y1 && action.y1 < 4 && 
       0 <= action.x2 && action.x2 < 8 &&
       0 <= action.y2 && action.y2 < 4) {
      
      boolean samePart = (action.x1 < 4 && action.x2 < 4) || (4 <= action.x1 && 4 <= action.x2);
      // check movement
      int boardFrom = board[action.x1][action.y1];
      int boardTo = board[action.x2][action.y2];
      int xdiff = action.x2 - action.x1;
      int ydiff = action.y2 - action.y1;
      switch (boardFrom) {
      case 0:
        // empty
        return -1;
      case 1:
        // pawn
        if (Math.abs(xdiff) == 1 && Math.abs(ydiff) == 1) {
          if (boardTo == empty) {
            // empty
            return 0;
          } else if (samePart) {
            // merge
            return boardTo == 3 || 0 < figures[color][boardTo] ? -1 : 0;
          } else {
            return boardTo;
          }
        }
        return -1;
      case 2:
        // drone
        if ((Math.abs(xdiff) == 0 && Math.abs(ydiff) <= 2) || 
            (Math.abs(ydiff) == 0 && Math.abs(xdiff) <= 2)) {
          if (Math.abs(xdiff) + Math.abs(ydiff) == 2 && board[(action.x1 + action.x2) / 2][(action.y1 + action.y2) / 2] != empty) {
            // jump
            return -1;
          }
          if (boardTo == empty) {
            // empty
            return 0;
          } else if (samePart) {
            // merge
            return boardTo != 1 || 0 < figures[color][2] ? -1 : 0;
          } else {
            return boardTo;
          }
        }
        return -1;
      case 3:
        // queen
        if (samePart && boardTo != empty) {
          // can not be merged
          return -1;
        }
        if (xdiff == 0) {
          // horizontal
          for (int i = Math.min(action.y1, action.y2) + 1; i < Math.max(action.y1, action.y2); i++) {
            if (board[action.x1][i] != empty) {
              // jump
              return -1;
            }
          }
          return boardTo;
        } else if (ydiff == 0) {
          // vertical
          for (int i = Math.min(action.x1, action.x2) + 1; i < Math.max(action.x1, action.x2); i++) {
            if (board[i][action.y1] != empty) {
              // jump
              return -1;
            }
          }
          return boardTo;
        } else if (Math.abs(xdiff) == Math.abs(ydiff)) {
          // diagonal
          for (int i = 1; i < Math.abs(xdiff); i++) {
            if (board[action.x1 + (i * (xdiff < 0 ? -1 : 1))][action.y1 + (i * (ydiff < 0 ? -1 : 1))] != empty) {
              // jump
              return -1;
            }
          }
          return boardTo;
        }
        return -1;
      default:
        // not possible
        throw new RuntimeException("board value can not be " + boardFrom);
      }
    }
    return -1;
  }

  @Override
  public void setAction(MCPlayer player, MCAction action, long time) {
    int score = score(board, figures, prevAction, prevScore, currentPlayer, action);
    if (score < 0) {
      time = remainingTimes[currentPlayer] + 1;
    }
    remainingTimes[currentPlayer] -= time;
    if (remainingTimes[currentPlayer] <= 0) {
      scores[currentPlayer] = -1;
      numAlives--;
      score = -1;
    }
    prevAction = action;
    prevScore = score;
    if (-1 < score) {
      scores[currentPlayer] += score;
      boolean samePart = (action.x1 < 4 && action.x2 < 4) || (4 <= action.x1 && 4 <= action.x2);
      if (score == 0 && board[action.x2][action.y2] != empty) {
        // merge
        figures[currentPlayer][board[action.x1][action.y1] - 1]--;
        figures[currentPlayer][board[action.x2][action.y2] - 1]--;
        board[action.x2][action.y2] += board[action.x1][action.y1];
        figures[currentPlayer][board[action.x2][action.y2] - 1]++;
        numFigures[currentPlayer]--;
      } else {
        if (!samePart) {
          figures[currentPlayer][board[action.x1][action.y1] - 1]--;
          figures[1 - currentPlayer][board[action.x1][action.y1] - 1]++;
          numFigures[currentPlayer]--;
          numFigures[1 - currentPlayer]++;
          if (board[action.x2][action.y2] != empty) {
            figures[1 - currentPlayer][board[action.x2][action.y2] - 1]--;
            numFigures[1 - currentPlayer]--;
          }
        }
        board[action.x2][action.y2] = board[action.x1][action.y1];
      }
      board[action.x1][action.y1] = empty;
    }
    if (numFigures[currentPlayer] == 0) {
      numAlives--;
      // it is a win!!!
      if (scores[currentPlayer] == scores[1 - currentPlayer]) {
        scores[currentPlayer] ++;
      }
    }
    currentPlayer = 1 - currentPlayer;
    if (currentPlayer == 0) {
      round ++;
    }
  }

  @Override
  public long getRemainingTime(MCPlayer player) {
    return player == null ? -1 : remainingTimes[player.color];
  }

  @Override
  public boolean isFinished() {
    return numAlives != players.length;
  }

  @Override
  public double getScore(MCPlayer player) {
    return player == null ? 0 : scores[player.color];
  }
  
  /**
   * Copies the values of the first array to the second array.
   * @param src to be cloned
   * @param dst stored to
   */
  private void setBoard(int[][] src, int[][] dst) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        dst[i][j] = src[i][j];
      }
    }
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Round: " + round + "\n");
    for (int i = 0; i < players.length; i++) {
      if (players[i] == null) {
        continue;
      }
      sb.append(players[i].getClass().getName() + ": " + (players[i].color < colors.length ? colors[players[i].color] : players[i].color) + "\n");
    }
    sb.append("\n  ");
    for (int i = 0; i < m; i++) {
      sb.append(" " + i);
    }
    sb.append("\n\n");
    for (int i = 0; i < n; i++) {
      sb.append(String.format(Locale.US, "%-2d", i));
      for (int j = 0; j < m; j++) {
        sb.append(" " + (board[i][j] == empty ? "*" : board[i][j]));
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public List<GameObject> getGameObjects() {
    Image[] images = Utils.getImages("/game/mc/ui/resources/", new String[]{"white_pawn", "white_rook", "white_queen", "black_pawn", "black_rook", "black_queen"});
    LinkedList<GameObject> gos = new LinkedList<GameObject>();
    int add = 0;
    for (int i = 0; i < n; i++) {
      if (3 < i) {
        add = 3;
      }
      for (int j = 0; j < m; j++) {
        if (board[i][j] != empty) {
          gos.add(new GameObject(j, i, 1, 1, images[add + board[i][j] - 1]));
        }
      }
    }
    return gos;
  }

  @Override
  public Class<? extends Application> getApplicationClass() {
    return MCApplication.class;
  }

  @Override
  public GameApplication getApplication() {
    return MCApplication.getInstance();
  }

  @Override
  public String[] getAppParams() {
    return null;
  }

}
