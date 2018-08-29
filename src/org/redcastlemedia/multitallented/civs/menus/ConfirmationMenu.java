package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfirmationMenu extends Menu {
    static String MENU_NAME = "CivConfirm";
    public ConfirmationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();
        String regionName = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        CivItem civItem = itemManager.getItemType(regionName);
        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        if (Menu.isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            if (civItem.getPrice() > 0 && (Civs.econ == null ||
                    !Civs.econ.has(player, civItem.getPrice()))) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "not-enough-money").replace("$1", civItem.getPrice() + ""));
                return;
            }

            clearHistory(civilian.getUuid());
            if (Civs.econ == null) {
                player.sendMessage(Civs.getPrefix() + " Econ plugin not enabled or hooked through Vault.");
                return;
            }
            Civs.econ.withdrawPlayer(player, civItem.getPrice());
            event.getWhoClicked().sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "item-bought")
                            .replace("$1", civItem.getDisplayName())
                            .replace("$2", Util.getNumberFormat(civItem.getPrice(), civilian.getLocale())));
            event.getWhoClicked().closeInventory();
            civilian.getStashItems().add(civItem);
            civilianManager.saveCivilian(civilian);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, CivItem civItem) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();

        inventory.setItem(0, civItem.clone().createItemStack());

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy").replace("$1", civItem.getDisplayName()));
        List<String> lore = new ArrayList<>();
        if (civItem.getPrice() > 0) {
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + civItem.getPrice());
        }
        cvItem.setLore(lore);
        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
