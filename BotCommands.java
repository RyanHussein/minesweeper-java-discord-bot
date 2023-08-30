import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.*;

public class BotCommands extends ListenerAdapter {

    HashMap<String, Minesweeper> games = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equals("play")) {
            if (hasActiveGame(event.getUser().getId())) {
                event.reply("You have an active game already, /quit to end the game or continue playing.").queue();
                return;
            }
            this.cleanHashMap();
            games.putIfAbsent(event.getUser().getId(), new Minesweeper());
            games.get(event.getUser().getId()).play(event);
        }

        if (command.equals("open")) {
            if (!(hasActiveGame(event.getUser().getId()))) {
                event.reply("You have no active game, use /play to start one!").queue();
                return;
            }
            OptionMapping colOption = event.getOption("column");
            OptionMapping rowOption = event.getOption("row");

            if (colOption == null || rowOption == null) {
                event.reply("Make sure to input both the column and row!").queue();
                return;
            }

            if (!(this.isValidOption(colOption, rowOption))) {
                event.reply("Invalid co-ordinates. Keep it between A - I.").queue();
                return;
            }

            if (!this.games.get(event.getUser().getId()).open(event, colOption.getAsString(), rowOption.getAsString())) {
                this.games.remove(event.getUser().getId());
                return;
            }

            if (this.games.get(event.getUser().getId()).checkWin()) {
                EmbedBuilder eb = this.games.get(event.getUser().getId()).getBombGridAsEmbed();
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                this.games.remove(event.getUser().getId());
            }
        }

        if (command.equals("flag")) {
            if (!(hasActiveGame(event.getUser().getId()))) {
                event.reply("You have no active game, use /play to start one!").queue();
                return;
            }
            OptionMapping colOption = event.getOption("column");
            OptionMapping rowOption = event.getOption("row");

            if (colOption == null || rowOption == null) {
                event.reply("Make sure to input both the column and row!").queue();
                return;
            }

            if (!(this.isValidOption(colOption, rowOption))) {
                event.reply("Invalid co-ordinates. Keep it between A - I.").queue();
                return;
            }

            games.get(event.getUser().getId()).flag(event, colOption.getAsString(), rowOption.getAsString());

            if (this.games.get(event.getUser().getId()).checkWin()) {
                EmbedBuilder eb = this.games.get(event.getUser().getId()).getBombGridAsEmbed();
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                this.games.remove(event.getUser().getId());
            }
        }

        if (command.equals("retrieve")) {
            if (!(hasActiveGame(event.getUser().getId()))) {
                event.reply("You have no active game, use /play to start one!").queue();
                return;
            }
            OptionMapping colOption = event.getOption("column");
            OptionMapping rowOption = event.getOption("row");

            if (colOption == null || rowOption == null) {
                event.reply("Make sure to input both the column and row!").queue();
                return;
            }

            if (!(this.isValidOption(colOption, rowOption))) {
                event.reply("Invalid co-ordinates. Keep it between A - I.").queue();
                return;
            }

            games.get(event.getUser().getId()).retrieve(event, colOption.getAsString(), rowOption.getAsString());
        }

        if (command.equals("quit")) {
            if (!hasActiveGame(event.getUser().getId())) {
                event.reply("You don't have an active game, /play to start one.").queue();
                return;
            }
            this.games.remove(event.getUser().getId());
            event.reply("Game ended. Use /play to start another!").queue();
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("play", "Start a game of Minesweeper!"));
        commandData.add(Commands.slash("open", "Open a tile")
                .addOption(OptionType.STRING, "column", "A - I", true)
                .addOption(OptionType.STRING, "row", "A - I", true));
        commandData.add(Commands.slash("flag", "Place a flag down")
                .addOption(OptionType.STRING, "column", "A - I", true)
                .addOption(OptionType.STRING, "row", "A - I", true));
        commandData.add(Commands.slash("retrieve", "Retrieve a flag")
                .addOption(OptionType.STRING, "column", "A - I", true)
                .addOption(OptionType.STRING, "row", "A - I", true));
        commandData.add(Commands.slash("quit", "Quit your current game"));
        event.getJDA().updateCommands().addCommands(commandData).queue();
    }

    // HELPER METHODS

    private boolean hasActiveGame(String id) {
        return this.games.containsKey(id);
    }

    private boolean isValidOption(OptionMapping col, OptionMapping row) {
        String x = col.getAsString();
        String y = row.getAsString();
        boolean xValid = x.matches("([A-I]|[a-i])");
        boolean yValid = y.matches("([A-I]|[a-i])");
        return xValid && yValid;
    }

    private void cleanHashMap() {
        for (String user : this.games.keySet()) {
            long startTime = this.games.get(user).getTimestamp().getTime();
            long limit = startTime + 3600000;
            if (System.currentTimeMillis() > limit) {
                this.games.remove(user);
            }
        }
    }
}