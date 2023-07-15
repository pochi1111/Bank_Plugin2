package san.kuroinu.bank_plugin2.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class lendmoney implements CommandExecutor {
    /*
    * お金を貸すコマンド
    * /lendmoney <プレイヤー名> <金額> <期間> <金利>
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //プレイヤーのみ
        if (!(sender instanceof Player)){
            sender.sendMessage(prefix+ ChatColor.RED+"プレイヤーから実行してください");
            return true;
        }
        if (args.length != 0){
            if (args[0] == "accept"){
                //借りる処理
                //あるかどうか
                for (ArrayList<String> list : lend_wait_players){
                    if (list.get(1).equals(((Player) sender).getName())) {
                        //借りる処理
                        //相手のお金があるか
                        if (econ.getBalance(plugin.getServer().getPlayer(list.get(0))) < Integer.parseInt(list.get(2))) {
                            sender.sendMessage(prefix + ChatColor.RED + "相手のお金が足りません");
                            return true;
                        }
                        AtomicInteger id = new AtomicInteger();
                        //借りる
                        econ.withdrawPlayer(plugin.getServer().getPlayer(list.get(0)), Integer.parseInt(list.get(2)));
                        econ.depositPlayer((Player) sender, Integer.parseInt(list.get(2)));
                        //借した人にメッセージ
                        plugin.getServer().getPlayer(list.get(0)).sendMessage(prefix + ChatColor.YELLOW + ((Player) sender).getName() + "さんに" + list.get(2) + "円を借しました");
                        //借りた人にメッセージ
                        ((Player) sender).sendMessage(prefix + ChatColor.YELLOW + list.get(0) + "さんから" + list.get(2) + "円を借りました");
                        //SQL処理
                        new Thread(()->{
                            try {
                                Connection con = ds.getConnection();
                                //今最大のidを調べる
                                PreparedStatement pstmt = con.prepareStatement("select max(id) from debt_db");
                                ResultSet rs = pstmt.executeQuery();
                                id.set(0);
                                while (rs.next()){
                                    id.set(rs.getInt("max(id)"));
                                }
                                pstmt = con.prepareStatement("insert into debt_db (debt_amount, lender_uuid, debtor_uuid, debt_term,id) values (?,?,?,?)");
                                pstmt.setInt(1, Integer.parseInt(list.get(2)));
                                pstmt.setString(2, plugin.getServer().getPlayer(list.get(0)).getUniqueId().toString());
                                pstmt.setString(3, ((Player) sender).getUniqueId().toString());
                                //↓は今日+借りる期間
                                pstmt.setString(4, LocalDate.now().plusDays(Integer.parseInt(list.get(3))).toString());
                                pstmt.setInt(5, id.get() +1);
                                pstmt.executeUpdate();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                        //リストから削除
                        lend_wait_players.remove(list);
                        //相手に約束手形を渡す
                        ItemStack item = new ItemStack(Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(ChatColor.GOLD+"約束手形");
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY+"借りた人: "+ChatColor.GREEN+((Player) sender).getName());
                        lore.add(ChatColor.GRAY+"借りた金額: "+ChatColor.GREEN+list.get(2)+"円");
                        lore.add(ChatColor.GRAY+"借りた期間: "+ChatColor.GREEN+list.get(3)+"日");
                        lore.add(ChatColor.GRAY+"借りた日: "+ChatColor.GREEN+LocalDate.now().toString());
                        lore.add(ChatColor.GRAY+"借りた時の金利: "+ChatColor.GREEN+list.get(4)+"%");
                        meta.setLore(lore);
                        meta.getPersistentDataContainer().set(lend_key, PersistentDataType.INTEGER, id.intValue());
                        item.setItemMeta(meta);
                        plugin.getServer().getPlayer(list.get(0)).getInventory().addItem(item);
                        return true;
                    }
                }
                return true;
            }
        }
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
        //自分がいま提案をしているかどうか
        for (ArrayList<String> list : lend_wait_players){
            if (list.get(0).equals(e.getName())){
                e.sendMessage(prefix+ ChatColor.RED+"あなたは現在提案中です");
                return true;
            }
        }
        Player target = e.getServer().getPlayer(args[0]);
        //相手に提示
        target.sendMessage(ChatColor.GRAY+"==========借金の提案==========");
        target.sendMessage(ChatColor.GRAY+"借りる金額: "+ChatColor.GREEN+args[1]+"円");
        target.sendMessage(ChatColor.GRAY+"借りる期間: "+ChatColor.GREEN+args[2]+"日");
        target.sendMessage(ChatColor.GRAY+"金利: "+ChatColor.GREEN+args[3]+"%");
        //借りるボタンを作成
        target.sendMessage(Component.text(ChatColor.GRAY+"借りる").clickEvent(ClickEvent.runCommand("/lendmoney accept "+args[0])));
        target.sendMessage(ChatColor.GRAY+"==============================");
        ArrayList<String> list = new ArrayList<>();
        //借りる人、借りる金額、借りる期間
        list.add(args[0]);
        double interest = Double.parseDouble(args[3]);
        int money = Integer.parseInt(args[1]);
        int harau = (int) (money * (1 + (interest / 100)));
        list.add(harau+"");
        list.add(args[2]);
        //借りる人のリストに追加
        lend_wait_players.add(list);
        //1分末
        e.sendMessage(prefix+"§a借金の提案をしました");
        new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            if (lend_wait_players.contains(list)){
                lend_wait_players.remove(list);
                e.sendMessage(prefix+"§c借金の提案が受け入れられませんでした");
            }
        }).start();
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
