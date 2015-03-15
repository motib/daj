## Overview ##
DAJ is an interactive, visual aid for studying distributed algorithms. Interactive, because you must explicitly specify every step of the interleaved execution sequence. Visual, because the state of the nodes is continuously displayed. Study aid, because they solve one of the most difficult problems encountered by students of these algorithms by automatically doing the necessary book-keeping. The program can create a log file of commands so that you can automatically replay scenarios until you understand them. Ten algorithms are current implemented, and you can implement other algorithms with only an elementary knowledge of Java. Visualizations are included for the virtual global structures contructed by some of the algorithms.

## References ##
M. Ben-Ari. Interactive Execution of Distributed Algorithms. _ACM Journal of Educational Resources in Computing_ 1(2), 2001.

The graphics display of the BG algorithm is described in: Ahuva Tikvati, Mordechai Ben-Ari, Yifat Ben-David Kolikant. Virtual trees for the Byzantine Generals algorithm. _Thirty-Fifth SIGCSE Technical Symposium on Computer Science Education_. Norfolk, VA, 2004.

## Algorithms implemented ##
Byzantine generals, Ricart-Agrawala mutual exclusion, Dijkstra-Scholten termination, Chandy-Lamport snapshots, Huang termination detection, Mattern termination detection, Suzuki-Kasami mutual exclusion, Neilsen-Mizuno mutual exclusion, Lamport mutual exclusion, Maekawa mutual exclusion, Carvalho-Roucairol mutual Exclusion, Failure by crashing, King of Byzantine generals.

## Acknowledgments ##
I wish to thank Judith Bishop of the University of Pretoria for her pioneering use of this software in her classes. University of Pretoria students Richard McGladdery, Frederick Kemp, Frank Harvie, Derick Burger, Darrell Newing and Leoni Lubbinge wrote several of the algorithms, which were adapted to version 2 by Basil Worrall. Basil also found some bugs in version 3. The virtual trees for the Byzantine Generals algorithm were designed by Ahuva Tikvati of the Weizmann Institute of Science and programmed by Antoine Pineau of the University of Joensuu. The visualization of the spanning tree of the Dijkstra-Scholten algorithm is based upon a program by Maor Zamsky. Otto Seppälä and Ville Karavirta of the Helsinki University of Technology implemented new algorithms.

## Other projects for teaching concurrency ##
Erigone Model Checker, jSpin development environment for Spin.