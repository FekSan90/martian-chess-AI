//FekSan


/**
*   Mesterséges inteligencia kurzusra készült mar(si sak)k AI kötelező program
*
* @author    Fekete Sándor (YZ2ZBA)
* @since     2018.11.17
* @version   1.0
*/


import game.engine.utils.Pair;
import game.mc.MCAction;
import game.mc.MCGame;
import game.mc.MCPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.*;

public class h153605 extends MCPlayer {

    /**
     * Prioritási sor sorrendbe rakási szempontja score paraméter alapján történjen
     *
     */
    class ActionComparator implements Comparator<ActionWithScore> {
        public int compare(ActionWithScore a1, ActionWithScore a2) {
            if (a1.score < a2.score)
                return 1;
            else if (a1.score > a2.score)
                return -1;
            return 0;
        }
    }

    /**
     * A lépésekhez egy pontot társít, ami alapján csökkenő sorrendbe fog szerepelni a prioritási sorban.
     *
     * @param action    Lépés
     * @param score     Hozzá tartozó jósági érték
     *
     */
    class ActionWithScore {
        public MCAction action;
        public int score;

        public ActionWithScore(MCAction action, int score) {
            this.action = action;
            this.score = score;
        }
        /**
         * Visszaadja a lépést
         *
         */
        public MCAction getAction() {
            return action;
        }
    }


    private MCAction prevAction;
    private int myScore;
    private int enemyScore;


    public h153605(int color, int[][] board, Random r) {


// -----Inenntől
        super(color, board, r);
        myScore = 0;
        enemyScore = 0;


    }

    @Override
    public MCAction getAction(List<Pair<Integer, MCAction>> prevActions) {
        int prevScore = 0;
        boolean samePart;
        for (Pair<Integer, MCAction> action : prevActions) {
            if (action.second == null) {
                continue;
            }
            prevAction = action.second;
            samePart = (action.second.x1 < 4 && action.second.x2 < 4) || (4 <= action.second.x1 && 4 <= action.second.x2);
            if (samePart) {
                // merge / move
                board[action.second.x2][action.second.y2] += board[action.second.x1][action.second.y1];
            } else {
                // capture / move
                prevScore = board[action.second.x2][action.second.y2];
                board[action.second.x2][action.second.y2] = board[action.second.x1][action.second.y1];
            }
            board[action.second.x1][action.second.y1] = MCGame.empty;
        }
        enemyScore += prevScore;
        int maxScore = 0;
//---- Idáig megegyezik a Greedyvel


        /**
         *  actions tartalmazza a lépéseket ami áltatlán estbe fog lépni, de ha emergency be kerül lépés, ami akkor
         *  történik meg ha a greedy ütni készül, tehát menekülni kell vagy ezt magas score számmal nagyobb prioritást élvez.
         *
         *  Minden más lépét is letáról arra az esetre ha valami oknál fogva nincs az actionba lépés.
         *
         */
        PriorityQueue<ActionWithScore> actions = new PriorityQueue<ActionWithScore>(10, new ActionComparator());
        PriorityQueue<ActionWithScore> emergency = new PriorityQueue<ActionWithScore>(10, new ActionComparator());


        /**
         *  cost tartalmazza a hasznossági szintet  (score paraméter)
         */
        int cost = 0;

        MCAction greedyStep;
//---- Inenntől
        /**
         *  generate actions, lehetséges lépések feltérképezése
         */
        for (int i = 4 * color; i < 4 * (color + 1); i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != MCGame.empty) {
                    for (int i2 = 0; i2 < 8; i2++) {
                        for (int j2 = 0; j2 < 4; j2++) {
                            MCAction action = new MCAction(i, j, i2, j2);
                            int score = -1;
                            try {
                                score = score(board, prevAction, prevScore, color, action, myScore - enemyScore);
                            } catch (Exception e) {
                                System.out.println("ACTION: " + action);
                                e.printStackTrace();
                                System.exit(1);
                            }
                            if (maxScore < score) {
                                /**
                                 * Ha van jobb akkor a többire nincs szükség
                                 */
                                maxScore = score;
                                actions.clear();
                            }

                            if (maxScore == score) {
//-- Idáig megeggyezik a greedy-vel
                                /**
                                 *Saját térfél-e
                                 */
                                samePart = (action.x1 < 4 && action.x2 < 4) || (4 <= action.x1 && 4 <= action.x2);

                                /**
                                 *Ha saját térfél és üres akkor a könzönbös
                                 */

                                if (board[i2][j2] == MCGame.empty && samePart) cost = 0;

                                /**
                                 * Ha ellenfél térfele és üres, nem elönyös az elöző eset jobb ne adogassunk csak ugy oda bábukat
                                 */
                                if (board[i2][j2] == MCGame.empty && !samePart) cost = -1;

                                /**
                                 * Ha nem üres és ellen fél területe az már jó, de ezt is rakjuk hassznossági sorrendebe
                                 * azért van a végén + 5 mert ekkor pozitiv marad  és ez jobb eset mintha a pl fentebbiek valamelyike
                                 *
                                  */

                                if (board[i2][j2] != MCGame.empty && !samePart) cost = board[i2][j2] - board[i][j] + 5;

                                /**
                                 * A ellenfél mit lépne jelen pillanatba
                                 */
                                greedyStep = greedyAction(board, action, color, myScore, enemyScore);


                                /**
                                 * Ahova lépne a ellenfél azaz ütne ha ezt meglépjük
                                 */
                                if (greedyStep.x2 != i2 && greedyStep.y2 != j2)
                                    actions.add(new ActionWithScore(action, cost));
                                /**
                                 * Menekülni kell mert az ellenfél le fogja ütni ezért magasabb prioritást élvez
                                 */
                                if (greedyStep.x2 == i && greedyStep.y2 == j)
                                    cost=10;

                                emergency.add(new ActionWithScore(action,cost));

                            }
                        }
                    }
                }
            }
        }

        /**
         *Meg vannak a lehetséges lépések, válogasssunk köztük
         *Alapállapot felálítása
         *
         */
        MCAction tmp;
        MCAction action = null;


        /**
         * Lehetőleg olyat lépjünk ami az  ellen felé halad, ne legyen olyan eset, hogy a tábla távolabbi felébe ide-oda
         * lépked és végtelen játékot hoz létre
         * így nagyobb eséllyelé lesz ütés ha a határ közelébe lépked
         * ez föként a végjátékban van fontos szerepe
         */
        if (actions.size() > 0) {
            while (actions.size() > 0 && action == null) {
                tmp = actions.poll().getAction();
                if ((tmp.x1 < tmp.x2 && color == 0) || (tmp.x1 > tmp.x2 && color == 1))
                    action = tmp;
            }
        }

        /**
         * Ha olyan helyzet alakul ki hogy 1 lépésünk van, de ezt a lépést töröljük mert az ellenfél leütné akkor fennálhat
         * hogy nincs az actions-ban lépés, az emergencybe mindig van lépés és nincs olyan eset hogy nem lépünk
         *
         */
        if (action == null || emergency.peek().score > 0) {
            action = emergency.poll().getAction();
        }


        if (maxScore == 0 && board[action.x2][action.y2] != MCGame.empty) {
            board[action.x2][action.y2] += board[action.x1][action.y1];
        } else {
            board[action.x2][action.y2] = board[action.x1][action.y1];
        }
        board[action.x1][action.y1] = MCGame.empty;
        myScore += maxScore;

        return action;
    }



    /**
     * A Greedy Player várható lépésével tér vissza, ha le tud ütni akkor 100% pontos különben biznytalan
     *
     * @param board         Éppen játszott tábla
     * @param MyAction      Amit lépni szeretnék
     * @param color         MElyik szinnel játszok
     * @param sscore        Játékban aktuális pontjaim száma
     * @param greedyscore   Ellen fél pontjai
     * @return Ellenfél váthaó lépése MCAction formában
     */

    private MCAction greedyAction(int[][] board, MCAction MyAction, int color, int sscore, int greedyscore) {
        // Szin megfordítás
        int calor = color == 1 ? 0 : 1;
        int prevScore = 0;

        // Tábla mosolás, ne az eredeti táblán lépjen
        int[][] bord = new int[board.length][];
        for (int i = 0; i < board.length; ++i) {
            bord[i] = new int[board[i].length];
            for (int j = 0; j < board[i].length; ++j) {
                bord[i][j] = board[i][j];
            }
        }

        MCAction prevAction = MyAction;
        boolean samePart = (prevAction.x1 < 4 && prevAction.x2 < 4) || (4 <= prevAction.x1 && 4 <= prevAction.x2);
        if (samePart) {
            // merge / move
            bord[prevAction.x2][prevAction.y2] += bord[prevAction.x1][prevAction.y1];
        } else {
            // capture / move
            prevScore = bord[prevAction.x2][prevAction.y2];
            bord[prevAction.x2][prevAction.y2] = bord[prevAction.x1][prevAction.y1];
        }
        bord[prevAction.x1][prevAction.y1] = MCGame.empty;

        greedyscore += prevScore;
        int maxiScore = 0;
        List<MCAction> aactions = new LinkedList<MCAction>();
        // generate actions
        for (int i = 4 * calor; i < 4 * (calor + 1); i++) {
            for (int j = 0; j < 4; j++) {
                if (bord[i][j] != MCGame.empty) {
                    for (int i2 = 0; i2 < 8; i2++) {
                        for (int j2 = 0; j2 < 4; j2++) {
                            MCAction aaction = new MCAction(i, j, i2, j2);
                            int scorw = -1;
                            try {
                                scorw = score(bord, prevAction, prevScore, calor, aaction, greedyscore - sscore);
                            } catch (Exception e) {
                                System.out.println("ACTION: " + aaction);
                                e.printStackTrace();
                                System.exit(1);
                            }
                            if (maxiScore < scorw) {
                                maxiScore = scorw;
                                aactions.clear();
                            }
                            if (maxiScore == scorw) {
                                aactions.add(aaction);
                            }
                        }
                    }
                }
            }
        }
        MCAction saction = aactions.size() == 0 ? null : aactions.get(r.nextInt(aactions.size()));
        return saction;
    }

    /**
     * Returns adott lépéshez kiszámítja a pontot valamint -1 ha helytelen a lépés
     *
     * @param board      aktuális tábla
     * @param prevAction elöző lépés
     * @param prevScore  elöző lépés pontja
     * @param color      aktuális játékos színe
     * @param action     aktuális játékos lépése
     * @param scoreDiff  saját és a ellenfél pont külnbsége
     * @return lépés pontja
     */
    private int score(int[][] board, MCAction prevAction, int prevScore, int color, MCAction action, int scoreDiff) {
        int[][] figures = new int[2][3];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != MCGame.empty) {
                    if (i < board.length / 2) {
                        figures[0][board[i][j] - 1]++;
                    } else {
                        figures[1][board[i][j] - 1]++;
                    }
                }
            }
        }
        int numFigures = figures[color][0] + figures[color][1] + figures[color][2];
        int score = MCGame.score(board, figures, prevAction, prevScore, color, action);
        boolean samePart = (action.x1 < 4 && action.x2 < 4) || (4 <= action.x1 && 4 <= action.x2);
        if (0 <= score && !samePart && numFigures == 1) {
            if (scoreDiff < 0) {
                score = -1;
            } else {
                score = 10;
            }
        }
        return score;
    }

}
