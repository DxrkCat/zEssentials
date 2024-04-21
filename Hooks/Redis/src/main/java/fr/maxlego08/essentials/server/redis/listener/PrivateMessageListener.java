package fr.maxlego08.essentials.server.redis.listener;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.server.messages.ServerPrivateMessage;
import fr.maxlego08.essentials.api.storage.IStorage;
import fr.maxlego08.essentials.api.user.PrivateMessage;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.server.redis.RedisListener;

public class PrivateMessageListener extends RedisListener<ServerPrivateMessage> {

    private final EssentialsPlugin plugin;

    public PrivateMessageListener(EssentialsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void onMessage(ServerPrivateMessage message) {
        System.out.println("Reception du message: " + message);
        IStorage iStorage = this.plugin.getStorageManager().getStorage();
        User targetUser = iStorage.getUser(message.targetUniqueId());
        System.out.println("User: " + targetUser);
        if (targetUser == null) return;

        PrivateMessage privateMessageReply = targetUser.setPrivateMessage(message.senderUniqueId(), message.senderName());
        this.plugin.getUtils().sendPrivateMessage(targetUser, privateMessageReply, Message.COMMAND_MESSAGE_OTHER, message.message());
    }

}
