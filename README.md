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
├── ProjetGI/
│   ├── src/                    # Java source files
│   ├── libs/                   # GridSim and SimJava dependencies
│   ├── bin/                    # Compiled classes
│   └── .classpath, .project    # Eclipse config files
├── README.md
├── LICENSE
├── .gitignore
├── TP 2025-2026_292cc2e22873b57e575cc022a17255fb.pdf # Assignment description
└── Tri_distribue_GridSim.pdf   # Project presentation
```

## Getting Started

### Prerequisites

- JDK 8 or higher
- The included `gridsim.jar` and `simjava2.jar` files (located in `ProjetGI/libs/`)

### Build

1. Navigate to the `ProjetGI` directory.
2. Compile the Java files, including the required libraries in the classpath:

```bash
cd ProjetGI
javac -cp "libs/gridsim.jar;libs/simjava2.jar" -d bin src/*.java
```

### Run

Run the application through the GUI:

```bash
java -cp "bin;libs/gridsim.jar;libs/simjava2.jar" SortingUI
```

## License

This project is licensed under the [MIT License](LICENSE).

## Author

[Tayeb Bekkouche](https://github.com/tayebg)
