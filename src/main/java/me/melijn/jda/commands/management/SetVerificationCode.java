package me.melijn.jda.commands.management;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.melijn.jda.Helpers;
import me.melijn.jda.Melijn;
import me.melijn.jda.blub.Category;
import me.melijn.jda.blub.Command;
import me.melijn.jda.blub.CommandEvent;
import me.melijn.jda.blub.Need;
import me.melijn.jda.utils.TaskScheduler;
import net.dv8tion.jda.core.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SetVerificationCode extends Command {

    public SetVerificationCode() {
        this.commandName = "setVerificationCode";
        this.usage = Melijn.PREFIX + commandName + " [code | null]";
        this.description = "set's a verificationCode that users will have to send in the verificationChannel";
        this.category = Category.MANAGEMENT;
        this.needs = new Need[]{Need.GUILD};
    }

    public static final LoadingCache<Long, String> verificationCodeCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                public String load(@NotNull Long key) {
                    return Melijn.mySQL.getGuildVerificationCode(key);
                }
            });

    @Override
    protected void execute(CommandEvent event) {
        if (Helpers.hasPerm(event.getMember(), commandName, 1)) {
            Guild guild = event.getGuild();
            if (guild.getTextChannelById(SetVerificationChannel.verificationChannelsCache.getUnchecked(guild.getIdLong())) != null) {
                String[] args = event.getArgs().split("\\s+");
                if (args.length > 0 && !args[0].equalsIgnoreCase("")) {
                    if (args[0].equalsIgnoreCase("null")) {
                        TaskScheduler.async(() -> {
                            Melijn.mySQL.removeVerificationCode(guild.getIdLong());
                            verificationCodeCache.invalidate(guild.getIdLong());
                        });
                        event.reply("The VerificationCode has been set to nothing by **" + event.getFullAuthorName() + "**");
                    } else {
                        TaskScheduler.async(() -> {
                            Melijn.mySQL.setVerificationCode(guild.getIdLong(), args[0]);
                            verificationCodeCache.put(guild.getIdLong(), args[0]);
                        });
                        event.reply("The VerificationCode has been set to " + args[0] + " by **" + event.getFullAuthorName() + "**");
                    }
                } else {
                    try {
                        event.reply("The VerificationCode is " + (verificationCodeCache.get(guild.getIdLong()) == null ? "unset" : verificationCodeCache.getUnchecked(guild.getIdLong())));
                    } catch (ExecutionException ignored) { }
                }
            } else {
                event.reply("You first have to setup a Verification TextChannel\nYou'll probably want to follow this guide: https://melijn.com/guides/guide-7");
            }
        } else {
            event.reply("You need the permission `" + commandName + "` to execute this command.");
        }
    }
}
