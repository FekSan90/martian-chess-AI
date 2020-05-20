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
package game.engine.utils;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import javafx.scene.image.Image;

/**
 * Class for utility functions.
 */
public final class Utils {
  private static final Gson gson = new Gson();
  private static Image[] images = null;

  /**
   * Returns the JSON string representation of the specified object.
   * @param o to be printed
   * @return JSON string
   */
  public static String jsonSerialize(Object o) {
    return gson.toJson(o);
  }
  
  public static <T> T jsonDeSerialize(Class<T> clazz, String o) {
    return gson.fromJson(o, clazz);
  }

  public static Image[] getImages(String pathPrefix) {
    if (images == null) {
      List<Image> imgs = new LinkedList<Image>();
      InputStream is;
      int idx = 0;
      do {
        is = Utils.class.getResourceAsStream(pathPrefix + idx + ".png");
        if (is != null) {
          imgs.add(new Image(is));
        }
        idx ++;
      } while (is != null);
      images = imgs.toArray(new Image[0]);
    }
    return images;
  }
  
  public static Image[] getImages(String pathPrefix, String[] fNames) {
    if (images == null) {
      List<Image> imgs = new LinkedList<Image>();
      InputStream is;
      for (int i = 0; i < fNames.length; i++) {
        is = Utils.class.getResourceAsStream(pathPrefix + fNames[i] + ".png");
        if (is != null) {
          imgs.add(new Image(is));
        }
      }
      images = imgs.toArray(new Image[0]);
    }
    return images;
  }
}
