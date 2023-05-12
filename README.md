# Illustrated Baseline for CG:SHOP 2023 - Convex Covers

<p align="center">
  <img src="lib/cover.gif" width="300">
</p>

This repository contains code that provides and illustrates a basic approach to the 
[CG:SHOP 2023](https://cgshop.ibr.cs.tu-bs.de/competition/cg-shop-2023) competition, which 
involves computing small convex covers of arbitrary polygons with holes. This is also my final project for the
CS263 Computational Geometry course at Tufts. 

- [Approach](#approach)
- [Installation and Usage](#installation-and-usage)
- [Bibliography](#bibliography)

## Approach

The approaches described in the overview paper published by the organizers of the CG:SHOP as well as those outlined in
the preprint by one of the winning teams (see bibliography below) start the search for a small convex cover with a 
triangulation of the input polygon. The teams that won CG:SHOP competition used constrained 
Delaunay triangulation but I decided to perform ear-clipping, since it turned out to be much easier to implement.
Even so, ear-clipping requires the polygon to be simple, and the competition allows instances to have holes. To remedy
this, I connect each hole to a vertex on the boundary of the polygon or the boundary of another hole that is visible 
from the given hole. This then allows me to perform triangulation by ear-clipping.

Once the polygon is triangulated, I perform an operation described by the authors of the competition in reference to
their benchmark solution: I randomly select pairs of convex pieces in my cover and check if their convex hull is
contained within the target polygon. If it is, I can remove the two pieces that formed the convex hull from the cover 
and add he hull itself as a single piece, thereby decreasing the number of pieces in he cover. If I fail to
find a new pair, whose convex hull would be within the polygon, after 10000 attempts (this number is configurable via 
command-line), I terminate the process and return the current cover. Performing a check of whether or not a convex piece
is within a polygon with holes turned out to be the most difficult part of the project and I believe I might still
be missing some edge cases that have to do with points not being in general position (although, as far as I can tell, 
my tool produces correct results on the instances in this repository).

The output of the program are .png files (of configurable size) that show the original polygon, its transformation into
a simple polygon (if it had holes), ts triangulation, and the final cover found by the tool. It is also possible, using
the `--gif` flag (see below) to get a step-by-step animation, like the one on top of this README, of the cover size 
reduction procedure I describe in the above paragraph.

All the geometric operations in the project are written by me, with the exception of Graham Scam, which I largely 
borrowed from [here](https://www.geeksforgeeks.org/convex-hull-using-graham-scan/) (I did have to modify this code as 
well to account for collinear points).

This repository also hosts the [slides](Presentation.pdf) I made in April 2023 on the Minimal Cover Problem as part of
CS263.

## Installation and Usage

The only requirement to run the code is to have Java SDK >= 1.8 installed on your machine.
To automatically compile and bundle the sources into `Cover.jar` archive, run:

```bash
make
```

To execute the program, run 

```bash
java -jar Cover.jar -file=PROBLEM.json
```

where `PROBLEM.json` is an input file that follows the format of problem instances from the CG:SHOP 2023 competition, 
as described [here](https://cgshop.ibr.cs.tu-bs.de/competition/cg-shop-2023/#instance-format). 
The [instances](instances) directory of this repository contains several example problems from the
competition, for example you can run the following command to generate images that form the .gif animation above 
(and follow the algorithm for reducing the size of a convex cover):

```bash
java -jar Cover.jar -file=instances/socg60.instance.json --gif
```

To get a full list of available options, run `java -jar Cover.jar -h`.

## Bibliography

Fekete, S. P., Keldenich, P., Krupke, D., & Schirra, S. (2023). [Minimum Coverage by Convex Polygons: The CG: SHOP Challenge 2023.](https://arxiv.org/pdf/2303.07007.pdf)

da Fonseca, G. D. (2023). [Shadoks Approach to Convex Covering](https://arxiv.org/pdf/2303.07007.pdf). 

O'Rourke, J. (1982). [The complexity of computing minimum convex covers for polygons](https://www.computational-geometry.org/documents/MinConvexCovers_Allerton_1982.pdf). In Proc. 20th Annu. Allerton Conf. on Communication, Control, and Computing, Allerton, IL, 1982 (pp. 75-84).

Chew, L. P. (1987, October). [Constrained delaunay triangulations](https://dl.acm.org/doi/pdf/10.1145/41958.41981). In Proceedings of the third annual symposium on Computational geometry (pp. 215-222).

Chazelle, B., & Dobkin, D. (1979, April). [Decomposing a polygon into its convex parts](https://dl.acm.org/doi/pdf/10.1145/800135.804396). In Proceedings of the eleventh annual ACM symposium on Theory of computing (pp. 38-48).