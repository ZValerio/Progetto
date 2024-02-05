package com.foodie.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CatalogoRicetteImplementazione2Dao implements CatalogoRicetteChefDao{  //DAO CON FILE SYSTEM
	
	private static CatalogoRicetteImplementazione2Dao istanza;    //SINGLETON
	private static final String PATH = "C:\\Users\\valba\\OneDrive\\Desktop\\Progetto\\Catalogo Ricette\\CatalogoRicette.txt";
	
	private CatalogoRicetteImplementazione2Dao(){
	}
	
	public static synchronized CatalogoRicetteImplementazione2Dao ottieniIstanza() { //METODO PER OTTENERE L'ISTANZA
		if(istanza==null) {
			istanza=new CatalogoRicetteImplementazione2Dao();
		}
		return istanza;
	}
	
	@Override
	public ArrayList<Ricetta> trovaRicette(Dispensa dispensa, int difficolta, String autore) throws Exception { //TROVA LE RICETTE NEL FILE O PER ALIMENTI-DIFFICOLTA' O PER AUTORE
		BufferedReader lettore = null;
	    ArrayList<String> linee = new ArrayList<>();
	    ArrayList<Ricetta> ricetteTrovate=new ArrayList<Ricetta>();
	    if(dispensa!=null && dispensa.getAlimenti().isEmpty()) { //CONTROLLO SE LA DISPENSA è VUOTA SE GLIELA FORNISCO
			System.out.println("Dispensa vuota!!! Riempila prima");
			return null;
		}
	    try {
	        lettore = new BufferedReader(new FileReader(PATH));  //MI CARICO TUTTE LE RIGHE DEL FILE
	        String linea;
	        while ((linea = lettore.readLine()) != null) {
	            linee.add(linea);
	        }
	        if (linee != null && !linee.isEmpty()) {
		        for (String s : linee) {
		            String[] campi = s.split(";");
		            if(dispensa!=null) { //SE FORNISCO LA DISPENSA SIGNIFICA CHE VOGLIO FARE LA QUERY PER ALIMENTI
						ArrayList<Alimento> alimentiDispensa= dispensa.getAlimenti();
		            	String[] alimenti= campi[4].split(",");
						if(controllaIngredienti(alimentiDispensa,alimenti)==true && Integer.parseInt(campi[3])==difficolta) {
							Ricetta ricetta=costruisciRicetta(campi);
							ricetteTrovate.add(ricetta);
						}
					}
					else { //SE NON LA FORNISCO E DO UN AUTORE SIGNIFICA CHE VOGLIO FARE LA QUERY PER AUTORE
						if(campi[1].equals(autore)) {
							Ricetta ricetta= costruisciRicetta(campi);
							ricetteTrovate.add(ricetta);
						}
					}
		        }
		        if(ricetteTrovate!=null && !ricetteTrovate.isEmpty()) {
		        	System.out.println("Ricette Trovate");
		        	return ricetteTrovate;
		        }
		    }
	        System.out.println("Nessuna ricetta trovata");
			return null;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	            if (lettore != null)
	                lettore.close();
	    }
	}
	
	private boolean controllaIngredienti(ArrayList<Alimento> alimentiDispensa,String[] alimenti) {//METODO PRIVATO CHE MI CONSENTE DI VEDERE SE LA DISPENSA CONTIENE GLI INGREDIENTI NECESSARI ALLA RICETTA
		for(String s:alimenti) {  //INGREDIENTI RICETTA
			for(int i=0;i<alimentiDispensa.size();i++) {  //ALIMENTI DISPENSA
				Alimento alimento= alimentiDispensa.get(i);
				if(alimento.getNome().equals(s)) {
					break;
				}
				else if(i==alimentiDispensa.size()-1) {
					return false;
				}
			}
		}
		return true;
	}

	private Ricetta costruisciRicetta(String[] campi) {  //METODO PER LA COSTRUZIONE DELLA RICETTA DALLA STRINGA OTTENUTA DAL FILE
		Ricetta ricetta = new Ricetta(campi[0], campi[2],Integer.parseInt(campi[3]), new ArrayList<Alimento>(), campi[1], new ArrayList<String>());
		String[] alimenti= campi[4].split(",");
    	String[] quantita= campi[5].split(",");
    	for(int i=0;i<alimenti.length;i++) {
    		Alimento alimento=new Alimento(alimenti[i]);
    		ricetta.aggiungiIngrediente(alimento, quantita[i]);
    	}
    	return ricetta;
	}
	
	@Override
	public void aggiungiRicetta(Ricetta ricetta) throws Exception { //METODO PER AGGIUNGERE LA RICETTA SUL FILE SE NON PRESENTE
		BufferedWriter scrittore=null;    //FORMATTAZIONE NOME;AUTORE;DESCRIZIONE;DIFFICOLTA;ALIMENTO1,ALIMENTO2;QUANTITA'1,QUANTITA'2.
		if(controllaSeEsistente(ricetta.getNome(),ricetta.getAutore())==true) {
			ricettaDuplicataException eccezione= new ricettaDuplicataException("Ricetta già esistente nel file!");
        	throw eccezione;
		}
		try {
			scrittore= new BufferedWriter(new FileWriter(PATH,true));
			String nome=ricetta.getNome();
			String descrizione=ricetta.getDescrizione();
			String autore=ricetta.getAutore();
			int difficolta=ricetta.getDifficolta();
			String alimenti="";
			String quantita="";
			for(int i=0;i<ricetta.getIngredienti().size();i++) {
				Alimento alimento= ricetta.getIngredienti().get(i);
				alimenti=alimenti+alimento.getNome();
				quantita=quantita+ricetta.getQuantita().get(i);
				if(i!=ricetta.getIngredienti().size()-1) {
					alimenti=alimenti+",";
					quantita=quantita+",";
				}
			}
			scrittore.write(nome+";"+autore+";"+descrizione+";"+Integer.toString(difficolta)+";"+alimenti+";"+quantita);
			scrittore.newLine();
			System.out.println("Ricetta aggiunta al database");
		}catch(IOException e) {
			e.printStackTrace();
			System.err.println("Ricetta non aggiunta al database");
		}finally {
			if(scrittore!=null)
				scrittore.close();
		}
	}

	private boolean controllaSeEsistente(String nome,String autore) {  //METODO PER VERIFICARE SE LA RICETTA è GIA' PRESENTE NEL FILE
		BufferedReader lettore = null;
		ArrayList<String> linee = new ArrayList<>();
		try {
	        lettore = new BufferedReader(new FileReader(PATH));
	        String linea;
	        while ((linea = lettore.readLine()) != null) {
	            linee.add(linea);
	        }
	        if(linee!=null && !linee.isEmpty()) {
	        	for (String s : linee) {
		            String[] campi = s.split(";");
		            if(campi[0].equals(nome) && campi[1].equals(autore)) {
		            	return true;
		            }
		        }
	        	return false;
	        }
	        else {
	        	return false;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	            if (lettore != null)
					try {
						lettore.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	    }
	}

	@Override
	public void eliminaRicetta(String nome, String autore) throws Exception {  //METODO PER ELIMINARE LA RICETTA DAL FILE
		BufferedReader lettore = null;
	    BufferedWriter scrittore = null;
	    ArrayList<String> lineeVecchie = new ArrayList<>();
        ArrayList<String> lineeNuove = new ArrayList<>();
	    try {
	        lettore = new BufferedReader(new FileReader(PATH));
	        String linea;
	        while ((linea = lettore.readLine()) != null) {
	            lineeVecchie.add(linea);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	            if (lettore != null)
	                lettore.close();
	    }
	    if (lineeVecchie != null && !lineeVecchie.isEmpty()) {
	        for (String s : lineeVecchie) {
	            String[] campi = s.split(";");
	            if (!campi[0].equals(nome) || !campi[1].equals(autore)) {
	                lineeNuove.add(s);
	            }
	        }
	    }
	    try {
	        scrittore = new BufferedWriter(new FileWriter(PATH));
	        if (lineeNuove != null && !lineeNuove.isEmpty()) {
	            for (String s : lineeNuove) {
	                scrittore.write(s);
	                scrittore.newLine();
	            }
	            System.out.println("Ricetta eliminata dal database");
	        }
	        else {
	        	System.out.println("Ricetta non presente nel database");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.err.println("Ricetta non eliminata dal database");
	    } finally {
	        try {
	            if (scrittore != null)
	                scrittore.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

	@Override
	public Ricetta ottieniDatiRicetta(String nome, String autore) throws Exception {  //METODO PER OTTENERE I DATI DELLA RICETTA SPECIFICA
		BufferedReader lettore = null;
	    ArrayList<String> linee = new ArrayList<>();
	    try {
	        lettore = new BufferedReader(new FileReader(PATH));  //MI CARICO TUTTE LE RIGHE DEL FILE
	        String linea;
	        while ((linea = lettore.readLine()) != null) {
	            linee.add(linea);
	        }
	        if (linee != null && !linee.isEmpty()) {
		        for (String s : linee) {
		            String[] campi = s.split(";");
					if(campi[0].equals(nome) && campi[1].equals(autore)) {
						Ricetta ricetta= costruisciRicetta(campi);
						return ricetta;
					}
		        }
		        return null;
		    }
	        else {
	        	return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	            if (lettore != null)
	                lettore.close();
	    }
	}

}