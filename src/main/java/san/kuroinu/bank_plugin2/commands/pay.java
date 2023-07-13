package san.kuroinu.bank_plugin2.commands;

import jdk.internal.net.http.common.Pair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import san.kuroinu.bank_plugin2.Bank_Controll;

import java.util.ArrayList;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class pay implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(prefix+ ChatColor.RED+"プレイヤーから実行してください");
        }
        Player p = (Player) sender;
        if (args.length < 2){
            pay_help(p);
            return true;
        }
        //プレイヤーが来たことがあるか
        if (!plugin.getServer().getOfflinePlayer(args[0]).hasPlayedBefore()){
            p.sendMessage(prefix+ ChatColor.RED+"指定したプレイヤーは存在しません");
            return true;
        }
        //数字かどうか
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        }catch (NumberFormatException e){
            p.sendMessage(prefix+ ChatColor.RED+"金額は数字で入力してください");
            return true;
        }
        //1円以上か
        if (amount < 1){
            p.sendMessage(prefix+ ChatColor.RED+"1円以上の金額を入力してください");
            return true;
        }
        //お金があるか
        if (econ.getBalance(p) < amount){
            p.sendMessage(prefix+ ChatColor.RED+"お財布のお金が足りません");
            return true;
        }
        //クールダウンの人か
        ArrayList<String> pair = new ArrayList<>();
        pair.add(p.getName());
        pair.add(args[0]);
        if (!pay_cooltime_players.contains(pair)){
            sender.sendMessage(prefix+ ChatColor.BOLD+ "=====[送金]=====");
            sender.sendMessage(prefix+ ChatColor.BOLD+"送る相手:"+args[0]);
            sender.sendMessage(prefix+ ChatColor.BOLD+"金額:"+amount);
            sender.sendMessage(prefix+ ChatColor.BOLD+"送金するにはもう一度送金コマンドを実行してください");
            pay_cooltime_players.add(pair);
            new Thread(()->{
                try {
                    Thread.sleep(1000*60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (pay_cooltime_players.contains(pair)){
                    pay_cooltime_players.remove(pair);
                }
            }).start();
            return true;
        }
        pay_cooltime_players.remove(pair);
        //お金を引く
        econ.withdrawPlayer(p, amount);
        //お金を入れる
        Bank_Controll bc = new Bank_Controll();
        bc.e = plugin.getServer().getOfflinePlayer(args[0]);
        bc.deposit_bank(amount);
        p.sendMessage(prefix+ ChatColor.GREEN +amount+"円を"+args[0]+"に送金しました");
        if (bc.e.isOnline()) bc.e.getPlayer().sendMessage(prefix+ ChatColor.YELLOW +p.getName()+"から"+amount+"円が銀行に入金されました!");
        return true;
    }

    void pay_help(Player e){
        e.sendMessage(prefix+"§a/pay <プレイヤー名> <金額> : 指定したプレイヤーにお金を送ります");
    }
}
