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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import game.mc.MCAction;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameGraphicsController implements Initializable, EventHandler<MouseEvent> {

  private static final BlockingQueue<MCAction> QUEUE = new ArrayBlockingQueue<MCAction>(1, true);

  @FXML
  private Canvas gameLayer;
  @FXML
  private Canvas bgLayer;
  @FXML
  private Pane root;
  private GraphicsContext gameGC;
  private GraphicsContext bgGC;
  private Double lastX, lastY;
  private int clickI, clickJ;

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    bgLayer.toFront();
    gameLayer.toFront();
    gameGC = gameLayer.getGraphicsContext2D();
    bgGC = bgLayer.getGraphicsContext2D();
    bgGC.setFill(Color.DARKRED);
    gameLayer.setOnMouseClicked(this);
    gameLayer.setOnMouseMoved(this);
    clickI = -1;
    clickJ = -1;
  }

  public void setSize(int h, int w) {
    gameLayer.setHeight(h);
    bgLayer.setHeight(h);
    gameLayer.setWidth(w);
    bgLayer.setWidth(w);
  }

  void clear() {
    gameGC.clearRect(0, 0, gameLayer.getWidth(), gameLayer.getHeight());
  }

  void update(double x, double y, double h, double w, Image image) {
    gameGC.drawImage(image, x, y, w, h);
  }

  @Override
  public synchronized void handle(MouseEvent event) {
    EventType<? extends MouseEvent> eventType = event.getEventType();
    double x = Math.min(Math.max(event.getX() - MCApplication.MULTIPLIER / 2, 0), gameLayer.getWidth() - MCApplication.MULTIPLIER);
    double y = Math.min(Math.max(event.getY() - MCApplication.MULTIPLIER / 2, 0), gameLayer.getHeight() - MCApplication.MULTIPLIER);
    if (MouseEvent.MOUSE_CLICKED.equals(eventType)) {
      onMouseClick(x, y);
    } else if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
      if (lastX != null && lastY != null) {
        onMouseLeave(lastX, lastY);
      }
      onMouseMove(x, y);
    }

  }

  public static MCAction getAction() throws InterruptedException {
    return QUEUE.take();
  }

  private void onMouseClick(double x, double y) {
    int i = (int) Math.round(y / MCApplication.MULTIPLIER);
    int j = (int) Math.round(x / MCApplication.MULTIPLIER);
    if (clickI != -1 && clickJ != -1) {
      MCAction action = new MCAction(clickI, clickJ, i, j);
      boolean succes = QUEUE.offer(action);
      while (!succes) {
        QUEUE.clear();
        succes = QUEUE.offer(action);
      }
      bgGC.clearRect(clickJ * MCApplication.MULTIPLIER, clickI * MCApplication.MULTIPLIER, MCApplication.MULTIPLIER, MCApplication.MULTIPLIER);
      clickI = -1;
      clickJ = -1;
    } else {
      clickI = i;
      clickJ = j;
      bgGC.setFill(Color.DARKGREEN);
      bgGC.fillRect(clickJ * MCApplication.MULTIPLIER, clickI * MCApplication.MULTIPLIER, MCApplication.MULTIPLIER, MCApplication.MULTIPLIER);
      bgGC.setFill(Color.DARKRED);
    }
  }

  private void onMouseMove(double x, double y) {
    lastX = (double) (Math.round(x / MCApplication.MULTIPLIER) * MCApplication.MULTIPLIER);
    lastY = (double) (Math.round(y / MCApplication.MULTIPLIER) * MCApplication.MULTIPLIER);
    if (lastX != clickJ * MCApplication.MULTIPLIER || lastY != clickI * MCApplication.MULTIPLIER) {
      bgGC.fillRect(lastX, lastY, MCApplication.MULTIPLIER, MCApplication.MULTIPLIER);
    }
  }

  private void onMouseLeave(Double x, Double y) {
    if (x != clickJ * MCApplication.MULTIPLIER || y != clickI * MCApplication.MULTIPLIER) {
      bgGC.clearRect(x, y, MCApplication.MULTIPLIER, MCApplication.MULTIPLIER);
    }
  }
}
