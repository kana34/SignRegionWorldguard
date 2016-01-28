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
	private Double prix_bloc;
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
		
		// On v�rifie si il y a inscrit le nom du plugin
		//----------------------------------------------
        if(lines[0].equalsIgnoreCase("[SRW]") || lines[0].equalsIgnoreCase("[SignRegionWorldguard]")){

    		this.player = event.getPlayer();   		
    		this.world = event.getBlock().getWorld();   		    		
    		
        	// On v�rifie si le joueur � la permission de cr�er un panneau
    		//------------------------------------------------------------
        	if(!Vault.permission.playerHas(player,"signregionworldguard.create")){
        		player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas la permission pour cr�er ce panneau !");
        		event.setCancelled(true);
        		return;
        	}
        	
        	// On v�rifie si le panneau se trouve sur 2 r�gions ou sur aucune
        	//---------------------------------------------------------------
        	this.parent = this.plugin.getConfig().getString("parent");
        	this.position = new Vector(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ());
    		this.worldGuard = this.plugin.getWorldGuard();
    		this.regionManager = worldGuard.getRegionManager(world);
    		this.listRegion = regionManager.getApplicableRegionsIDs(position);
    		listRegion.remove(parent);
    		    	
    		if(listRegion.size() > 1){
    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Le panneau se trouve sur 2 r�gions !");
    			event.setCancelled(true);
        		return;
    		}
    		else if(listRegion.size() == 0){
    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Aucune region ne ce trouve ici !");
    			event.setCancelled(true);
        		return;
    		}    	   		
    		else{    			
    			// On v�rifie si on fixe le prix par bloc ou non
        		//----------------------------------------------
        		if(this.plugin.getConfig().getBoolean("prix_par_bloc") == true){
        			this.prix_bloc = this.plugin.getConfig().getDouble("prix_bloc");
        			this.prix = Double.toString(regionManager.getRegion(listRegion.get(0)).volume() * prix_bloc);
        		}
        		else{
        			this.prix = this.plugin.getConfig().getString("prix");
        		}
        		
        		// On formate le panneau
        		//----------------------
    			event.setLine(1, listRegion.get(0));
    			if(lines[2].toString().isEmpty()){
        			event.setLine(2, prix);
        		}
        		if(lines[3].toString().isEmpty()){
        			event.setLine(3, "cubicraft");
        		}
        		event.getBlock().getState().update();
        		player.sendMessage(ChatColor.GREEN + "[SignRegionWorldguard] " + ChatColor.WHITE + "Panneau cr�� avec succ�s !");
        		return;
    		}
        }
	}
	
	@org.bukkit.event.EventHandler(priority=EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event){
		this.bloc = event.getBlock();			
		
		// On v�rifie si c'est un panneau
		//-------------------------------
		if (bloc != null && bloc.getState() instanceof Sign){
			this.sign = (Sign) event.getBlock().getState();
			this.lines = sign.getLines();
			// On v�rifie si il y a inscrit le nom du plugin
			//----------------------------------------------
			if(lines[0].equalsIgnoreCase("[SRW]") || lines[0].equalsIgnoreCase("[SignRegionWorldguard]")){
				
				this.player = event.getPlayer();				
				
				// On v�rifie la permission
				//-------------------------
	    		if(!Vault.permission.playerHas(player,"signregionworldguard.create")){
	    			event.setCancelled(true);
	    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas la permission pour d�truire ce panneau !");
	    			return;
	    		}
	    		player.sendMessage(ChatColor.GREEN + "[SignRegionWorldguard] " + ChatColor.WHITE + "Panneau d�truit !");
	        }
		}
	}
}

