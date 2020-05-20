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

import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class GameApplication extends Application {

  private static final CountDownLatch waiter = new CountDownLatch(1);
  private static GameApplication instance = null;

  public final static GameApplication getInstance() {
    try {
      waiter.await();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    return instance;
  }

  public abstract void draw(Drawable game);

  protected abstract Scene getScene();

  @Override
  public final void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle(this.getClass().getSimpleName());
    primaryStage.setScene(getScene());
    primaryStage.show();
    instance = this;
    waiter.countDown();
  }

  public final void close() {
    Platform.exit();
  }
}
