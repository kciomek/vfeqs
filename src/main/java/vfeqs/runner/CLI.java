package vfeqs.runner;

import org.apache.commons.cli.*;


public class CLI {
    private final CommandLineParser parser;

    private int numberOfThreads;
    private boolean printStep;
    private boolean processAllQuestions;
    private String experimentDescription;
    private boolean printFinalResult;
    private boolean calculateProbabilities;

    public CLI() {
        this.parser = new DefaultParser();
    }

    private Options generateOptions() {
        Options options = new Options();

        options.addOption(Option.builder("h")
                        .required(false)
                        .desc("prints help")
                        .longOpt("help")
                        .build()
        );

        options.addOption(Option.builder("t")
                        .required(false)
                        .hasArg()
                        .desc("number of threads to run (default: 1)")
                        .build()
        );

        options.addOption(Option.builder("i")
                        .required(false)
                        .hasArg()
                        .desc("experiment description (-h to see the format)")
                        .build()
        );

        options.addOption(Option.builder("s")
                        .required(false)
                        .longOpt("print-each-step-information")
                        .desc("prints various statistics for each iteration (it makes processing much slower and producing a lot of output data)")
                        .build()
        );

        options.addOption(Option.builder("r")
                .required(false)
                .longOpt("print-final-result")
                .desc("prints final result - ranking, chosen alternative or assignment (makes -a option enabled)")
                .build()
        );

        options.addOption(Option.builder("e")
                .required(false)
                .longOpt("calculate-probabilities")
                .desc("calculates probabilities (makes -a option enabled)")
                .build()
        );

        options.addOption(Option.builder("a")
                .required(false)
                .longOpt("process-all-questions")
                .desc("applies processing of all questions (even if final result is already known)")
                .build()
        );

        return options;
    }

    public void parse(String args[]) throws ParseException {
        CommandLine cmd = parser.parse(this.generateOptions(), args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("...", this.generateOptions());
            System.out.println();
            System.out.println("If experiment data was not provided with -i then it will be read from stdin. " +
                    "One line = one experiment description.\n\n" +
                    "Experiment description format:\n" +
                    "\tb x[/+] a/c/p[/s:m][@w] f[/g] i/r/q d h e n t[/o]\n" +
                    "where:\n" +
                    " b - type of problem ('R' - ranking, 'C' - choice, 'S' - sorting)\n" +
                    " x - priority <integer> or <integer>/+ for increasing priority,\n" +
                    " a - number of alternatives,\n" +
                    " c - number of criteria,\n" +
                    " p - number of characteristic points,\n" +
                    " s:m - optional seed for instance building with method generation ('h' - vectors from hypersphere (absolute values), 'u' - elements from uniform distribution, 'g' - elements from gauss distribution) (if provided then i != 1 does not make sense),\n" +
                    " w - number of classes (required only for sorting problems),\n" +
                    " f - initial number of randomly generated preference information (not supported yet),\n" +
                    " g - optional seed for random generation of initial preference information,\n" +
                    " i - number of instances for experiment,\n" +
                    " r - number of repetitions per instance (every execution as separate result),\n" +
                    " q - number of internal repetitions (all executions as single result),\n" +
                    " d - decision maker identifier ('full', 'erandom', 'fixed/<data>'), where <data> has format <integer>-...-<integer> and represents ranks (for ranking and choice) or assignments (for sorting)\n" +
                    " h - comma-separated list of strategies (heuristics) (available for ranking: RAND, DVF, SQEVAL, (W|M|E)-(IN|ERA|PE|RE), [X]DS-(W|M|E)-<integer as depth>, [X]DVF-<integer as depth>, and for choice RAND[-P], DVF[-P], (W|M|E)-(POA|FRAI)[-P]),\n" +
                    " e - epsilon,\n" +
                    " n - number of samples,\n" +
                    " t - thinning function identifier ('tfl' = (n-1)^3, 'tfc'),\n" +
                    " o - optional thinning function parameter (if not provided, for 'tfl' will be 1, for 'tfc' will be ((c-1)+(p-1))^3).\n" +
                    "\n" +
                    "Output format (tab-separated):\n" +
                    "\tb a c p w s m vftype d d' h q e n th time leafs f/g min mean median max wmean emean wsum esum\n" +
                    "where:\n" +
                    " vftype     - type of value functions (for now, always 'strict')\n" +
                    " d          - decision maker (note that input 'erandom' is output 'fixed' with parameter d' (see next line),\n" +
                    " d'         - parameter of decision maker (for input 'fixed' and 'erandom')\n" +
                    " th         - thinning function,\n" +
                    " time       - processing time in ms,\n" +
                    " leafs      - number of leafs (make sense only for d='full'),\n" +
                    " min, mean, median, max, wmean, emean - statistics of number of questions asked (emean and wmean have proper values only for tests with parameter '-e'),\n" +
                    " wsum, esum - control values (used for debugging, expected to be equal to 1.0 +/- computation accuracy error; valid for executions with parameter '-e' and d='full'),\n" +
                    " the rest   - same as for input format/experiment description format."
            );
            System.exit(1);
        }

        if (cmd.hasOption("t")) {
            this.numberOfThreads = Integer.parseInt(cmd.getOptionValue("t"));
        } else {
            this.numberOfThreads = 1;
        }

        if (cmd.hasOption("i")) {
            this.experimentDescription = cmd.getOptionValue("i");
        }

        this.printStep = cmd.hasOption("s");

        this.processAllQuestions = cmd.hasOption("a");

        this.printFinalResult = cmd.hasOption("r");

        this.calculateProbabilities = cmd.hasOption("e");
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public boolean isPrintStep() {
        return printStep;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public boolean isPrintFinalResult() {
        return printFinalResult;
    }

    public boolean isCalculateProbabilities() {
        return calculateProbabilities;
    }

    public boolean isProcessAllQuestions() {
        return processAllQuestions;
    }
}
