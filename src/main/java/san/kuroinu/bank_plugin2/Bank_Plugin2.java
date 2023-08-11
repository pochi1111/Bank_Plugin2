package san.kuroinu.bank_plugin2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jdk.internal.net.http.common.Pair;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import san.kuroinu.bank_plugin2.commands.*;
import san.kuroinu.bank_plugin2.listeners.atm_close;
import san.kuroinu.bank_plugin2.listeners.atm_out;
import san.kuroinu.bank_plugin2.listeners.atm_select;
import san.kuroinu.bank_plugin2.listeners.lend_collect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Bank_Plugin2 extends JavaPlugin {
    public static JavaPlugin plugin;
    public static HikariDataSource ds;
    public static String prefix = "§6[§abank§6]§r";
    public static ArrayList<String> atm_open_players = new ArrayList<>();
    public static ArrayList<String> atm_select_players = new ArrayList<>();
    public static ArrayList<String> atm_out_players = new ArrayList<>();
    public static Economy econ = null;
    public static NamespacedKey money_key;
    public static NamespacedKey lend_key;
    public static ArrayList<ArrayList<String>> pay_cooltime_players = new ArrayList<>();
    public static ArrayList<ArrayList<String>> lend_wait_players= new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        plugin.saveDefaultConfig();
        money_key = new NamespacedKey(plugin,"money");
        lend_key = new NamespacedKey(plugin,"lend");
        // vaultの導入確認
        if (!setupEconomy() ) {
            getServer().getConsoleSender().sendMessage("Vaultが導入されていません");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //コマンドを登録
        getCommand("atm").setExecutor(new atm());
        getCommand("deposit").setExecutor(new deposit());
        getCommand("withdraw").setExecutor(new withdraw());
        getCommand("bank").setExecutor(new bank());
        getCommand("pay").setExecutor(new pay());
        //getCommand("lendmoney").setExecutor(new lendmoney());
        //リスナーを登録
        getServer().getPluginManager().registerEvents(new atm_close(), this);
        getServer().getPluginManager().registerEvents(new atm_select(), this);
        getServer().getPluginManager().registerEvents(new atm_out(), this);
        //getServer().getPluginManager().registerEvents(new lend_collect(), this);
        //テーブルを作成
            // mysqlの設定
            HikariConfig conf = new HikariConfig();
            conf.setJdbcUrl(plugin.getConfig().getString("mysql.url"));
            conf.setUsername(plugin.getConfig().getString("mysql.user"));
            conf.setPassword(plugin.getConfig().getString("mysql.password"));
            ds = new HikariDataSource(conf);
            Connection con = null;
            try {
                con = ds.getConnection();
                //db_moneyテーブルがあるかどうか
                if (!con.prepareStatement("SHOW TABLES LIKE 'db_money'").executeQuery().next()){
                    //ないなら作成
                    con.prepareStatement("create table db_money (id    int auto_increment primary key,uuid  text null,money int  null)").executeUpdate();
                }
                //debt_dbがあるかどうか
                if (!con.prepareStatement("SHOW TABLES LIKE 'debt_db'").executeQuery().next()) {
                    con.prepareStatement("create table debt_db(id int auto_increment primary key,debt_amount int  null,lender_uuid text null comment '貸す人のuuid',debtor_uuid text null comment '借りる人のuuid',debt_term date null);").executeUpdate();
                }
                con.close();
                super.onEnable();
            } catch (SQLException e) {
                db_config_relaod();
                throw new RuntimeException(e);
            }
    }

    private static Boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (ds != null){
            ds.close();
        }
        super.onDisable();
    }

    public static ItemStack createGUIitem(Material mat, String name, String lore, int amount){
        ItemStack i = new ItemStack(mat, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(name);
        ArrayList<String> lore_list = new ArrayList<>();
        lore_list.add(lore);
        im.setLore(lore_list);
        i.setItemMeta(im);
        return i;
    }

    public static void db_config_relaod(){
        plugin.getServer().broadcastMessage(prefix+ ChatColor.RED+"データベースに接続できませんでした。再接続を試みます。");
        plugin.getServer().broadcastMessage(prefix+ChatColor.RED+"再接続が終了するまで、bankを利用しないでください");
        //logに流す
        plugin.getServer().getConsoleSender().sendMessage(prefix+ChatColor.RED+"データベースに接続できませんでした。再接続を試みます。");
        Thread t;
        t = new Thread (()->{
                if (ds != null) ds.close();
                HikariConfig conf = new HikariConfig();
                conf.setJdbcUrl(plugin.getConfig().getString("mysql.url"));
                conf.setUsername(plugin.getConfig().getString("mysql.user"));
                conf.setPassword(plugin.getConfig().getString("mysql.password"));
                ds = new HikariDataSource(conf);
        });
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }
}
