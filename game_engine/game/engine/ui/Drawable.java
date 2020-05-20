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

import java.util.List;

import javafx.application.Application;

/**
 * Defines the functions have to be implemented for a game that has gui.
 */
public interface Drawable {
  /**
   * Returns the class of the application that can draw the game.
   * @return application class
   */
  public Class<? extends Application> getApplicationClass();
  /**
   * Returns the game objects to be drawn.
   * @return game objects
   */
  public List<GameObject> getGameObjects();
  /**
   * Returns the application that can draw the game.
   * @return application
   */
  public GameApplication getApplication();
  /**
   * Returns the parameters, required for the application class.
   * @return application parameters
   */
  public String[] getAppParams();
}
