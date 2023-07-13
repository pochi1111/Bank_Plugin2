package san.kuroinu.bank_plugin2.commands;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import san.kuroinu.bank_plugin2.Bank_Controll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class bank implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0){
            if (args[0].equals("reload") && sender.isOp()){
                //pluginのconfigをリロード
                plugin.reloadConfig();
                db_config_relaod();
            }
        }
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED+"プレイヤーから実行してください");
            return true;
        }
        int saihu = 0;
        int bank_money;
        saihu = (int) econ.getBalance(sender.getName());
        //銀行のお金がいくらあるかを取得
        Bank_Controll bc = new Bank_Controll();
        bc.e = (Player) sender;
        bank_money = bc.bank_balance();
        sender.sendMessage(ChatColor.YELLOW+"====="+ChatColor.AQUA+sender.getName()+"の所持金"+ChatColor.YELLOW+"=====");
        sender.sendMessage("お財布: "+ChatColor.YELLOW+saihu+"円");
        sender.sendMessage("銀行: "+ChatColor.YELLOW+bank_money+"円");
        return true;
    }
}
