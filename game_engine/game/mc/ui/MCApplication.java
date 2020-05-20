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
package game.mc.ui;

import game.engine.ui.Drawable;
import game.engine.ui.GameApplication;
import game.engine.ui.GameObject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class MCApplication extends GameApplication {

  public static final int MULTIPLIER = 50;
  private GameGraphicsController controller;

  @Override
  public void draw(Drawable drawable) {
    controller.clear();
    for (GameObject go : drawable.getGameObjects()) {
      double x = go.getX() * MULTIPLIER;
      double y = go.getY() * MULTIPLIER;
      double h = go.getHeight() * MULTIPLIER;
      double w = go.getWidth() * MULTIPLIER;
      controller.update(x, y, h, w, go.getImage());
    }
  }

  @Override
  protected Scene getScene() {
    int h = 8 * MULTIPLIER;
    int w = 4 * MULTIPLIER;
    FXMLLoader loader = new FXMLLoader(MCApplication.class.getResource("/game/mc/ui/resources/GameGraphicsView.fxml"));
    Pane root = new Pane();
    try {
      root = loader.load();
      controller = loader.getController();
      controller.setSize(h, w);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return new Scene(root, w, h);
  }

}
