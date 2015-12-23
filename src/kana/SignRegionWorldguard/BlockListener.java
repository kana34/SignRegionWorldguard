package kana.SignRegionWorldguard;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class BlockListener implements Listener {
	
	private String[] lines;
	private Sign sign;
	private Player player;
	private String prix;
	private Block bloc;
	private WorldGuardPlugin worldGuard;
	private World world;
	private RegionManager regionManager;
	private List<String> listRegion;
	private String parent;
	private Vector position;
	
	SignRegionWorldguard plugin;
	public BlockListener(SignRegionWorldguard plugin){
		this.plugin = plugin;
	}
	
	@org.bukkit.event.EventHandler(priority=EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event){
		this.sign = (Sign) event.getBlock().getState();
		this.lines = event.getLines();
		
		// On vérifie si il y a inscrit le nom du plugin
		//----------------------------------------------
        if(lines[0].equalsIgnoreCase("[SRW]") || lines[0].equalsIgnoreCase("[SignRegionWorldguard]")){

    		this.player = event.getPlayer();
    		this.prix = this.plugin.getConfig().getString("prix");
    		this.world = event.getBlock().getWorld();
    		
        	// On vérifie si le joueur à la permission de créer un panneau
    		//------------------------------------------------------------
        	if(!Vault.permission.playerHas(player,"signregionworldguard.create")){
        		player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas la permission pour créer ce panneau !");
        		event.setCancelled(true);
        		return;
        	}
        	
        	// On vérifie si le panneau se trouve sur 2 régions ou sur aucune
        	//---------------------------------------------------------------
        	this.parent = this.plugin.getConfig().getString("parent");
        	this.position = new Vector(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ());
    		this.worldGuard = this.plugin.getWorldGuard();
    		this.regionManager = worldGuard.getRegionManager(world);
    		this.listRegion = regionManager.getApplicableRegionsIDs(position);
    		listRegion.remove(parent);
    		    	
    		if(listRegion.size() > 1){
    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Le panneau se trouve sur 2 régions !");
    			event.setCancelled(true);
        		return;
    		}
    		else if(listRegion.size() == 0){
    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Aucune region ne ce trouve ici !");
    			event.setCancelled(true);
        		return;
    		}
    		// On formate le panneau
    		//----------------------
    		else{    			
    			event.setLine(1, listRegion.get(0));
    			if(lines[2].toString().isEmpty()){
        			event.setLine(2, prix);
        		}
        		if(lines[3].toString().isEmpty()){
        			event.setLine(3, "cubicraft");
        		}
        		event.getBlock().getState().update();
        		player.sendMessage(ChatColor.GREEN + "[SignRegionWorldguard] " + ChatColor.WHITE + "Panneau créé avec succés !");
        		return;
    		}
        }
	}
	
	@org.bukkit.event.EventHandler(priority=EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event){
		this.bloc = event.getBlock();			
		
		// On vérifie si c'est un panneau
		//-------------------------------
		if (bloc != null && bloc.getState() instanceof Sign){
			this.sign = (Sign) event.getBlock().getState();
			this.lines = sign.getLines();
			// On vérifie si il y a inscrit le nom du plugin
			//----------------------------------------------
			if(lines[0].equalsIgnoreCase("[SRW]") || lines[0].equalsIgnoreCase("[SignRegionWorldguard]")){
				
				this.player = event.getPlayer();				
				
				// On vérifie la permission
				//-------------------------
	    		if(!Vault.permission.playerHas(player,"signregionworldguard.create")){
	    			event.setCancelled(true);
	    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas la permission pour détruire ce panneau !");
	    			return;
	    		}
	    		player.sendMessage(ChatColor.GREEN + "[SignRegionWorldguard] " + ChatColor.WHITE + "Panneau détruit !");
	        }
		}
	}
}

