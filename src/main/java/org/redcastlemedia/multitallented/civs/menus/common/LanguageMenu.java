package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;

@CivsMenu(name = "language")
public class LanguageMenu extends CustomMenu {
    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("languages")) {
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            String[] languages = new String[LocaleManager.getInstance().getAllLanguages().size()];
            languages = LocaleManager.getInstance().getAllLanguages().toArray(languages);
            if (languages.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            String language = languages[startIndex + count];
            CVItem cvItem = CVItem.createCVItemFromString(ChatColor.stripColor(LocaleManager.getInstance()
                    .getTranslation(language, "icon")));
            String name = LocaleManager.getInstance().getTranslation(language, "name");
            if (name == null) {
                name = "Error";
            }
            cvItem.setDisplayName(ChatColor.stripColor(name));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(language);
            cvItem.setLore(lore);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if (actionString.equals("select-lang")) {
            String itemName = clickedItem.getItemMeta().getDisplayName();
            String langKey = clickedItem.getItemMeta().getLore().get(0);
            civilian.setLocale(langKey);
            CivilianManager.getInstance().saveCivilian(civilian);
            Player player = Bukkit.getPlayer(civilian.getUuid());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(langKey, "language-set").replace("$1", itemName));
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

        @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        int maxPage = (int) Math.ceil((double) LocaleManager.getInstance().getAllLanguages().size() /
                (double) itemsPerPage.get("languages"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }
}
