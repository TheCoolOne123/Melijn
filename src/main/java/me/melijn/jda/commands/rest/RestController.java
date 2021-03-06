package me.melijn.jda.commands.rest;

import me.melijn.jda.Melijn;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @GetMapping("/guildCount")
    public Map<String, Object> guildCount() {
        JSONObject response = new JSONObject();
        response.put("guildCount", String.valueOf(Melijn.getShardManager().getGuilds().size()));
        return response.toMap();
    }

    @GetMapping("/shards")
    public Map<String, Object> getShards() {
        JSONObject response = new JSONObject();
        for (JDA shard : Melijn.getShardManager().getShards())
            response.put(String.valueOf(shard.getShardInfo().getShardId()), new JSONObject()
                    .put("guildCount", shard.getGuilds().size())
                    .put("userCount", shard.getUsers().size())
                    .put("connectedVoiceChannels", shard.getGuilds().stream().filter(guild -> guild.getSelfMember().getVoiceState().inVoiceChannel()).count())
            );
        return response.toMap();
    }

    @GetMapping("/guild/{id}")
    public Map<String, Object> getGuildInformation(@PathVariable String id) {
        if (id == null || !id.matches("\\d+")) return new JSONObject().put("error", "invalid id").toMap();
        Guild guild = Melijn.getShardManager().getGuildById(id);
        if (guild == null) return new JSONObject().put("isBotMember", false).toMap();
        return new JSONObject()
                .put("name", guild.getName())
                .put("memberCount", guild.getMemberCache().size())
                .put("ownerId", guild.getOwnerId())
                .put("isBotMember", true).toMap();
    }

    @GetMapping("/member/{guildId}/{userId}")
    public Map<String, Object> getMemberInformation(@PathVariable String guildId, @PathVariable String userId) {
        if (guildId.matches("\\d+") && userId.matches("\\d+")) {
            Guild guild = Melijn.getShardManager().getGuildById(guildId);
            if (guild != null) {
                User kipje = Melijn.getShardManager().getUserById(userId);
                if (kipje != null && guild.getMember(kipje) != null) {
                    return new JSONObject()
                            .put("isMember", true)
                            .put("isAdmin", guild.getMember(kipje).getPermissions().contains(Permission.ADMINISTRATOR) ||
                                    guild.getMember(kipje).isOwner() ||
                                    (0 < guild.getMember(kipje).getRoles().stream().filter(role -> role.getPermissions().contains(Permission.ADMINISTRATOR)).count()))
                            .toMap();
                } else {
                    return new JSONObject()
                            .put("error", "Member not found")
                            .put("isMember", false)
                            .toMap();
                }
            } else {
                return new JSONObject()
                        .put("error", "Invalid guildId")
                        .put("isMember", false)
                        .toMap();
            }
        } else {
            return new JSONObject()
                    .put("error", "Id must be a number")
                    .put("isMember", false)
                    .toMap();
        }
    }
}
