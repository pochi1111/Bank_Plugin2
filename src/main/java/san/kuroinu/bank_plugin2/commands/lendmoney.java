package san.kuroinu.bank_plugin2.commands;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.econ;
import static san.kuroinu.bank_plugin2.Bank_Plugin2.prefix;

public class lendmoney implements CommandExecutor {
    /*
    * お金を貸すコマンド
    * /lendmoney <プレイヤー名> <金額> <期間> <金利>
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 4){
            help_lendmoney((Player) sender);
            return true;
        }
        Player e = (Player) sender;
        String check = check_args(args, e);
        if (!check.equals("ok")){
            e.sendMessage(check);
            return true;
        }
        Player target = e.getServer().getPlayer(args[0]);
        //相手に提示
        target.sendMessage(ChatColor.GRAY+"==========借金の提案==========");
        target.sendMessage(ChatColor.GRAY+"借りる金額: "+ChatColor.GREEN+args[1]+"円");
        target.sendMessage(ChatColor.GRAY+"借りる期間: "+ChatColor.GREEN+args[2]+"日");
        target.sendMessage(ChatColor.GRAY+"金利: "+ChatColor.GREEN+args[3]+"%");
        //借りるボタンを作成

        target.sendMessage(ChatColor.GRAY+"==============================");
        return true;
    }

    void help_lendmoney(Player e){
        e.sendMessage(prefix+"§a/lendmoney <プレイヤー名> <金額> <期間> <金利>");
    }

    String check_args(String[] args, Player e){
        //金額と期間が整数かどうか
        int money;
        int lend_day;
        try{
            money = Integer.parseInt(args[1]);
            lend_day = Integer.parseInt(args[2]);
        }catch (NumberFormatException ex){
            return prefix+"§c金利と期間は数字を入力してください";
        }
        //金利が小数かどうか
        double interest;
        try{
            interest = Double.parseDouble(args[3]);
        }catch (NumberFormatException ex){
            return prefix+"§c金利は小数を入力してください";
        }
        //お金がお財布にあるかどうか
        if (econ.getBalance(e)<money){
            return prefix+"§cお財布にお金が足りません";
        }
        //そのプレイヤーがいるかどうか
        OfflinePlayer target = e.getServer().getOfflinePlayer(args[0]);
        if (!target.isOnline()){
            return prefix+"§cそのプレイヤーはオフラインです";
        }
        if (args[0].equals(e.getName())){
            return prefix+"§c自分にお金を貸すことはできません";
        }
        if (interest < 0){
            return prefix+"§c金利は0以上にしてください";
        }
        if (lend_day < 1){
            return prefix+"§c期間は1以上にしてください";
        }
        if (money < 1){
            return prefix+"§c金額は1以上にしてください";
        }
        if (interest > 10){
            return prefix+"§c金利は10以下にしてください";
        }
        return "ok";
    }
}
