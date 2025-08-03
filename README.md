# Minesweeper

A feature-rich Minesweeper game implemented in Java using Swing for the GUI.

## How to Run

1. Make sure you have Java installed (JDK 8 or higher).
2. Open a terminal in the project directory.
3. Compile the code:
   ```
   javac MinesweeperGUI.java
   ```
4. Run the game:
   ```
   java MinesweeperGUI
   ```

## Features
- Multiple difficulty levels: Easy (9x9, 10 mines), Medium (16x16, 40 mines), Hard (16x30, 99 mines)
- High score tracking (best time saved between sessions)
- Timer (5 minutes limit for each game)
- Mines left counter
- Restart button for new games
- Flag mines with right-click (updates mines left counter)
- Reveal cells with left-click
- Game over and victory dialogs
- Menu bar for difficulty and high score display
- Responsive UI with dynamic grid resizing
- All mine locations revealed on loss or timeout

## Icons
Place `bomb.png` and `red-flag.png` in the `icons` folder for mine and flag graphics.

## License
MIT