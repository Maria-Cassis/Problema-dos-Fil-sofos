import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class Filosofos {
    private static final int TOTAL_FILOSOFOS = 5;
    private static final int REFEICOES = 3;
    
    private static final Semaphore[] garfo = new Semaphore[TOTAL_FILOSOFOS];
    private static final ReentrantLock printLock = new ReentrantLock();
    
    static {
        for (int i = 0; i < TOTAL_FILOSOFOS; i++) {
            garfo[i] = new Semaphore(1);
        }
    }
    
    public static void pensar(int numero) {
        printSafely("Filósofo " + numero + " está pensando.");
        sleep(400);
    }
    
    public static void comer(int numero) {
        printSafely("Filósofo " + numero + " está comendo sua refeição.");
        sleep(600);
    }
    
    private static void printSafely(String mensagem) {
        printLock.lock();
        try {
            System.out.println(mensagem);
        } finally {
            printLock.unlock();
        }
    }
    
    private static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void cicloFilosofo(int numero) {
        int garfoEsq = numero;
        int garfoDir = (numero + 1) % TOTAL_FILOSOFOS;
        
        for (int refeicao = 0; refeicao < REFEICOES; refeicao++) {
            pensar(numero);
            
            try {
                if (numero % 2 == 0) {
                    garfo[garfoEsq].acquire();
                    garfo[garfoDir].acquire();
                } else {
                    garfo[garfoDir].acquire();
                    garfo[garfoEsq].acquire();
                }
                
                printSafely(String.format("Filósofo %d pegou os garfos %d e %d (refeição %d/%d)", 
                    numero, garfoEsq, garfoDir, refeicao + 1, REFEICOES));
                
                comer(numero);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                garfo[garfoDir].release();
                garfo[garfoEsq].release();
                printSafely(String.format("Filósofo %d devolveu garfos %d e %d.", numero, garfoEsq, garfoDir));
            }
        }
    }
    
    public static void main(String[] args) {
        Thread[] threads = new Thread[TOTAL_FILOSOFOS];
        
        for (int n = 0; n < TOTAL_FILOSOFOS; n++) {
            final int numero = n;  
            threads[n] = new Thread(() -> cicloFilosofo(numero), "Filósofo-" + numero);
            threads[n].start();
        }
        
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        printSafely("Todos terminaram suas refeições. Mesa liberada.");
    }
}
