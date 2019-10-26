// -*- coding: utf-8 -*-

import java.util.Random ;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

public class TriRapide {
    private static final int taille = 1_000_000 ;                   // Longueur du tableau à trier
    private static final int [] tableau = new int[taille] ;         // Le tableau d'entiers à trier
    private static final int [] tableau2 = new int[taille] ;         // La copie du tableau d'entiers à trier
    private static final int borne = 10 * taille ;                  // Valeur maximale dans le tableau

    private static CompletionService<Integer> verificateur;
    private static int count;

    static class triParallele implements Runnable {

    	int[] t;
    	int debut;
    	int fin;

    	triParallele(int[] t, int debut, int fin){
    		this.t = t;
    		this.debut = debut;
    		this.fin = fin;
    	}

    	public void run() {
    		trierRapidementParallele(t, debut, fin);
    	}
    }

    private static void echangerElements(int[] t, int m, int n) {
        int temp = t[m] ;
        t[m] = t[n] ;
        t[n] = temp ;
    }

    private static int partitionner(int[] t, int debut, int fin) {
        int v = t[fin] ;                               // Choix (arbitraire) du pivot : t[fin]
        int place = debut ;                            // Place du pivot, à droite des éléments déplacés
        for (int i = debut ; i<fin ; i++) {            // Parcours du *reste* du tableau
            if (t[i] < v) {                            // Cette valeur t[i] doit être à droite du pivot
                echangerElements(t, i, place) ;        // On le place à sa place
                place++ ;                              // On met à jour la place du pivot
            }
        }
        echangerElements(t, place, fin) ;              // Placement définitif du pivot
        return place ;
    }

    private static void trierRapidementSeq(int[] t, int debut, int fin) {
        if (debut < fin) {                             // S'il y a un seul élément, il n'y a rien à faire!
            int p = partitionner(t, debut, fin) ;
            trierRapidementSeq(t, debut, p-1) ;
            trierRapidementSeq(t, p+1, fin) ;
        }
    }

    private static void trierRapidementParallele(int[] t, int debut, int fin){
    	if (debut < fin) {                             // S'il y a un seul élément, il n'y a rien à faire!
            int p = partitionner(t, debut, fin) ;
            synchronized (TriRapide.class) {count += 1;}
            System.out.println(count);
            verificateur.submit(new triParallele(t, debut, p-1), null);
            verificateur.submit(new triParallele(t, p+1, fin), null);
            /*try {
        		verificateur.take();
        		verificateur.take();
        	} catch(InterruptedException e) {e.printStackTrace();}*/
        	//catch(ExecutionException e) {e.printStackTrace();}
        }
    }

    private static void afficher(int[] t, int debut, int fin) {
        for (int i = debut ; i <= debut+3 ; i++) {
            System.out.print(" " + t[i]) ;
        }
        System.out.print("...") ;
        for (int i = fin-3 ; i <= fin ; i++) {
            System.out.print(" " + t[i]) ;
        }
        System.out.print("\n") ;
    }

    public static void main(String[] args) {
        Random alea = new Random() ;
        for (int i=0 ; i<taille ; i++) {                          // Remplissage aléatoire du tableau
            tableau[i] = alea.nextInt(2*borne) - borne ;
            tableau2[i] = tableau[i] ;         
        }

        System.out.print("Tableau initial : ") ;
        afficher(tableau, 0, taille -1) ;                         // Affiche le tableau à trier
        System.out.print("Tableau copié : ") ;
        afficher(tableau2, 0, taille -1) ;                         // Affiche le tableau à trier

        System.out.println("Démarrage du tri rapide.") ;

        long debutDuTriSeq = System.nanoTime();
        trierRapidementSeq(tableau, 0, taille-1) ;                   // Tri du tableau
        long finDuTriSeq = System.nanoTime();

        count = 0;
        ExecutorService executeur = Executors.newFixedThreadPool(4);
        verificateur = new ExecutorCompletionService<Integer>(executeur);

        long debutDuTriPar = System.nanoTime();
        trierRapidementParallele(tableau2, 0, taille-1) ;                   // Tri du tableau
        long finDuTriPar = System.nanoTime();

        try {
            for (int i = 0; i < count; i++) {
                System.out.println("i: " + i);
                verificateur.take();
            }
        } catch(InterruptedException e) {e.printStackTrace();}

        executeur.shutdown();

        long dureeDuTriSeq = (finDuTriSeq - debutDuTriSeq) / 1_000_000 ;
        long dureeDuTriPar = (finDuTriPar - debutDuTriPar) / 1_000_000 ;
        System.out.print("Tableau trié : ") ; 
        afficher(tableau, 0, taille -1) ;                         // Affiche le tableau obtenu
        System.out.println("obtenu en " + dureeDuTriSeq + " millisecondes.") ;
        System.out.print("Tableau 2 trié : ") ; 
        afficher(tableau2, 0, taille -1) ;                         // Affiche le tableau obtenu
        System.out.println("obtenu en " + dureeDuTriPar + " millisecondes.") ;
    }
}
