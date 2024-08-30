package com.thegreatchicken.TGCPlugin.inventory.enchantments;

import java.lang.reflect.Field;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry.SimpleRegistry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import com.thegreatchicken.TGCPlugin.PluginLoader;

public class GlowEnchantment extends Enchantment {
	
	public static void registerEnchantment () {
		try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		try {
			GlowEnchantment glow = new GlowEnchantment();
            
			System.out.println(org.bukkit.Registry.ENCHANTMENT);
        }
        catch (IllegalArgumentException e){
        }
        catch(Exception e){
            e.printStackTrace();
        }
	}

	public static final NamespacedKey KEY = new NamespacedKey(PluginLoader.PLUGIN, "glow_enchant");
	  public GlowEnchantment() {
	      super();
	  }

	  @Override
	  public boolean canEnchantItem(ItemStack arg0) {
	      return false;
	  }

	  @Override
	  public boolean conflictsWith(Enchantment arg0) {
	      return false;
	  }

	  @Override
	  public EnchantmentTarget getItemTarget() {
	      return null;
	  }

	  @Override
	  public int getMaxLevel() {
	      return 0;
	  }

	  @Override
	  public String getName() {
	      return null;
	  }

	  @Override
	  public int getStartLevel() {
	      return 0;
	  }

	@Override
	public boolean isTreasure() {
		return false;
	}

	@Override
	public boolean isCursed() {
		return false;
	}

	@Override
	public NamespacedKey getKey() {
		return KEY;
	}

	@Override
	public String getTranslationKey() {
		return KEY.getKey();
	}

}