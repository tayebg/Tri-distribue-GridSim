public class SimulationRunner {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: SimulationRunner <N> <K>");
            System.exit(1);
        }

        int N, K;
        try {
            N = Integer.parseInt(args[0].trim());
            K = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException e) {
            System.err.println("ERROR: N and K must be valid integers.");
            System.exit(1);
            return;
        }

        if (N <= 0 || K <= 0) {
            System.err.println("ERROR: N and K must be greater than 0.");
            System.exit(1);
            return;
        }

        if (K > N) {
            System.err.println("ERROR: Number of workers K cannot exceed number of elements N.");
            System.exit(1);
            return;
        }

        // Run simulation - GridSim.init() is only called once per JVM lifetime,
        // so this is safe here since this process exits right after.
        MainSimulation.runSimulation(N, K);

        // Print structured summary markers for the UI to parse
        int generatedSize = (MasterNode.generatedData != null) ? MasterNode.generatedData.size() : 0;
        int finalSize     = (MasterNode.finalResult   != null) ? MasterNode.finalResult.size()   : 0;

        System.out.println("__SUMMARY_START__");
        System.out.println("N=" + N);
        System.out.println("K=" + K);
        System.out.println("GENERATED_SIZE=" + generatedSize);
        System.out.println("FINAL_SIZE=" + finalSize);
        System.out.println("GENERATED_LIST=" + (MasterNode.generatedData != null ? MasterNode.generatedData.toString() : "[]"));
        System.out.println("FINAL_LIST="     + (MasterNode.finalResult   != null ? MasterNode.finalResult.toString()   : "[]"));
        System.out.println("__SUMMARY_END__");
    }
}
