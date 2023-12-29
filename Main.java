package ua.hackhud.pvpmodifers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {

    private Map<String, Long> cooldowns = new HashMap<>();
    private Map<String, Long> cooldown_tossing = new HashMap<>();

    private Map<String, Long> cooldown_back = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("TeleportPlugin включен!");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("TeleportPlugin отключен!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();

        // Проверяем, что игрок кликнул правой кнопкой мыши и держит предмет с материалом AIR (воздух)
        if (event.getAction().toString().contains("RIGHT")
                && itemInHand.getItemMeta() != null && itemInHand.getItemMeta().hasLore()) {

            // Проверяем, есть ли в лоре слово "Рывок"
            for (String loreLine : itemInHand.getItemMeta().getLore()) {
                if (loreLine.contains("Рывок")) {
                    // Бросаем игрока на 5 блоков в направлении, куда он смотрит
                    Vector direction = player.getLocation().getDirection().multiply(5);
                    player.setVelocity(direction);
                    player.sendMessage(ChatColor.GREEN + "Вы сделали рывок!");
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShoot(EntityShootBowEvent event) {
        if(event.isCancelled()){
            return;
        }
        if(event.getEntityType().equals(EntityType.PLAYER)){
            ItemStack bow = event.getBow();
            if(bow.hasItemMeta() && bow.getItemMeta().hasLore()){
                FixedMetadataValue value = new FixedMetadataValue(this,bow.getItemMeta().getLore());
                event.getProjectile().setMetadata("lores",value);
            }
        }
    }
    @EventHandler
    public void onPlayerGetDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }

        if (event.getDamager().getType().equals(EntityType.ARROW)) {
            Arrow arrow = (Arrow) event.getDamager();
            Player player = (Player) arrow.getShooter();
            if (arrow.hasMetadata("lores")) {
                List<String> lore = (List<String>) arrow.getMetadata("lores").get(0).value();
                for(String loreLine : lore) {
                    if (loreLine.contains("Разряд")) {
                        if (cooldowns.containsKey(player.getName()) &&
                        cooldowns.get(player.getName()) > System.currentTimeMillis()) {
                            int cooldown = (int) (cooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000;
                            cooldown = cooldown == 0 ? 1 : cooldown;
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&7Способность &6Разряд&7 на перезарядке: " + cooldown));
                            return;
                        }

                        Player damagedPlayer = (Player) event.getEntity();
                        Location playerLocation = damagedPlayer.getLocation();
                        damagedPlayer.getWorld().strikeLightning(playerLocation);
                        cooldowns.put(player.getName(), System.currentTimeMillis() + 5 * 1000);
                        break;
                    }
                }
            }
        }
    }
    @EventHandler
    public void tossing(EntityDamageByEntityEvent event){
        if (event.isCancelled()) {
            return;
        }
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }
        if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
            return;
        }
            Player attacker = (Player) event.getDamager();
            Player defender = (Player) event.getEntity();
            if(attacker.getItemInHand().getItemMeta().hasLore()){
                for(String loreLine : attacker.getItemInHand().getItemMeta().getLore()) {
                    if (cooldown_tossing.containsKey(attacker.getName()) &&
                            cooldown_tossing.get(attacker.getName()) > System.currentTimeMillis()) {
                        int cooldown = (int) (cooldown_tossing.get(attacker.getName()) - System.currentTimeMillis()) / 1000;
                        cooldown = cooldown == 0 ? 1 : cooldown;
                        attacker.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&7Способность &6Подкидывание&7 на перезарядке: " + cooldown));
                        return;
                    }
                    if (loreLine.contains("Подкидывание")) {
                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            Vector vector = new Vector(attacker.getLocation().getDirection().getX(), 1, attacker.getLocation().getDirection().getZ());
                            defender.setVelocity(vector);
                        }, 1);
                        cooldown_tossing.put(attacker.getName(), System.currentTimeMillis() + 5 * 1000);
                    }
                }
            }
        }
    @EventHandler
    public void back(EntityDamageByEntityEvent event){
        if (event.isCancelled()) {
            return;
        }
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }
        if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
            return;
        }
        Player attacker = (Player) event.getDamager();
        Player defender = (Player) event.getEntity();
        if(attacker.getItemInHand().getItemMeta().hasLore()){
            for(String loreLine : attacker.getItemInHand().getItemMeta().getLore()) {
                if (cooldown_back.containsKey(attacker.getName()) &&
                        cooldown_back.get(attacker.getName()) > System.currentTimeMillis()) {
                    int cooldown = (int) (cooldown_back.get(attacker.getName()) - System.currentTimeMillis()) / 1000;
                    cooldown = cooldown == 0 ? 1 : cooldown;
                    attacker.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&7Способность &6Бесшумный убийца&7 на перезарядке: " + cooldown));
                    return;
                }
                if (loreLine.contains("Бесшумный убийца")) {
                        event.setCancelled(true);
                        Vector direction = defender.getLocation().getDirection().normalize();
                        double x = defender.getLocation().getX()-1 * direction.getX();
                        double y = defender.getLocation().getY();
                        double z = defender.getLocation().getZ()-1 * direction.getZ();
                        World world = defender.getWorld();
                        Location defenderLocation = new Location(world,x,y,z,(float) defender.getLocation().getDirection().getX()* (float) defender.getLocation().getDirection().getX(), (float) defender.getLocation().getDirection().getY());
                        attacker.teleport(defenderLocation);
                    cooldown_back.put(attacker.getName(), System.currentTimeMillis() + 5 * 1000);
                }
            }
        }
    }
    }

