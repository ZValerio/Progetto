package com.foodie.model;

import java.sql.*;
import java.util.ArrayList;

public class CatalogoRicetteImplementazioneDao implements CatalogoRicetteChefDao{
	private static CatalogoRicetteImplementazioneDao istanza; 
	private static String utente = "root";
    private static String password = "root"; 
    private static String databaseUrl = "jdbc:mysql://localhost:3306/ricette";
    private static String driverMySql = "com.mysql.jdbc.Driver";
	private CatalogoRicetteImplementazioneDao(){
	}
	public static CatalogoRicetteImplementazioneDao ottieniIstanza() { //METODO PER OTTENERE L'ISTANZA
		if(istanza==null) {
			istanza=new CatalogoRicetteImplementazioneDao();
		}
		return istanza;
	}
	@Override
	public ArrayList<Ricetta> trovaRicetta(Dispensa dispensa, int difficolta) throws Exception{
		ArrayList<Ricetta> ricetteTrovate=new ArrayList<Ricetta>();
		Statement dichiarazione=null;
		Connection connessione=null;
		ResultSet risultati=null;
		try {
			Class.forName(driverMySql);
			connessione= DriverManager.getConnection(databaseUrl, utente,password);
			dichiarazione = connessione.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			risultati= querySQL.trovaRicette(dichiarazione,dispensa.getAlimenti(),difficolta);
			while (risultati.next()) {
		        String nomeRicetta = risultati.getString("nome");
		        String descrizione = risultati.getString("descrizione");
		        int difficoltaRicetta = risultati.getInt("difficolta");
		        String autore = risultati.getString("autore");
		        Ricetta ricetta = new Ricetta(nomeRicetta, descrizione, difficoltaRicetta, new ArrayList<Alimento>(), autore, new ArrayList<String>());
		        do {
		            String alimento = risultati.getString("alimento");
		            String quantita = risultati.getString("quantita");
		            ricetta.aggiungiIngrediente(new Alimento(alimento), quantita);
		        } while (risultati.next() && nomeRicetta.equals(risultati.getString("nome")));
		        ricetteTrovate.add(ricetta);
		    }
			dichiarazione.close();
			connessione.close();
			risultati.close();
			return ricetteTrovate;
		}finally {
			if (dichiarazione != null)
                dichiarazione.close();
            if (connessione != null)
                connessione.close();
            if (risultati != null)
                connessione.close();
		}
	}
	@Override
	public void aggiungiRicetta(Ricetta ricetta) throws Exception {
		Statement dichiarazione = null;
        Connection connessione = null;
        ResultSet risultati= null;
        try {
            Class.forName(driverMySql);
            connessione = DriverManager.getConnection(databaseUrl, utente, password);
            dichiarazione = connessione.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            risultati = querySQL.selezionaRicetteDallaDescrizione(dichiarazione);
            while (risultati.next()) {
                String descrizione = risultati.getString("descrizione");
                System.out.println("Trovata descrizione: "+ descrizione);
                if (descrizione.equals(ricetta.getDescrizione())){
                	ricettaDuplicataException eccezione= new ricettaDuplicataException("ricetta già esistente nel database!");
                	throw eccezione;
                }
            }
            risultati.close();
            dichiarazione.close();
            dichiarazione = connessione.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            if(querySQL.inserisciRicetta(dichiarazione, ricetta)>0) {
            	System.out.println("ricetta aggiunta al database");
            }
            else {
            	System.out.println("ricetta non aggiunta al database o solo parzialmente!");
            }
        } finally {       	
                if (dichiarazione != null)
                    dichiarazione.close();
                if (connessione != null)
                    connessione.close();
                if(risultati != null)
                	risultati.close();
        }
	}
	@Override
	public void eliminaRicetta(Ricetta ricetta) throws Exception {
		Statement dichiarazione = null;
        Connection connessione = null;
        try {
            Class.forName(driverMySql);
            connessione = DriverManager.getConnection(databaseUrl, utente, password);
            dichiarazione = connessione.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            if(querySQL.rimuoviRicetta(dichiarazione, ricetta)>0) {
            	System.out.println("ricetta eliminata dal database");
            }
            else {
            	System.out.println("ricetta non eliminata dal database o non presente");
            }
            dichiarazione.close();
            connessione.close();
        } finally {       	
                if (dichiarazione != null)
                    dichiarazione.close();
                if (connessione != null)
                    connessione.close();
        }
	}
	@Override
	public void aggiornaRicetta(Ricetta ricetta) throws Exception {  //poi si implementerà meglio!
		eliminaRicetta(ricetta);
		aggiungiRicetta(ricetta);
		System.out.println("ricetta aggiornata!");
	}
}