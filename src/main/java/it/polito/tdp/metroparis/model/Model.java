package it.polito.tdp.metroparis.model;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private List<Fermata> fermate;
	Map<Integer, Fermata> fermateIdMap ;
	
	private Graph<Fermata, DefaultEdge> grafo;

	public List<Fermata> getFermate() {
		if (this.fermate == null) {
			MetroDAO dao = new MetroDAO();
			this.fermate = dao.getAllFermate();
			
			this.fermateIdMap = new HashMap<Integer, Fermata>();
			for (Fermata f : this.fermate)
				this.fermateIdMap.put(f.getIdFermata(), f);

		}
		return this.fermate;
	}
	
	public List<Fermata> calcolaPercorso(Fermata partenza, Fermata arrivo) {
		creaGrafo() ;
		Map<Fermata, Fermata> alberoInverso = visitaGrafo(partenza);
		
		Fermata corrente = arrivo;
		List<Fermata> percorso = new ArrayList<>();
	
		while(corrente != null) {
			percorso.add(0, corrente); //inserisco in testa così che ogni volta che aggiungo 'corrente' nuovo si inserisce come primo elemento
			corrente = alberoInverso.get(corrente);  //se avessi scritto: percorso.add(corrente) avrei avuto percorso che va dalla stazione di arrivo a quella di partenza
		}
		
		return percorso ;
	}

	public void creaGrafo() {
		this.grafo = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);

//		Graphs.addAllVertices(this.grafo, this.fermate);
		Graphs.addAllVertices(this.grafo, getFermate());
		
		MetroDAO dao = new MetroDAO();
		
//		METODO 1 -> itero su ogni coppia di vertici..aggiungo
//		   gli archi facendo fare il lavoro al database, ma se i vertici 
//		   sono tanti come in questo caso (619), farò 619*619 query al database 
//		   e quindi il tempo di una risposta risulta elevato
		
//		for(Fermata partenza: fermate) {                     
//			for(Fermata arrivo: fermate) {                     
//				if(dao.isFermateConnesse(partenza, arrivo)) {   
//					this.grafo.addEdge(partenza, arrivo);      
//				}
//			}
//		}
		
//		METODO 2a -> dato ciascun vertice, trova i vertici ad esso
//		   adiacenti
//		   Il DAO mi restituisce una lista di interi (id numerici)
		
//		for(Fermata partenza: fermate) {
//			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza);
//				for(Integer i: idConnesse) {
//					Fermata arrivo = null;
//					for(Fermata f: fermate) {
//						if(f.getIdFermata()==i) {
//							arrivo = f;
//							break;
//						}
//					}
//				this.grafo.addEdge(partenza, arrivo);
//			}
//		}
//		
//		METODO 2b -> dato ciascun vertice, trova i vertici ad esso
//		   adiacenti
//Il DAO mi restituisce una lista di oggetti di tipo fermata ---> query annidata(sotto-query) p una join tra tabelle
		
//		for(Fermata partenza: fermate) {
//			List<Fermata> arrivi = dao.getFermateConnesse(partenza);
//			for(Fermata arrivo: arrivi) {
//				this.grafo.addEdge(partenza, arrivo);
//			}
//		}
		
//		METODO 2c -> dato ciascun vertice, trova i vertici ad esso
//		   adiacenti
//		   Il DAO mi restituisce una lista di id numerici, che converto in
//         oggetti tramite una Map<Integer, Fermata> --> Identity Map
		
//		for(Fermata partenza: fermate) {
//			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza);
//			for(int id: idConnesse) {
//				Fermata arrivo = fermateIdMap.get(id); //ti do un id numerico e con
//				this.grafo.addEdge(partenza, arrivo);  //la mappa vado a ricercare con la get l'oggetto di riferimento
//			}
//		}
		
//		METODO 3 -> faccio una sola query che mi restituisce una coppia di fermate
//				da collegare..delego quasi tutto il lavoro al database

		List<CoppiaId> fermateDaCollegare = dao.getAllFermateConnesse();
		for (CoppiaId coppia : fermateDaCollegare) {
			this.grafo.addEdge(fermateIdMap.get(coppia.getIdPartenza()), fermateIdMap.get(coppia.getIdArrivo()));
		}

//		System.out.println(this.grafo);
		System.out.println("Vertici = " + this.grafo.vertexSet().size());
		System.out.println("Archi   = " + this.grafo.edgeSet().size());
	}

	
	
	public Map<Fermata, Fermata> visitaGrafo(Fermata partenza) {
		GraphIterator<Fermata, DefaultEdge> visita = new BreadthFirstIterator<>(this.grafo, partenza); //visita in ampiezza
//		GraphIterator<Fermata, DefaultEdge> visita1 = new DepthFirstIterator<>(this.grafo, partenza);  //visita in profondità
		Map<Fermata,Fermata> alberoInverso = new HashMap<>() ;
		alberoInverso.put(partenza, null) ;
		
		visita.addTraversalListener(new RegistraAlberoDiVisita(alberoInverso, this.grafo));
		while (visita.hasNext()) {
			Fermata f = visita.next();
      		System.out.println(f);
		}
		
		
		// Ricostruiamo il percorso a partire dall'albero inverso (pseudo-code)
//		List<Fermata> percorso = new ArrayList<>() ;
//		fermata = arrivo
//		while(fermata != null)
//			fermata = alberoInverso.get(fermata)
//			percorso.add(fermata)
		return alberoInverso;
	}

}
