import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class DiscordBot {

    public static void main(String[] args) throws LoginException, InterruptedException {

        JDA bot = JDABuilder.createDefault("MTEyOTc5ODM1OTM4MTcwODk4Mg.GwhVjp.3eqkGRDCZMaMu4kb9JA6FiAmcwoyDeJbybTuXs")
                .setActivity(Activity.playing("Minesweeper"))
                .addEventListeners(new BotCommands())
                .build().awaitReady();
    }
}