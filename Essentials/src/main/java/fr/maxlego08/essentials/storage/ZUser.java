package fr.maxlego08.essentials.storage;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.TeleportRequest;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.modules.TeleportationModule;
import fr.maxlego08.essentials.user.ZTeleportRequest;
import fr.maxlego08.essentials.zutils.utils.ZUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZUser extends ZUtils implements User {

    private final EssentialsPlugin plugin;
    private final Map<UUID, TeleportRequest> teleports = new HashMap<>();
    private final UUID uniqueId;
    private String name;
    private TeleportRequest teleportRequest;
    private User targetUser;

    public ZUser(EssentialsPlugin plugin, UUID uniqueId) {
        this.plugin = plugin;
        this.uniqueId = uniqueId;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(this.name);
    }

    @Override
    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(this.uniqueId).isOnline();
    }

    @Override
    public boolean isIgnore(UUID uniqueId) {
        return false;
    }

    @Override
    public void sendTeleportRequest(User targetUser) {

        if (targetUser == null || !targetUser.isOnline()){
            message(this, Message.COMMAND_TPA_ERROR_SAME);
            return;
        }

        if (targetUser.getUniqueId().equals(this.uniqueId)) {
            message(this, Message.COMMAND_TPA_ERROR_SAME);
            return;
        }

        if (targetUser.isIgnore(this.uniqueId)) {
            message(this, Message.COMMAND_TELEPORT_IGNORE_PLAYER, targetUser);
            return;
        }

        this.teleports.entrySet().removeIf(next -> !next.getValue().isValid());

        if (this.teleports.containsKey(targetUser.getUniqueId())) {
            message(this, Message.COMMAND_TPA_ERROR, targetUser);
            return;
        }

        TeleportationModule teleportationModule = this.plugin.getModuleManager().getModule(TeleportationModule.class);
        long expired = System.currentTimeMillis() + (teleportationModule.getTeleportTpaExpire() * 1000L);
        TeleportRequest teleportRequest = new ZTeleportRequest(this.plugin, targetUser, this, expired);
        targetUser.setTeleportRequest(teleportRequest);
        this.teleports.put(targetUser.getUniqueId(), teleportRequest);

        message(this, Message.COMMAND_TPA_SENDER, targetUser);
        message(targetUser, Message.COMMAND_TPA_RECEIVER, getPlayer());
    }

    @Override
    public void cancelTeleportRequest(User targetUser) {

        if (!this.teleports.containsKey(targetUser.getUniqueId())) {
            message(this, Message.COMMAND_TP_CANCEL_ERROR, targetUser);
            return;
        }

        this.teleports.remove(targetUser.getUniqueId());

        if (targetUser.getTeleportRequest() != null && targetUser.getTeleportRequest().getFromUser() == this) {
            targetUser.setTeleportRequest(null);
        }

        message(this, Message.COMMAND_TP_CANCEL_SENDER, targetUser);
        message(targetUser, Message.COMMAND_TP_CANCEL_RECEIVER, this);
    }

    @Override
    public Collection<TeleportRequest> getTeleportRequests() {
        return this.teleports.values();
    }

    @Override
    public TeleportRequest getTeleportRequest() {
        return teleportRequest;
    }

    @Override
    public void setTeleportRequest(TeleportRequest teleportRequest) {
        this.teleportRequest = teleportRequest;
    }

    @Override
    public void removeTeleportRequest(User user) {
        this.teleports.remove(user.getUniqueId());
    }

    @Override
    public void teleport(Location location) {
        this.plugin.getScheduler().teleportAsync(this.getPlayer(), location);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return getPlayer().hasPermission(permission.asPermission());
    }

    @Override
    public User getTargetUser() {
        return targetUser;
    }

    @Override
    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }
}
