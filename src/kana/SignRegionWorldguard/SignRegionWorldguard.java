package kana.SignRegionWorldguard;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SignRegionWorldguard extends JavaPlugin implements Listener{
	
	private Logger logger = Logger.getLogger("Minecraft");
	public Plugin plugin;
    
    public void onEnable(){
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerListener(this), this);
    	pm.registerEvents(new BlockListener(this), this);
    	
    	Vault.load(this);
    	Vault.setupChat();
    	Vault.setupPermissions();
    	if (!Vault.setupEconomy()) {
            logger.info(String.format("[%s] - SignRegionWorldguard necessite Vault pour fonctionner!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    	
    	this.loadConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
        
		logger.info("[SignRegionWorldguard] Plugin charger parfaitement!");
		
		// Metric
		//-------
		try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (IOException e) {
	    	logger.info("[SignRegionWorldguard - Metric] Un problème un survenu!");
	    }
    }
    
    public void onDisable(){
    	logger.info("[SignRegionWorldguard] Plugin stopper...");
    }
    
    public void loadConfig(){           
    	this.getConfig().options().copyDefaults(true);
		this.saveConfig();
    }
    
    WorldGuardPlugin getWorldGuard() {
        this.plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            getServer().getPluginManager().disablePlugin(this);
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		// Récupération du joueur qui envoie la commande
    	//----------------------------------------------
		Player player = null;
    	if(sender instanceof Player){
    		player = (Player) sender;
    	}
    	if(commandLabel.equalsIgnoreCase("srw")){
    		
	        if(args.length == 0){
	        	sender.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Tapez /srw reload");
	        	return true;
	        }	        
	        else if(args.length == 1){
	        	//----------------
	        	//---- RELOAD ----
		        //----------------
	        	if(args[0].equalsIgnoreCase("reload")){
	        		// On vérifi si le joueur à la permission
	        		//---------------------------------------
	        		if(!Vault.permission.has(player, "signregionworldguard.reload")){
	        			sender.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas la permission d'utiliser cette commande !");
	    	        	return false;
	        		}
	        		this.reloadConfig();
	        		sender.sendMessage(ChatColor.GREEN + "[SignRegionWorldguard] " + ChatColor.WHITE + "Configuration rechargée !");
	        		return true;
	        	}
	        	else{
	        		sender.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Tapez /srw reload");
		        	return true;
	        	}
	        }
	        else{
        		sender.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Tapez /srw reload");
	        	return true;
        	}
    	}
    	return false;
    }
}
