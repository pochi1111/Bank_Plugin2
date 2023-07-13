package san.kuroinu.bank_plugin2.commands;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class atm implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(prefix+ ChatColor.RED+"プレイヤーから実行してください");
            return true;
        }
        Player e = (Player) sender;
        if (args.length == 0){
            //atmの選択画面を開く
            Inventory inv = plugin.getServer().createInventory(null, 9, ChatColor.BOLD+"ATM");
            for (int i = 0; i < 4; i++) {
                inv.setItem(i,createGUIitem(Material.HOPPER, ChatColor.BOLD+"お金を入れる", ChatColor.GRAY+"お金を入れます",1));
            }
            for (int i = 5; i < 9; i++) {
                inv.setItem(i,createGUIitem(Material.CHEST, ChatColor.BOLD+"お金を出す", ChatColor.GRAY+"お金を出します",1));
            }
            e.openInventory(inv);
            atm_select_players.add(e.getName());
            return true;
        }
        if (args[0].equals("help")){
            atmhelp(e);
        }
        if (args[0].equals("in")){
            //お財布に入れるインベントリを表示
            Inventory inv = plugin.getServer().createInventory(null, 27, prefix+"現金を入れてください");
            e.openInventory(inv);
            atm_open_players.add(e.getName());
        }else if (args[0].equals("out")){
            if (args.length >= 3){
                //2コメと3こめが整数かどうか
                int money;
                int amount;
                try{
                    money = Integer.parseInt(args[1]);
                    amount = Integer.parseInt(args[2]);
                }catch (NumberFormatException ex){
                    e.sendMessage(prefix + "金額と個数は整数で入力してください");
                    return true;
                }
                //money,amountが1以上か
                if (money <= 1 || amount <= 1){
                    e.sendMessage(prefix + "金額と個数は1以上で入力してください");
                    return true;
                }
                //money*amount以上のお金を持っているか
                int balance = (int) econ.getBalance(e);
                if (balance < money*amount){
                    e.sendMessage(prefix + "お金が足りません");
                    return true;
                }else{
                    //インベントリに空きがあるかどうか
                    Inventory inv = e.getInventory();
                    int empty = 0;
                    for (int i = 0; i < inv.getSize(); i++){
                        if (inv.getItem(i) == null){
                            empty++;
                        }
                    }
                    int tmp = amount;
                    if (amount%64 == 0){
                        amount /= 64;
                    }else{
                        amount = amount/64 + 1;
                    }
                    if (empty < amount) {
                        e.sendMessage(prefix + "インベントリに空きが足りません");
                        return true;
                    }
                    //お金をインベントリに入れる
                    ItemStack wallet = createwallet(tmp, money);
                    inv.addItem(wallet);
                    econ.withdrawPlayer(e, money*tmp);
                }
            }else if (args.length == 1){
                //お金を出すGUIを表示させる
                Inventory inv = plugin.getServer().createInventory(null, 27, prefix+"現金を出してください");
                for (int i = 0; i < 9; i++) {
                    inv.setItem(i,createGUIitem(Material.GRAY_STAINED_GLASS, ChatColor.BOLD+"お金を出す", ChatColor.GRAY+"左クリックで1個、shiftクリックで16個",1));
                }
                for (int i = 18; i < 27; i++) {
                    inv.setItem(i,createGUIitem(Material.GRAY_STAINED_GLASS, ChatColor.BOLD+"お金を出す", ChatColor.GRAY+"./atm helpで詳しい出し方",1));
                }
                inv.setItem(9,createwallet(1, 100));
                inv.setItem(10,createwallet(1, 500));
                inv.setItem(11,createwallet(1, 1000));
                inv.setItem(12,createwallet(1, 5000));
                inv.setItem(13,createwallet(1, 10000));
                inv.setItem(14,createwallet(1, 50000));
                inv.setItem(15,createwallet(1, 100000));
                inv.setItem(16,createwallet(1, 500000));
                inv.setItem(17,createwallet(1, 1000000));
                e.openInventory(inv);
                atm_out_players.add(e.getName());
            }else{
                atmhelp(e);
            }
        }else{
            atmhelp(e);
        }
        return true;
    }

    boolean atmhelp(Player e){
        e.sendMessage(prefix + "/atm ・・・atmを開きます");
        e.sendMessage(prefix + "/atm in・・・お財布に入金します");
        e.sendMessage(prefix + "/atm out <金額> <個数> ・・・お財布から現金を取り出します");
        return true;
    }

    ItemStack createwallet(int amount, int money){
        //100円以下なら黄色の染料、1000円以下なら鉄、１００００まんえん以下ならダイヤ、それ以上ならエメラルド
        Material material;
        if (money <= 100){
            material = Material.YELLOW_DYE;
        }else if (money <= 1000){
            material = Material.IRON_INGOT;
        }else if (money <= 10000){
            material = Material.DIAMOND;
        }else{
            material = Material.EMERALD;
        }
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "お金");
        meta.setLore(Collections.singletonList(money+"円"));
        meta.setCustomModelData(plugin.getConfig().getInt("wallet_cmd"));
        //NBTに金額を入れる
        meta.getPersistentDataContainer().set(money_key, PersistentDataType.INTEGER, money);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("atm")){
            if (args.length == 1) {
                if (args[0].length() == 0) {
                    return Arrays.asList("in","out","help");
                } else {
                    if ("in".startsWith(args[0]) && "out".startsWith(args[0]) && "help".startsWith(args[0])) {
                        return Arrays.asList("in","out","help");
                    }
                    else if("in".startsWith(args[0])){
                        return Collections.singletonList("in");
                    }
                    else if("out".startsWith(args[0])){
                        return Collections.singletonList("out");
                    }
                    else if("help".startsWith(args[0])){
                        return Collections.singletonList("help");
                    }
                }
            }
        }
        return null;
    }
}
