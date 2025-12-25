# fluid-sim-col
Coloured fluid simulator made in pure Java. Click and drag to add fluid.

## Overview

This is a real-time, grid-based 2D fluid simulation. Dragging the mouse injects **dye** and **velocity** into the field; the simulation then transports (advects) and diffuses them over time to produce the swirling “ink in water” effect.

## Demo

YouTube: https://www.youtube.com/watch?v=aPf_i_lAKT4

## Controls

- Mouse drag: add dye + push fluid (adds velocity)
- `Space`: toggle velocity vector view
- `C`: randomize dye colour cycling
- `Esc`: quit

## Running

Main entry point:
- `src/graphics/Fluid.java` (`public static void main`)

From the repo root (example):
```bash
javac -d bin src/utility/*.java src/graphics/*.java
java -cp bin graphics.Fluid
```

## Implementation notes

- The simulation runs on a 2D grid storing:
  - velocity field (`Vec2[][]`)
  - dye/colour field (`Color[][]`)
- Each tick performs diffusion + incompressibility projection + advection (plus optional fading).
- Rendering is done with Swing (`JFrame` + `JPanel`) by writing the dye field into a `BufferedImage`.

## References / Inspiration

- Inspecto — “But How DO Fluid Simulations Work?”  
  https://www.youtube.com/watch?v=qsYE1wMEMPA
- Jos Stam — *Real-Time Fluid Dynamics for Games*  
  https://www.researchgate.net/publication/2560062_Real-Time_Fluid_Dynamics_for_Games
- Mike Ash — “Fluid Simulation for Dummies”  
  https://www.mikeash.com/pyblog/fluid-simulation-for-dummies.html
- The Coding Train — “Coding Challenge 132: Fluid Simulation”  
  https://www.youtube.com/watch?v=alhpH6ECFvQ
