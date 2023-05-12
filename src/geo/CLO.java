package geo;

import org.apache.commons.cli.*;

public class CLO { // command line options

    // Required Options:
    public final String file; // instance file

    // Optional Options:
    public final boolean gif; // generate step-by-step display of the merging process
    public final int seed;  // random seed to use
    public final int searchLimit; // number of unsuccessful attempts the tool will make to find a pair of
                                  // convex pieces to merge before terminating
    public final int width; // width of the saved images (in pixels)
    public final int height;  // height of the saved images (in pixels)

    // Default Values:
    public static final int SEED_DEFAULT = 1;
    public static final int SEARCH_LIMIT_DEFAULT = 10000;
    public static final int WIDTH_DEFAULT = 1000;
    public static final int HEIGHT_DEFAULT = 1000;

    CLO(String[] args) {
        Options options = new Options();

        Option file = new Option("file", "file", true,
                "Problem instance file.");
        file.setRequired(true);
        file.setType(String.class);
        options.addOption(file);

        Option gif = new Option("gif", "gif", false,
                "Generate a .png file for each step in the algorithm.");
        gif.setRequired(false);
        gif.setType(boolean.class);
        options.addOption(gif);

        Option seed = new Option("seed", "seed", true,
                "The random seed to be used.");
        seed.setRequired(false);
        seed.setType(Number.class);
        options.addOption(seed);

        Option searchLimit = new Option("searchLimit", "searchLimit", true,
                "Number of unsuccessful attempts the tool will make to find a pair of convex " +
                        "pieces to merge before terminating");
        searchLimit.setRequired(false);
        searchLimit.setType(Number.class);
        options.addOption(searchLimit);

        Option width = new Option("width", "width", true,
                "Width of generated images (in pixels).");
        width.setRequired(false);
        width.setType(Number.class);
        options.addOption(width);

        Option height = new Option("height", "height", true,
                "Height of generated images (in pixels).");
        height.setRequired(false);
        height.setType(Number.class);
        options.addOption(height);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        this.file = cmd.getOptionValue("file");
        this.gif = cmd.hasOption("gif");
        this.seed = cmd.hasOption("seed") ?
                Integer.parseInt(cmd.getOptionValue("seed")) :
                SEED_DEFAULT;
        this.searchLimit = cmd.hasOption("searchLimit") ?
                Integer.parseInt(cmd.getOptionValue("searchLimit")) :
                SEARCH_LIMIT_DEFAULT;
        this.width = cmd.hasOption("width") ?
                Integer.parseInt(cmd.getOptionValue("width")) :
                WIDTH_DEFAULT;
        this.height = cmd.hasOption("height") ?
                Integer.parseInt(cmd.getOptionValue("height")) :
                HEIGHT_DEFAULT;
    }

}