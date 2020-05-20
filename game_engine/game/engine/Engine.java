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
package game.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import game.engine.ui.DrawTask;
import game.engine.ui.Drawable;
import game.engine.ui.GameApplication;
import game.engine.utils.ActionTask;
import game.engine.utils.ConstructionTask;
import game.engine.utils.Pair;
import game.engine.utils.StringBufferOutputStream;
import game.engine.utils.TimeOutTask;
import javafx.application.Platform;

public final class Engine {
  
  private static boolean isDebug;
  private final double fps;
  
  private final Game<Player<Action>, Action> game;
  private final Player<Action>[] players;
  private final List<Pair<Integer, Action>>[] prevActions;
  private static final PrintStream defaultOut = System.out;
  private static final PrintStream defaultErr = System.err;

  private static final StringBuffer sbOut = new StringBuffer();
  private static final StringBuffer sbErr = new StringBuffer();
  private static final PrintStream userOut = new PrintStream(new StringBufferOutputStream(sbOut));
  private static final PrintStream userErr = new PrintStream(new StringBufferOutputStream(sbErr));

  private static final ActionTask actionTask = new ActionTask();
  private static final ExecutorService service = Executors.newCachedThreadPool();
  
  private final String ofName;
  private ObjectOutputStream oos;
  private ObjectInputStream ois;
  static {
    ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(true);
  }

  @SuppressWarnings("unchecked")
  public Engine(double fps, String gameClass, String[] params) throws Exception {
    long postfix = System.nanoTime() % (long)1E9;
    boolean isReplay = false;
    this.fps = fps;
    Engine.isDebug = 0.0 < fps;
    File f = new File(gameClass);
    ofName = "gameplay_" + postfix + ".data";
    // read game from file to replay
    if (f.exists()) {
      ois = new ObjectInputStream(new FileInputStream(f));
      gameClass = (String)ois.readObject();
      params = (String[])ois.readObject();
      isReplay = true;
    } else {
      oos = new ObjectOutputStream(new FileOutputStream(ofName));
      oos.writeObject(gameClass);
      oos.writeObject(params);
    }
    
    if (!isDebug) {
      System.setOut(userOut);
      System.setErr(userErr);
    }
    
    game = (Game<Player<Action>, Action>) Class.forName(gameClass).getConstructor(PrintStream.class, String[].class, boolean.class).newInstance(new Object[] {defaultErr, params, isReplay});
    players = game.getPlayers();
    prevActions = new List[players.length];
    for (int i = 0; i < players.length; i++) {
      prevActions[i] = new LinkedList<Pair<Integer, Action>>();
    }
    if (isDebug) {
      defaultOut.println("GAME: " + gameClass);
    }
  }

  @SuppressWarnings("unchecked")
  public void play() throws Exception {
    GameApplication gApp = null;
    DrawTask drawTask = null;
    boolean isDrawable = game instanceof Drawable;
    long elapsed;
    
    // print table
    if (isDebug) {
      defaultOut.println(game);
      if (isDrawable) {
        Drawable drawable = (Drawable) game;
        gApp = drawable.getApplication();
        drawTask = new DrawTask(drawable);
      }
    }
    while (!game.isFinished()) {
      // get next player and its remaining time
      Player<Action> currentPlayer = game.getNextPlayer();
      if (currentPlayer == null) {
        break;
      }
      long remaining = game.getRemainingTime(currentPlayer);
      
      // print player statistics
      if (isDebug) {
        defaultOut.println("CURRENT: " + currentPlayer + " SCORE: " + game.getScore(currentPlayer) + " REM.TIME: " + remaining + " ms");
      }

      // timer for taking action based on the remaining time
      List<Pair<Integer, Action>> prevAction = prevActions[currentPlayer.getColor()];
      actionTask.setParams(currentPlayer, prevAction);
      Pair<Action, Long> result = timeOutTask(actionTask, remaining + 1);
      // we are in replay mode
      if (oos == null) {
        Object o = ois.readObject();
        result = (Pair<Action, Long>) o;
      }
      Action currentAction = result.first;
      elapsed = result.second;
      
      // clean previous actions for current player and set action for other players
      prevAction.clear();
      for (int i = 0; i < prevActions.length; i++) {
        if (i != currentPlayer.getColor()) {
          prevActions[i].add(new Pair<Integer, Action>(currentPlayer.getColor(), currentAction));
        }
      }
      
      // log current action
      if (oos != null) {
        oos.writeObject(result);
      }

      if (isDebug) {
        defaultOut.println("ACTION: " + currentAction);
        defaultOut.println("ELAPSED TIME: " + elapsed + " ms");
        if (!game.isValid(currentAction)) {
          defaultOut.println("ACTION: " + currentAction + " IS NOT VALID!!!");
        }
      }

      // sets the player's action
      game.setAction(currentPlayer, currentAction, elapsed);

      // draw table
      if (isDebug) {
        defaultOut.println(game);
        if (isDrawable) {
          Platform.runLater(drawTask);
        }
        try {
          Thread.sleep((long)(1000.0 * 1.0/fps));
        } catch (InterruptedException e) {
          e.printStackTrace(defaultErr);
        }
      }
    }
    
    // print final scores and remaining times
    for (int i = 0; i < players.length; i++) {
      defaultOut.println(i + " " + players[i] + " " + game.getScore(players[i]) + " " + game.getRemainingTime(players[i]));
    }
    
    service.shutdown();
    if (isDebug && isDrawable) {
      gApp.close();
    }
    
    System.setOut(defaultOut);
    System.setErr(defaultErr);
    if (oos != null) {
      oos.writeObject("end");
      oos.close();
      defaultErr.println("logfile: " + ofName);
    } else {
      ois.close();
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("required parameters for the engine are:");
      System.err.println("\t- fps");
      System.err.println("\t- game class");
      System.err.println("\t- game params");
      System.exit(1);
    }
    double fps = Double.parseDouble(args[0]);
    if (fps < 0) {
      System.err.println("Negative value is forbidden for fps: " + fps);
      System.exit(1);
    }
    String gameClass = args[1];
    String[] params = Arrays.copyOfRange(args, 2, args.length);
    Engine engine = null;
    try {
      engine = new Engine(fps, gameClass, params);
    } catch (Exception e) {
      e.printStackTrace(defaultErr);
      service.shutdown();
      return;
    }

    Game<Player<Action>, Action> game = engine.game;
    Thread tDraw = null;
    if (isDebug && game instanceof Drawable) {
      final Drawable drawable = (Drawable) game;
      tDraw = new Thread(new Runnable() {
        @Override
        public void run() {
          GameApplication.launch(drawable.getApplicationClass(), drawable.getAppParams());
        }
      });
      tDraw.start();
    }
    engine.play();
    if (isDebug && game instanceof Drawable) {
      tDraw.join();
    }
    System.exit(0);
  }

  public static final <R> Pair<R, Long> timeOutTask(TimeOutTask<R> task, long timeout) {
    Future<R> future = service.submit(task);
    R result = null;
    long elapsed = 0;
    try {
      result = future.get(timeout + 1, TimeUnit.MILLISECONDS);
      elapsed = task.getElapsed();
    } catch (Throwable e) {
      e.printStackTrace();
      elapsed = timeout + 1;
    } finally {
      future.cancel(true);
    }
    if (!isDebug && (sbOut.length() > 0 || sbErr.length() > 0)) {
      elapsed = timeout + 1;
      cleanOut();
    }
    return new Pair<R, Long>(result, elapsed);
  }

  public static final <R> Pair<R, Long> construct(long timeout, Constructor<R> constructor, Object... params) throws Exception {
    ConstructionTask<R> task = new ConstructionTask<R>();
    task.setConstructor(constructor, params);
    Pair<R, Long> result = timeOutTask(task, timeout + 1);
    return result;
  }
  
  private static final void cleanOut() {
    defaultOut.println("Writing is permited!");
    defaultOut.println("USER.OUT:\n" + sbOut);
    defaultOut.println("USER.ERR:\n" + sbErr);
    sbOut.delete(0, sbOut.length());
    sbErr.delete(0, sbErr.length());
  }

}
