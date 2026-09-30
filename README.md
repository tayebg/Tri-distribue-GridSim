# Tri-distribue GridSim — Distributed Sorting Simulator

A Java desktop application and simulation built on top of the GridSim toolkit. This project demonstrates distributed computing concepts by simulating a master node that divides a large list of elements and distributes the chunks to multiple worker nodes. The workers independently sort their sublists and return them to the master node, which merges them into a final sorted list.

## Features

- **Custom GUI Dashboard**: A modern, dark-themed Java Swing UI to configure, run, and monitor the simulation.
- **Distributed Sorting**: Implements a divide-and-conquer strategy over a simulated grid network.
- **GridSim Integration**: Utilizes the GridSim and SimJava2 frameworks to model network latency, bandwidth, and node processing.
- **Configurable Parameters**: Allows dynamic input of total elements (N) and the number of worker nodes (K).
- **Validation**: Compares the distributed sorted list against a standard Java sorted list to guarantee correctness.

## Tech Stack

- Java SE
- Java Swing / AWT
- GridSim Toolkit
- SimJava2

## Project Structure

```
Tri-distribue-GridSim/
├── src/                        # Java source files
├── lib/                        # GridSim and SimJava dependencies
├── bin/                        # Compiled classes
├── docs/                       # Project documentation and PDFs
├── run.bat                     # Windows build and run script
├── .classpath, .project        # Eclipse config files
├── README.md
└── LICENSE
```

## Getting Started

### Prerequisites

- JDK 8 or higher
- The included `gridsim.jar` and `simjava2.jar` files (located in `lib/`)

### Build & Run

**Windows users:** Simply run `run.bat` to compile and launch the application.

**Manual Build:**
Compile the Java files, including the required libraries in the classpath:

```bash
javac -cp "lib/gridsim.jar;lib/simjava2.jar" -d bin src/*.java
```

### Run

Run the application through the GUI:

```bash
java -cp "bin;lib/gridsim.jar;lib/simjava2.jar" SortingUI
```

## License

This project is licensed under the [MIT License](LICENSE).

## Author

[Tayeb Bekkouche](https://github.com/tayebg)
