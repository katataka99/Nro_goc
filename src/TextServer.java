/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Administrator
 */
public class TextServer {
     public static final String TOP_HP = "SELECT name, gender, items_body, CAST( split_str(data_point,',',6) AS UNSIGNED) AS hp FROM player INNER JOIN account ON account.id = player.account_id WHERE account.is_admin = 0 AND account.ban = 0 ORDER BY CAST( split_str(data_point,',',6)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String TOP_KI = "SELECT name, gender, items_body, CAST( split_str(data_point,',',7) AS UNSIGNED) AS ki FROM player INNER JOIN account ON account.id = player.account_id WHERE account.is_admin = 0 AND account.ban = 0 ORDER BY CAST( split_str(data_point,',',7)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String TOP_NV = "SELECT name, gender, items_body, CAST( JSON_EXTRACT(data_task, '$[0]') AS UNSIGNED) AS nv, CAST( JSON_EXTRACT(data_task, '$[1]') AS UNSIGNED) AS subnv, CAST( JSON_EXTRACT(data_task, '$[3]') AS UNSIGNED) AS lasttime FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 ORDER BY CAST( JSON_EXTRACT(data_task, '$[0]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_task, '$[1]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_task, '$[2]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_task, '$[3]') AS UNSIGNED) ASC LIMIT 20;";
    public static final String TOP_WHIS = "SELECT name, player.id, gender, items_body, CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) AS top, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) AS time, CAST( JSON_EXTRACT(data_luyentap, '$[7]') AS UNSIGNED) AS lasttime FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 AND CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) > 0 ORDER BY CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) ASC LIMIT 20;";
    public static final String TOP_3_WHIS = "SELECT name, id, gender, items_body, CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) AS top, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) AS time, CAST( JSON_EXTRACT(data_luyentap, '$[7]') AS UNSIGNED) AS lasttime FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 AND CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) > 0 ORDER BY CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) ASC LIMIT 3;";
    public static final String TOP_NAP = "SELECT name, gender, items_body,CAST( cash AS UNSIGNED) AS cash FROM account, player WHERE account.id = player.account_id ORDER BY cash DESC LIMIT 20;";

}
