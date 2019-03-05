## Nostalgia

Nostalgia is a chess program that supports three main modes: **Human Vs Human**, in which the user can play with himself (or with the others); **Human Vs Computer**, in which the user gets to play with the machine; and **Computer Vs Computer**, in which two instances of the engine (that may have different levels) would play with each other.

![Human Vs Human sample game](/../screenshots/screenshots/sample_game.png?raw=true "Sample Human Vs Human Game")

Note: The AI implementation still needs a lot of improvements. Do not expect it to play at the same level as that of Stockfish.

## How To Run

Nostalgia is (primarily) written in Scala. So to run the program, you need to have a Java Runtime Environment running on your machine. Detailed setup instructions will be provided soon.

## Engine + GUI

Nostalgia has two main parts, the **Engine** and the **Graphical User Interface (GUI)**.

The engine contains the AI implementation and is divided into four sub-parts: _board representation_, _move generators_, _evaluators_, and _move searchers_. The GUI is the interface with which the user would interact while using the program.



## Technologies Used

**Programming Languages:** Scala, Java

**GUI Framework/Library:** JavaFX

**Version Control System:** Git

**Supported Platforms:** Linux, Mac, Windows