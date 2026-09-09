package jdbc.daos;

/**
 *
 * @author HM
 */
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdbc.DBConnecter;
import jdbc.NDVResultSet;
import org.json.simple.JSONArray;
import player.Inventory;
import player.Player;
import utils.Util;

public class SuperRankDAO {

    public static int getHighestRank() {
        try {
            NDVResultSet rs = DBConnecter.executeQuery("SELECT rank FROM player ORDER BY rank DESC LIMIT 1");
            if (rs.next()) {
                return rs.getInt("rank");
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public static List<Long> getPlayerListInRankRange(int rank, int limit) {

        List<Long> list = new ArrayList<>();
        try {
            NDVResultSet rs = DBConnecter.executeQuery("SELECT id FROM player WHERE rank <= ? AND rank > 0 ORDER BY rank DESC LIMIT ?", rank, limit);
            while (rs.next()) {
                list.add((long) rs.getInt("id"));
            }
        } catch (Exception e) {
        }
        try {
            int rand = random(rank);
            if (rand != -1) {
                NDVResultSet rs = DBConnecter.executeQuery("SELECT id FROM player WHERE rank = ? LIMIT 1", rand);
                if (rs.next()) {
                    list.add((long) rs.getInt("id"));
                }
            }
        } catch (Exception e) {
        }
        Collections.reverse(list);
        return list;
    }

    public static List<Long> getPlayerListInRank(int rank, int limit) {
        List<Long> list = new ArrayList<>();
        try {
            NDVResultSet rs = DBConnecter.executeQuery("SELECT id FROM player WHERE rank > 0 ORDER BY rank ASC LIMIT ?", limit);
            while (rs.next()) {
                list.add((long) rs.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (rank > 100) {
                NDVResultSet rs = DBConnecter.executeQuery("SELECT * FROM player WHERE rank > ? AND rank < ? ORDER BY rank ASC LIMIT 4", rank - 3, rank + 2);
                while (rs.next()) {
                    list.add((long) rs.getInt("id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int random(int rank) {
        if (rank > 10000) {
            return Util.nextInt(6666, 10000);
        } else if (rank > 6666) {
            return Util.nextInt(3333, 6666);
        } else if (rank > 3333) {
            return Util.nextInt(1000, 3333);
        } else if (rank > 1000) {
            return Util.nextInt(666, 1000);
        } else if (rank > 666) {
            return Util.nextInt(333, 666);
        } else if (rank > 333) {
            return Util.nextInt(100, 333);
        }
        return -1;
    }

    public static void updateRank(Player player) {
        try {
            String query = "UPDATE player SET rank = ? WHERE id = ?";
            DBConnecter.executeUpdate(query, player.superRank.rank, player.id);
        } catch (Exception e) {
        }
    }

    public static void updatePlayer(Player player) {
        if (player != null && player.iDMark.isLoadedAllDataPlayer()) {
            try {
                JSONArray dataArray = new JSONArray();

                //data kim lượng
                dataArray.add(player.inventory.gold > Inventory.LIMIT_GOLD
                        ? Inventory.LIMIT_GOLD : player.inventory.gold);
                dataArray.add(player.inventory.gem);
                dataArray.add(player.inventory.ruby);
                dataArray.add(player.inventory.coupon);
                dataArray.add(player.inventory.event);
                String inventory = dataArray.toJSONString();
                dataArray.clear();

                //data super rank
                dataArray.add(player.superRank.lastTimePK);
                dataArray.add(player.superRank.lastTimeReward);
                dataArray.add(player.superRank.ticket);
                dataArray.add(player.superRank.win);
                dataArray.add(player.superRank.lose);
                JsonObject jsonObject = new JsonObject();
                JsonArray stringArray = new JsonArray();
                for (String str : player.superRank.history) {
                    stringArray.add(str);
                }
                JsonArray longArray = new JsonArray();
                for (Long value : player.superRank.lastTime) {
                    longArray.add(value);
                }
                jsonObject.add("history", stringArray);
                jsonObject.add("lasttime", longArray);
                String jsonString = new Gson().toJson(jsonObject);
                dataArray.add(jsonString);
                String dataSuperRank = dataArray.toJSONString();
                dataArray.clear();

                String query = "UPDATE player SET data_inventory = ?, rank = ?, data_super_rank = ? WHERE id = ?";
                DBConnecter.executeUpdate(query, inventory, player.superRank.rank, dataSuperRank, player.id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
