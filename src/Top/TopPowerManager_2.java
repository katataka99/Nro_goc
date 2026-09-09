package Top;

import item.Item;
import java.math.BigInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jdbc.DBConnecter;
import network.Message;
import player.Player;
import services.ItemService;

public class TopPowerManager_2 {

    public static void showTopPower(Player player) {
        String sqlQuery = """
            SELECT id, name, gender, items_body, 
            CAST(JSON_EXTRACT(data_point, '$[1]') AS UNSIGNED) AS value 
            FROM player 
            ORDER BY value DESC 
            LIMIT 100
        """;

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement(
                sqlQuery,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        ); ResultSet rs = ps.executeQuery()) {

            Message msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100 Sức Mạnh");

            int count = 0;
            while (rs.next()) {
                count++;
            }
            msg.writer().writeByte(count);

            rs.beforeFirst();
            int rank = 1;

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte gender = rs.getByte("gender");
                String valueStr = rs.getString("value");
                BigInteger value = new BigInteger(valueStr);

                short head = (short) (gender == 2 ? 28 : (gender == 1 ? 32 : 64));
                short body = (short) (gender == 2 ? 16 : (gender == 1 ? 10 : 14));
                short leg = (short) (gender == 2 ? 17 : (gender == 1 ? 11 : 15));

                String itemsBodyJson = rs.getString("items_body");
                if (itemsBodyJson != null && !itemsBodyJson.isEmpty()) {
                    JSONArray dataArray = (JSONArray) JSONValue.parse(itemsBodyJson);
                    if (dataArray != null) {
                        for (int i = 0; i < dataArray.size() && i < 6; i++) {
                            JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                            if (dataItem != null && dataItem.get(0) != null) {
                                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                                if (tempId != -1) {
                                    Item item = ItemService.gI().createNewItem(tempId,
                                            Integer.parseInt(String.valueOf(dataItem.get(1))));
                                    if (item.template.head != -1) {
                                        head = (short) item.template.head;
                                    }
                                    if (item.template.body != -1) {
                                        body = (short) item.template.body;
                                    }
                                    if (item.template.leg != -1) {
                                        leg = (short) item.template.leg;
                                    }
                                }
                            }
                        }
                    }
                }

                msg.writer().writeInt(rank++);
                msg.writer().writeInt(id);
                msg.writer().writeShort(head);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(body);
                msg.writer().writeShort(leg);
                msg.writer().writeUTF(name);
                msg.writer().writeUTF(Util.numberToMoneyTuanBinh(value) + " Sức mạnh");
                msg.writer().writeUTF("...");
            }

            player.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
