package san.kuroinu.bank_plugin2.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import san.kuroinu.bank_plugin2.Bank_Controll;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class withdraw implements CommandExecutor{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(prefix+ ChatColor.RED+"プレイヤーから実行してください");
            return true;
        }
        if (args.length==0){
            help_withdraw((Player) sender);
            return true;
        }
        int money;
        try {
            money = Integer.parseInt(args[0]);
        }catch (NumberFormatException e){
            sender.sendMessage(prefix+ChatColor.RED+"数字を入力してください");
            return true;
        }
        //1円以上か
        if (money<1){
            sender.sendMessage(prefix+ChatColor.RED+"1円以上を入力してください");
            return true;
        }
        Player e  = (Player) sender;
        //お金が銀行にあるかどうか
        new Thread(()->{
            Bank_Controll bs = new Bank_Controll();
            bs.e = (Player) sender;
            if (bs.withdraw_bank(money)) {
                e.sendMessage(prefix + ChatColor.YELLOW + money + "円を銀行から引き出しました");
                econ.depositPlayer(e, money);
            }else{
                e.sendMessage(prefix+ ChatColor.RED +"銀行にお金が足りません");
            }
        }).start();

        return true;
    }

    void help_withdraw(Player e){
        e.sendMessage(prefix+"§a/withdraw <金額> §r: 銀行からお財布にお金をおろす");
    }
}
