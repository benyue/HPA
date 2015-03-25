package Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import HPA.CreateHPA;
import HPA.CreateHPAState;
import HPA.CreateHPATransition;
import HPA.HPANode;
import HPA.PA;
import HPA.PATransition;
import HPA.edge;

public class io {

	/**
	 * Print out states and transitions of the PA to the console
	 * 
	 * @param message
	 *            any message on the first line of the graph
	 * */
	public static void printPA2console(CreateHPA g, String message) {
		System.out.println("\n" + message);
		System.out.println(g.name + " has " + g.V.size() + " states, " + "and "
				+ g.T.size() + " transitions.");
		System.out.println("Initial states: " + g.Q0.toString() + ";\n"
				+ "Final states: " + g.F.toString());
		System.out.println("Symbols: " + g.symbols);
		System.out.println("States (" + g.V.size()+" totally):");
		for (CreateHPAState s : g.V) {
			// printState(s);
			System.out.println("State id=" + s.id + ": value=" + s.name
					+ " out-trans=" + s.outTransID.toString() + " inputs="
					+ s.inputs + " Prop=" + s.prop);
		}
		System.out.println("Transitions (" + g.T.size()+" totally):");
		for (CreateHPATransition t : g.T) {
			// printTransition(t);
			System.out.println("Tran id=" + t.id + ": "
					+ g.V.get(t.sourceID).name + " - " + t.input + " -> "
					+ g.V.get(t.endID).name + " w/ pr="
					+ t.pr.toString().replaceAll(" ", "") + " Prop=" + t.propSatisfied);
		}
	}

	/**
	 * Output the whole PA into a plain file.
	 * 
	 * @throws IOException
	 * */
	public static void writePA2file(CreateHPA g, String filename, String comment)
			throws IOException {
		File file = new File(filename);
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		o.write(g.V.size() + "\n" + comment);
		o.write(g.name + " w/ prefix "+g.prefix + " has " + g.V.size() + " states, "+
				g.T.size() + " transitions, and "+g.symbols.size()+" symbols.\n");
		o.write("Initial states: " + g.Q0.toString() + ";\n" + "Final states: "
				+ g.F.toString() + "\n");
		o.write("Symbols (" + g.symbols.size()+" totally):\n"
				+ g.symbols + "\n");
		o.write("States (" + g.V.size()+" totally):\n");
		for (CreateHPAState s : g.V) {
			// printState(s);
			o.write("State id=" + s.id + ": value=" + s.name + " out-trans="
					+ s.outTransID.toString() + " inputs=" + s.inputs
					+ " Prop=" + s.prop + "\n");
		}
		o.write("Transitions (" + g.T.size()+" totally):\n");
		for (CreateHPATransition t : g.T) {
			// printTransition(t);
			o.write("Tran id=" + t.id + ": " + g.V.get(t.sourceID).name + " - "
					+ t.input + " -> " + g.V.get(t.endID).name + " w/ pr="
					+ t.pr.toString().replaceAll(" ", "") 
					+ " PropSatisfied=" + t.propSatisfied
					+ " PropRequired=" + t.propRequired
					+ "\n");
		}
		o.close();
		// System.out.println(g.name + " written to file.");
	}

	/**
	 * Output the PA to a FAT tool format.
	 * http://cl-informatik.uibk.ac.at/software/fat/grammar.php
	 * 
	 * @throws IOException
	 * */
	public static void writeNFA2FAT(CreateHPA g, String filename, String comment)
			throws IOException {
		File file = new File(filename);
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		Set<String> symbols = new HashSet<String>();
		o.write("//" + comment + "\n");
		o.write("NFA " + g.name + " = {\n");
		// states
		o.write("states = {");
		for (CreateHPAState s : g.V) {
			//o.write(s.id + "_" + s.name + " ");
			o.write(s.name + " ");
		}
		o.write("}\n");
		o.write("//States.size()=" + g.V.size() + "\n");

		// transitions
		o.write("transitions = {\n");
		o.write("//Transitions.size()=" + g.T.size() + "\n");
		for (CreateHPATransition t : g.T) {
			// e.g. q2-a->q2
			// e.g. q1-b->{q2 q0}
			String input = //t.id+"000"+t.input + "" + t.pr.toString().replace(" / ", "00");
					t.input;
			/*o.write(g.V.get(t.sourceID).id + "_" + g.V.get(t.sourceID).name
					+ "-" + input + "->" + g.V.get(t.endID).id + "_"
					+ g.V.get(t.endID).name);
					*/
			o.write(g.V.get(t.sourceID).name
					+ "-" + input + "->" + g.V.get(t.endID).name);
			symbols.add(input);
			o.write("\n");
		}
		o.write("}\n");
		// alphabet
		o.write("alphabet = {");
		for (String s : symbols) {
			o.write(s + " ");
		}
		o.write("}\n");
		// initial state
		o.write("initial state = ");
		for (int i : g.Q0) {
			//o.write(g.V.get(i).id + "_" + g.V.get(i).name);
			o.write(g.V.get(i).name);
			break;// TODO: only 1 initial state allowed
		}
		o.write("\n");
		// final states
		o.write("final states = {");
		for (int i : g.F) {
			//o.write(g.V.get(i).id + "_" + g.V.get(i).name + " ");
			o.write(g.V.get(i).name + " ");
		}
		o.write("}\n");

		o.write("}\n");
		o.close();
		// System.out.println(g.name + " written to file.");
	}

	/**
	 * Output the PA to simple HPA tool format, note state id is presented
	 * instead of value.
	 * 
	 * @throws IOException
	 * */
	public static void write2HPA(CreateHPA g, String filename, String comment)
			throws IOException {
		File file = new File(filename);
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		o.write(g.V.size() + "\n");
		// states
		o.write("//States.size()=" + g.V.size() + "\n");
		for (CreateHPAState s : g.V) {
			o.write(s.name + " ");
			for (String p : s.prop) {
				o.write("#" + p + " ");
			}
			o.write("\n");
		}
		// alphabet
		// transitions
		o.write("//Transitions.size()=" + g.T.size() + "\n");
		for (CreateHPAState s : g.V) {
			for (String in : s.inputs) {
				o.write(s.id + " " + in + " ");
				for (int oid : s.outTransID) {
					CreateHPATransition t = g.T.get(oid);
					if (t.input.equals(in)) {
						o.write(t.endID + " "
								+ t.pr.toString().replaceAll(" ", "") + " ");
					}
				}
				o.write("\n");
			}
		}
		// initial state
		// final states

		o.close();
		// System.out.println(g.name + " written to file.");
	}
	
	/**
	 * Output the PA to a FAT tool format.
	 * http://cl-informatik.uibk.ac.at/software/fat/grammar.php
	 * 
	 * @throws IOException
	 * */
	public static void writePA2FAT(PA g, String filename, String comment)
			throws IOException {
		File file = new File(filename);
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		Set<String> symbols = new HashSet<String>();
		o.write("//" + comment + "\n");
		o.write("NFA hpa = {\n");
		// states
		o.write("states = {");
		for (HPANode s : g.V) {
			//o.write(s.id + "_" + s.name + " ");
			o.write(s.name + " ");
		}
		o.write("}\n");
		o.write("//States.size()=" + g.V.size() + "\n");

		// transitions
		o.write("transitions = {\n");
		int TSize = 0;
		for (HPANode s : g.V) {
			for(PATransition t:s.out_transitions){
				String input = t.input;
				for(edge e:t.distribution){
					o.write(s.name
							+ "-" + input + "->" + g.V.get(e.node).name);
					TSize++;
					o.write("\n");
				}
				symbols.add(input);
			}
		}
		o.write("}\n");
		o.write("//Transitions.size()=" + TSize + "\n");
		// alphabet
		o.write("alphabet = {");
		for (String s : symbols) {
			o.write(s + " ");
		}
		o.write("}\n");
		// initial state
		o.write("initial state = ");
		for (int i : g.Q0) {
			//o.write(g.V.get(i).id + "_" + g.V.get(i).name);
			o.write(g.V.get(i).name);
			break;// TODO: only 1 initial state allowed
		}
		o.write("\n");
		// final states
		o.write("final states = {");
		for (int i : g.F) {
			//o.write(g.V.get(i).id + "_" + g.V.get(i).name + " ");
			o.write(g.V.get(i).name + " ");
		}
		o.write("}\n");

		o.write("}\n");
		o.close();
		// System.out.println(g.name + " written to file.");
	}

}
