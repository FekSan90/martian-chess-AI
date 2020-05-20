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

import game.engine.Action;

/**
 * Specifies the type of the action for the Matrian Chess game. That is a pair 
 * of positions. The first is the position of a figure on the table, the second 
 * is a position where to move it.
 */
public final class MCAction implements Action {
  private static final long serialVersionUID = 1984433168441571946L;
  
  /** From and to coordinates of the moving figure. */
  public final int x1, y1, x2, y2;
  /**
   * @param x1 row index of move from
   * @param y1 column index of move from
   * @param x2 row index of move to
   * @param y2 column index of move to
   */
  public MCAction(int x1, int y1, int x2, int y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }
  
  public String toString() {
    return x1 + ", " + y1 + " -> " + x2 + ", " + y2;
  }
}
