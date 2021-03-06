package me.melijn.jda.commands.fun;

import me.melijn.jda.Helpers;
import me.melijn.jda.blub.Category;
import me.melijn.jda.blub.Command;
import me.melijn.jda.blub.CommandEvent;
import me.melijn.jda.utils.MessageHelper;
import net.dv8tion.jda.core.Permission;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static me.melijn.jda.Melijn.PREFIX;

public class SayCommand extends Command {

    public SayCommand() {
        this.commandName = "say";
        this.description = "Makes the bot say stuff";
        this.usage = PREFIX + commandName + " <message>";
        this.aliases = new String[]{"zeg"};
        this.permissions = new Permission[]{Permission.MESSAGE_ATTACH_FILES};
        this.category = Category.FUN;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getGuild() == null || Helpers.hasPerm(event.getMember(), this.commandName, 0)) {
            if (event.getArgs().length() > 0) {
                final BufferedImage image;
                try {
                    String resourcename = event.getExecutor().equalsIgnoreCase("zeg") ? "melijn_zegt.png" : "melijn_says.png";
                    image = ImageIO.read(new File(resourcename));
                    Graphics g = image.getGraphics();
                    g.setFont(g.getFont().deriveFont(40f));
                    if (event.getArgs().length() < 26) {
                        g.drawString(event.getArgs(), 650, 200);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        String[] parts = event.getArgs().split("\\s+");
                        for (String part : parts) {
                            if (part.length() > 25) {
                                String[] characters = part.split("");
                                int i = 0;
                                for (String charl : characters) {
                                    sb.append(charl);
                                    if (i++ == 22) {
                                        sb.append("-\n");
                                        i = 0;
                                    }
                                }
                            } else if ((sb.toString().split("\n")[sb.toString().split("\n").length - 1].length() + part.length()) > 28) {
                                sb.append("\n").append(part);
                            } else {
                                if (sb.toString().length() > 0) sb.append(" ");
                                sb.append(part);
                            }
                        }
                        int i = 0;
                        String[] lines = sb.toString().split("\n");
                        for (String line : lines) {
                            g.drawString(line, 640, 220 - (20 * lines.length) + (i++ * 40));

                        }
                    }
                    g.dispose();
                    String imageName = String.valueOf(System.currentTimeMillis());
                    File file = new File(imageName + ".png");
                    ImageIO.write(image, "png", file);
                    event.getTextChannel().sendFile(file).queue(q -> file.delete());

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                MessageHelper.sendUsage(this, event);
            }
        } else {
            event.reply("You need the permission `" + commandName + "` to execute this command.");
        }
    }
}
