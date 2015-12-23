package kana.SignRegionWorldguard;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlayerListener implements Listener{
	
	private Sign sign;
	private String[] lines;
	private Player player;
	private WorldGuardPlugin worldGuard;
	private RegionManager regionManager;
	private World world;
	private DefaultDomain owner;
	private String prix;
	private EconomyResponse achat;
	private EconomyResponse depot;
	private EconomyResponse depotRetour;
	private String groupe;
	private int nbrLimitTerrain;
	private int nbrTerrainJoueur;
	private LocalPlayer localplayer;
	private String nomTerrain;
	private ProtectedRegion region;
	private EconomyResponse verifArgent;
	
	SignRegionWorldguard plugin;
	public PlayerListener(SignRegionWorldguard plugin){
		this.plugin = plugin;
	}
	
	@org.bukkit.event.EventHandler(priority=EventPriority.NORMAL)      
    public void onPlayerInteract(PlayerInteractEvent e) throws StorageException{
	    // On vérifie clic droit
		//----------------------
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            // On vérifie si c'est un panneau
			//-------------------------------
			if(e.getClickedBlock().getState() instanceof Sign){
				this.sign = (Sign)e.getClickedBlock().getState();
				this.lines = sign.getLines();
				
				// On vérifie si il y a inscrit le nom du plugin
				//----------------------------------------------
				if(lines[0].equalsIgnoreCase("[SRW]") || lines[0].equalsIgnoreCase("[SignRegionWorldguard]")){
					
					this.world = sign.getWorld();					
					this.player = e.getPlayer();
					this.groupe = Vault.permission.getPrimaryGroup(player);
					this.prix = lines[2];
					
					// Vérification de la permission pour utiliser le panneau
					//-------------------------------------------------------
					if(!Vault.permission.playerHas(player,"signregionworldguard.use")){
						player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas la permission d'utiliser ce panneau!");
						return;
					}
					
					// Vérification que le joueur a assez d'argent pour la transaction
					//-----------------------------------------------------------------
					this.verifArgent = Vault.economy.bankHas(player.getName(), Double.parseDouble(prix));
	        		if(!verifArgent.transactionSuccess()){
	        			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous n'avez pas assez d'argent pour acheter ce terrain !");
	        			return;
	        		}
					
	        		// Vérification du nombre de terrain du joueur
	        		//--------------------------------------------
					this.nbrLimitTerrain = this.plugin.getConfig().getInt("nbr_terrain_max." + groupe);
					this.worldGuard = this.plugin.getWorldGuard();
	    			this.localplayer = worldGuard.wrapPlayer(player);
	    			this.nbrTerrainJoueur = worldGuard.getRegionManager(world).getRegionCountOfPlayer(localplayer);
	    			if(nbrTerrainJoueur >= nbrLimitTerrain){
	    				player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Vous avez atteint le nombre limite de terrain !");
		    			return;
	    			}

					// On ajoute le joueur sur le terrain
	    			//-----------------------------------
					this.nomTerrain = sign.getLine(1);
					this.regionManager = worldGuard.getRegionManager(world);
					this.region = regionManager.getRegion(nomTerrain);
					
					this.owner = new DefaultDomain();							    	
			    	owner.addPlayer(player.getName());
			    	region.setOwners(owner);
			    	
			    	// On retire l'argent du joueur
		    		//-----------------------------
			    	this.achat = Vault.economy.bankWithdraw(player.getName(), Double.parseDouble(prix));
			    	if(achat.transactionSuccess()){
			    		
			    		// On dépose l'argent sur le compte du proprio du terrain
			    		//-------------------------------------------------------
			    		this.depot = Vault.economy.bankDeposit(lines[3].toString(), Double.parseDouble(prix));
			    		if(!depot.transactionSuccess()){
			    			player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Un problème est survenu lors de l'achat, contactez un Admin !");
			    			
			    			this.depotRetour = Vault.economy.bankDeposit(player.toString(), Double.parseDouble(prix));
			    			if(!depotRetour.transactionSuccess()){
			    				player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Un problème est survenu lors de l'achat, contactez un Admin pour recupérer votre argent!");
			    				this.plugin.getConfig().addDefault("erreur_transaction." + player.getName(), 1);;
			    				return;
			    			}
			    		}
			    		
			    		// On sauvegarde Worldguard
			    		//-------------------------
						regionManager.save();
						
						// On supprime le panneau
						//-----------------------
						sign.getBlock().setType(Material.AIR);
						
						// On téléporte le joueur sur son terrain et envoie un message
						//------------------------------------------------------------
						player.teleport(sign.getLocation());
						player.sendMessage(ChatColor.GREEN + "[SignRegionWorldguard] " + ChatColor.WHITE + "Région " + ChatColor.BLUE + region.getId().toString() + ChatColor.WHITE + " acheté avec succés !");
						return;			    		
			    	}
			    	else{
			    		player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Un problème est survenu lors de l'achat, contactez un Admin !");
			    		return;
			    	}
				}
			}
		}
		else if(e.getAction() == Action.LEFT_CLICK_BLOCK){
			// On vérifie si c'est un panneau
			//-------------------------------
			if(e.getClickedBlock().getState() instanceof Sign){
				this.sign = (Sign)e.getClickedBlock().getState();
				this.lines = sign.getLines();
				this.player = e.getPlayer();
				
				// On vérifie si il y a inscrit le nom du plugin
				//----------------------------------------------
				if(lines[0].equalsIgnoreCase("[SRW]") || lines[0].equalsIgnoreCase("[SignRegionWorldguard]")){
					player.sendMessage(ChatColor.RED + "[SignRegionWorldguard] " + ChatColor.WHITE + "Acheter cette parcelle avec clic droit pour " + lines[2] + " " + Vault.economy.currencyNamePlural());
				}
			}
		}
	}
}
