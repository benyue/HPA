# HPA
Hierarchical Probabilistic Automata Model Checker (HiPAM) is a tool based on algorithms for checking emptiness of languages accepted by 1-Hierarchical Probabilistic Automata (1-HPA). 1-HPA is a subclass of Probabilistic Automata (PA) in which the state space is stratified into two levels. The emptiness problem for 1-HPA has been shown to be decidable. HiPAM can be used to model check any open concurrent probabilistic system that can be modeled as a 1-HPA. In particular, it can be used to check the correctness of failure-prone open concurrent systems, under the condition that failures are modeled probabilistically and at most one failure occurs. HiPAM takes as input, a synchronous open concurrent program, its failure specification, and a correctness/incorrectness property specified by a deterministic automaton on finite strings or Buchi automaton on infinite strings. It checks the correctness of the given system by constructing a 1-HPA and checking the emptiness of its language under the given probability threshold. 

HiPAM is platform-independent as it is written in Java, and it comes with complete documentation. It also has an interface providing compatibility with PRISM, a popular model checker for general probabilistic systems.

[Download]

If you are not familiar with Git, please click "Download Zip" on the home page of the project to download everything including examples.

[Source Code]

Entrance: UI.UI_Menu

Yue (Cindy) Ben
