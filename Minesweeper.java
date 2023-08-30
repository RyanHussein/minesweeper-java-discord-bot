import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

public class Minesweeper {

    private final String[][] grid;
    private final int size;
    private final int bombs;
    private final String[][] bombGrid;
    private int flagsLeft;
    private boolean won;
    private final Timestamp timestamp;
    private final List<String> numberEmojis;
    private final List<String> alphabetEmojis;
    private final HashMap<String, Integer> coordinateTranslator;

    public Minesweeper() {
        this.won = false;
        this.size = 9;
        this.bombs = 10;
        this.flagsLeft = 10;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.grid = this.initialiseGrid();
        this.bombGrid = this.initialiseBombGrid();
        this.numberEmojis = this.initialiseNumberEmojis();
        this.alphabetEmojis = this.initialiseAlphabetEmojis();
        this.coordinateTranslator = this.initialiseTranslator();
    }

    // HELPER METHODS

    private String clean(String toClean) {
        return toClean.toLowerCase().strip();
    }

    private boolean isEmpty(String[][] grid, int x, int y) {
        return grid[y][x] == null;
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x <= this.size - 1 && y >= 0 && y <= this.size - 1;
    }

    private boolean moveMatchesBoxChosen(String move, int x, int y) {
        if (!(this.isValidCoordinate(x, y))) {
            return false;
        }

        if (this.clean(move).equals("o")) {
            return this.grid[y][x].equals("◻️");
        }

        if (this.clean(move).equals("f")) {
            return this.grid[y][x].equals("◻️");
        }

        if (this.clean(move).equals("r")) {
            return this.grid[y][x].equals("🚩");
        }

        return false;
    }

    // INITIALISING METHODS

    private List<String> initialiseNumberEmojis() {
        List<String> emojis = new ArrayList<>();
        emojis.add(":zero:");
        emojis.add(":one:");
        emojis.add(":two:");
        emojis.add(":three:");
        emojis.add(":four:");
        emojis.add(":five:");
        emojis.add(":six:");
        emojis.add(":seven:");
        emojis.add(":eight:");
        return emojis;
    }

    private List<String> initialiseAlphabetEmojis() {
        List<String> emojis = new ArrayList<>();
        emojis.add(":regional_indicator_a:");
        emojis.add(":regional_indicator_b:");
        emojis.add(":regional_indicator_c:");
        emojis.add(":regional_indicator_d:");
        emojis.add(":regional_indicator_e:");
        emojis.add(":regional_indicator_f:");
        emojis.add(":regional_indicator_g:");
        emojis.add(":regional_indicator_h:");
        emojis.add(":regional_indicator_i:");
        return emojis;
    }

    private HashMap<String, Integer> initialiseTranslator() {
        HashMap<String, Integer> translator = new HashMap<>();
        translator.put("A", 0);
        translator.put("B", 1);
        translator.put("C", 2);
        translator.put("D", 3);
        translator.put("E", 4);
        translator.put("F", 5);
        translator.put("G", 6);
        translator.put("H", 7);
        translator.put("I", 8);
        return translator;
    }

    private String[][] initialiseGrid() {
        String[][] grid = new String[size][size];
        for (String[] row : grid) {
            Arrays.fill(row, "◻️");
        }
        return grid;
    }

    private String[][] initialiseBombGrid() {
        String[][] Background = new String[size][size];
        this.setBombs(Background);
        return Background;
    }

    private void setBombs(String[][] Background) {
        int bombsLeft = this.bombs;
        Random random = new Random();
        while (bombsLeft > 0) {
            int randX = random.nextInt(size);
            int randY = random.nextInt(size);
            if (this.isEmpty(Background, randX, randY)) {
                Background[randY][randX] = ":bomb:";
                bombsLeft --;
            }
        }
    }

    // GAME METHODS

    public String drawGrid() {
        StringBuilder sb = new StringBuilder();
        sb.append("⬛");
        for (String alphabet : this.alphabetEmojis) {
            sb.append(alphabet);
        }
        sb.append("\n");

        for (int row = 0; row < this.size; row++) {
            sb.append(this.alphabetEmojis.get(row));
            for (int col = 0; col < this.size; col++) {
                sb.append(this.grid[row][col]);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String drawBombGrid() {
        StringBuilder sb = new StringBuilder();
        sb.append("⬛");
        for (String alphabet : this.alphabetEmojis) {
            sb.append(alphabet);
        }
        sb.append("\n");

        for (int row = 0; row < this.size; row++) {
            sb.append(this.alphabetEmojis.get(row));
            for (int col = 0; col < this.size; col++) {
                if (this.isEmpty(this.bombGrid, col , row)) {
                    sb.append("◻️");
                } else {
                    sb.append(this.bombGrid[row][col]);
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public boolean chooseBox(int x, int y, String choice, SlashCommandInteractionEvent event) {

        if (this.clean(choice).equals("f")) {
            if (!(this.moveMatchesBoxChosen(choice, x, y))) {
                event.reply("You can't place a flag there!").queue();
                return true;
            }
            if (flagsLeft > 0) {
                this.grid[y][x] = "🚩";
                this.flagsLeft --;
                return true;
            }
            event.reply("No more flags, /retrieve one of the others.").queue();
            return true;
        }

        if (this.clean(choice).equals("r")) {
            if (this.moveMatchesBoxChosen(choice, x, y)) {
                this.grid[y][x] = "◻️";
                this.flagsLeft ++;
                return true;
            }
            event.reply("You can only retrieve flags.").queue();
            return true;
        }

        if (this.moveMatchesBoxChosen(choice, x, y)) {
            if (!(this.isEmpty(this.bombGrid, x, y))) { return false; }
            this.updateGrid(x, y, event);
        } else {
            event.reply("You can't open that!").queue();
        }

        return true;
    }

    public void updateGrid(int x, int y, SlashCommandInteractionEvent event) {
        int neighbouringBombs = 0;

        // Traverse surrounding boxes
        for (int row = y-1; row <= y+1; row++) {
            for (int col = x-1; col <= x+1; col++) {
                if (this.isValidCoordinate(col, row)) {
                    if(!(this.isEmpty(this.bombGrid, col, row))) {
                        neighbouringBombs ++;
                    }
                }
            }
        }

        if (neighbouringBombs == 0) {
            this.grid[y][x] = "⬛";

            // Traverse surrounding boxes
            for (int row = y-1; row <= y+1; row++) {
                for (int col = x-1; col <= x+1; col++) {
                    if (col == x && row == y) { continue; }
                    if (this.moveMatchesBoxChosen("o", col, row)) {chooseBox(col, row, "o", event);}
                }
            }

        } else {
            this.grid[y][x] = this.numberEmojis.get(neighbouringBombs);
        }
    }

    public boolean checkWin() {
        for (int row = 0; row < this.size; row ++) {
            for (int col = 0; col < this.size; col ++) {
                if (this.grid[row][col].equals("◻️")) { return false; }
                if (this.grid[row][col].equals("🚩")) {
                    if (this.isEmpty(this.bombGrid, col, row)) {
                        return false;
                    }
                }
            }
        }
        this.won = true;
        return true;
    }

    // METHODS FOR DISCORD BOT

    public void play(SlashCommandInteractionEvent event) {
        event.reply("Use /open /flag /retrieve to play").queue();
        EmbedBuilder eb = this.getGridAsEmbed();
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public boolean open(SlashCommandInteractionEvent event, String col, String row) { // RETURNS FALSE IF BOMB IS HIT
        int x = this.coordinateTranslator.get(col.toUpperCase());
        int y = this.coordinateTranslator.get(row.toUpperCase());
        if(!this.chooseBox(x, y, "o", event)) {
            event.reply("You hit a bomb.").queue();
            EmbedBuilder eb = this.getBombGridAsEmbed();
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
            return false;
        } else {
            event.reply("Column: " + col.toUpperCase() + " Row: " + row.toUpperCase() + " Opened").queue();
            EmbedBuilder eb = this.getGridAsEmbed();
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
            return true;
        }
    }

    public void flag(SlashCommandInteractionEvent event, String col, String row) { // RETURNS FALSE IF INVALID CO-ORD
        int x = this.coordinateTranslator.get(col.toUpperCase());
        int y = this.coordinateTranslator.get(row.toUpperCase());
        this.chooseBox(x, y, "f", event);
        event.reply("Column: " + col.toUpperCase() + " Row: " + row.toUpperCase() + " Flagged").queue();
        EmbedBuilder eb = this.getGridAsEmbed();
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public void retrieve(SlashCommandInteractionEvent event, String col, String row) {
        int x = this.coordinateTranslator.get(col.toUpperCase());
        int y = this.coordinateTranslator.get(row.toUpperCase());
        this.chooseBox(x, y, "r", event);
        event.reply("Column: " + col.toUpperCase() + " Row: " + row.toUpperCase() + " Retrieved").queue();
        EmbedBuilder eb = this.getGridAsEmbed();
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public EmbedBuilder getGridAsEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(this.flagsLeft + " flags left");
        eb.setColor(Color.red);
        eb.addField("", this.drawGrid(), true);
        return eb;
    }

    public EmbedBuilder getBombGridAsEmbed() {
        String outcome;
        if (this.won) {
            outcome = "You Win!";
        } else {
            outcome = "You Lose :(";
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(outcome);
        eb.setColor(Color.red);
        eb.addField("The bombs were at:", this.drawBombGrid(), true);
        return eb;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }
}