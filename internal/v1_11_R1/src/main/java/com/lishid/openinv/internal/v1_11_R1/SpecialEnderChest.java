/*
 * Copyright (C) 2011-2018 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_11_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import com.lishid.openinv.internal.ISpecialEnderChest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.IInventory;
import net.minecraft.server.v1_11_R1.InventoryEnderChest;
import net.minecraft.server.v1_11_R1.InventorySubcontainer;
import net.minecraft.server.v1_11_R1.ItemStack;

import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventory;

public class SpecialEnderChest extends InventorySubcontainer implements IInventory, ISpecialEnderChest {

    private final InventoryEnderChest enderChest;
    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;

    public SpecialEnderChest(Player player, Boolean online) {
        super(PlayerDataManager.getHandle(player).getEnderChest().getName(),
                PlayerDataManager.getHandle(player).getEnderChest().hasCustomName(),
                PlayerDataManager.getHandle(player).getEnderChest().getSize());
        this.playerOnline = online;
        EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
        this.enderChest = nmsPlayer.getEnderChest();
        this.bukkitOwner = nmsPlayer.getBukkitEntity();
        setItemLists(this, enderChest.getContents());
    }

    private void setItemLists(InventorySubcontainer subcontainer, List<ItemStack> list) {
        try {
            // Prepare to remove final modifier
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            // Access and replace main inventory array
            Field field = InventorySubcontainer.class.getField("items");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(subcontainer, list);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public void setPlayerOnline(Player player) {
        if (!playerOnline) {
            try {
                EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
                this.bukkitOwner = nmsPlayer.getBukkitEntity();
                setItemLists(nmsPlayer.getEnderChest(), this.items);
            } catch (Exception e) {}
            playerOnline = true;
        }
    }

    @Override
    public void setPlayerOffline() {
        playerOnline = false;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void update() {
        super.update();
        enderChest.update();
    }

}
