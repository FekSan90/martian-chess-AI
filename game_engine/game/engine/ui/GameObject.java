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
package game.engine.ui;

import javafx.scene.image.Image;

public class GameObject {

  private final double x;
  private final double y;
  private final double w;
  private final double h;
  private final Image img;

  public GameObject(double x, double y, double w, double h, Image img) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.img = img;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidth() {
    return w;
  }

  public double getHeight() {
    return h;
  }

  public Image getImage() {
    return img;
  }
}
