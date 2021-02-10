package me.thisorthat.spendfly.main;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class SpendFly extends JavaPlugin implements Listener  {
	static  ArrayList<UUID> currentTracking = new ArrayList<>();
	static ArrayList<UUID> fallProtection = new ArrayList<>();
	static Economy e = null;
	// static BukkitRunnable runner = null;

	FileConfiguration config;
	@Override
	public void onEnable() {
		
		
		saveResource("Config.yml", false);
		
		config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "Config.yml"));
		double cost = config.getDouble("costpersecond");
		
		setupEconomy();
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < currentTracking.size(); i++) {
					UUID id = currentTracking.get(i);
					Player p = Bukkit.getPlayer(id);
					if(p==null) {
						
						currentTracking.remove(id);
						
						
						continue;
					}
					
					if(p.isFlying()) {
						if(e.getBalance(p) <=cost) {
							p.setAllowFlight(false);
							p.setFlying(false);
							currentTracking.remove(id);
						} else {
					e.withdrawPlayer(p, cost);
					p.sendMessage("");
					p.sendMessage("Flying... Balance: $" + e.getBalance(p));
						}
					
					}
				}
			}
		}, 0, 20);
		
		

		Bukkit.getServer().getLogger().info("WORKING ON STUFF !!!!!!!!!!DAFAW3Q423");
		getServer().getPluginManager().registerEvents(this,this);
		

	}

	public void onDisable() {

	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			e = economyProvider.getProvider();
		}

		return (e != null);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("pfly")) {
			
			double cost = config.getDouble("costpersecond");
			Player p = (Player) sender;
			if(e.getBalance(p) >= cost) {
			if (currentTracking.contains(p.getUniqueId())) {
				currentTracking.remove(p.getUniqueId());
				p.setAllowFlight(false);
				p.sendMessage("");
				sender.sendMessage("Disabled Fly Mode at $" + config.getDouble("costpersecond") + "/sec");

			} else {
				p.sendMessage("");
				sender.sendMessage("Enabled Fly Mode at $" + config.getDouble("costpersecond") + "/sec");
				currentTracking.add(p.getUniqueId());
				p.setAllowFlight(true);
			}
			} else {
				sender.sendMessage("Fly Mode Not Permitted - Insufficient Funds... ($" + cost + " required)");
			}

		}

		return false;
	}
	
	@EventHandler
	public void onPDamage(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
		Player p = (Player) event.getEntity();
			p.setAllowFlight(false);
			p.setFlying(false);
			currentTracking.remove(p.getUniqueId());
			p.sendMessage("Oh no! You have taken damage; flight mode disabled.");
			
		}
		
	}
	@EventHandler
	public void onPJoin(PlayerJoinEvent event) {
		
		Player p = event.getPlayer();
	//	p.sendMessage("brhh");
		p.setAllowFlight(false);
		p.setFlying(false);
		if(currentTracking.contains(p.getUniqueId())) {
			currentTracking.remove(p.getUniqueId());
		}
		fallProtection.add(p.getUniqueId());
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if(fallProtection.contains(p.getUniqueId())) {
		if(!(p.getLocation().subtract(0,1,0).getBlock().getType() == Material.AIR)) {
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				
				@Override
				public void run() {
					//p.sendMessage("not falling no air");
					fallProtection.remove(p.getUniqueId());
					
				}
			}, 1L);
		
		}
		}
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent event) {
		
		if(event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if(fallProtection.contains(p.getUniqueId())  &&  event.getCause() == DamageCause.FALL) {
				//p.sendMessage("YOU ARE BEING");
				fallProtection.remove(p.getUniqueId());
				event.setCancelled(true);
				
			}
			
		}
	}
	
	
	
	
	
	
}
