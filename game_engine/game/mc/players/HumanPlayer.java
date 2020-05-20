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

import java.util.List;
import java.util.Random;

import game.engine.utils.Pair;
import game.mc.MCAction;
import game.mc.MCPlayer;
import game.mc.ui.GameGraphicsController;

/**
 * Player for manual testing.
 */
public class HumanPlayer extends MCPlayer {

  public HumanPlayer(int color, int[][] board, Random r) {
    super(color, board, r);
  }

  @Override
  public MCAction getAction(List<Pair<Integer, MCAction>> prevActions) {
    try {
      return GameGraphicsController.getAction();
    } catch (InterruptedException ex) {
      return null;
    }
  }

}
