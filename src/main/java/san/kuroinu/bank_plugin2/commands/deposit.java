package san.kuroinu.bank_plugin2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import san.kuroinu.bank_plugin2.Bank_Controll;

import java.sql.Connection;
import java.sql.SQLException;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class deposit implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(prefix+"§cプレイヤーから実行してください");
            return true;
        }
        if (args.length==0){
            help_deposit((Player) sender);
            return true;
        }
        int money;
        try {
            money = Integer.parseInt(args[0]);
        }catch (NumberFormatException e){
            sender.sendMessage(prefix+"§c数字を入力してください");
            return true;
        }
        Player e  = (Player) sender;
        //お金がお財布にあるかどうか
        if (econ.getBalance(e)<money){
            e.sendMessage(prefix+"§cお財布にお金が足りません");
            return true;
        }
        //お金を移動
        econ.withdrawPlayer(e, money);
        //銀行にお金を入れる
        new Thread(()->{
            Bank_Controll bs = new Bank_Controll();
            bs.e = (Player) sender;
            bs.deposit_bank(money);
            e.sendMessage(prefix+"§a"+money+"円を銀行に預けました");
        }).start();

        return true;
    }
    void help_deposit(Player e){
        e.sendMessage(prefix+"§a/deposit <金額> §r: お財布のお金を銀行に預ける");
    }
}
