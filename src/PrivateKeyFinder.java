import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class PrivateKeyFinder 
{
	/* Calculates GCD of e and eulerN recursively using the Euclidean Algorithm. Solution borrowed from Princeton University:
	http://introcs.cs.princeton.edu/java/78crypto/ExtendedEuclid.java.html */
	private static long[] gcd(long e, long eulerN) {
                if (eulerN == 0) { return new long[] { e, 1, 0 }; }
                long[] vals = gcd(eulerN, e % eulerN);
                long d = vals[0];
                long a = vals[2];
                long b = vals[1] - (e / eulerN) * vals[2];
                return new long[] { d, a, b };
	}
					
	// Calculates p and q using the quadratic formula
	private static long[] calculatePQQuad(long n, long eulerN) {
		long[] pq = new long[2]; 
		long b = n - eulerN + 1;
		long c = n;
		pq[0] = (long)(b + Math.sqrt((double)b*(double)b - 4*(double)c)) / 2;
		pq[1] = (long)(b - Math.sqrt((double)b*(double)b - 4*(double)c)) / 2;
		return pq;
	}
	
	// Calculates Euler Totient Function for n
	private static long getEulerTotient(long n) {	
		long count = 0;
		ArrayList<Long> factors = new ArrayList<Long>();
		if (n % 2 == 0) {factors.add(2L);}
		if (n % 3 == 0) {factors.add(3L);}
		if (n % 5 == 0) {factors.add(5L);}
		for (int i = 2; i < n; i++) {
		boolean multOfFactor = false;
			for (int j = 0; j < factors.size(); j++) {
				if (i % factors.get(j) == 0) {
					multOfFactor = true;
					break;
				}
			}
			if (multOfFactor) {continue;}	
			else if (n % i == 0) {
				factors.add((long)i); 
			} else {
				count++;
			}
		}
		return count + 1;
	}
        
        // Provides sample values for n and e 
        private static long[] getSampleValues() {
                Random r = new Random();
                // Primes
                long[] sampleE = {813283, 47363, 29287, 709, 10739591, 37811, 19, 2269, 6113, 383, 1877, 13903, 983, 2545861, 664123, 
                                29, 67, 241, 11842807, 7, 2311, 7577, 3373, 431, 45887};
                // Corresponding semiprimes
                long[] sampleN = {33582751, 128865743, 54569519, 56440001, 48588341, 782303, 928639, 528109, 44801, 626, 6109, 65591, 
                                7387, 22915111, 8702927, 9332989, 77, 2491, 249, 21551749, 15, 4234403, 1012627051, 253301693, 446, 460584613};
                int i = r.nextInt(sampleE.length);
                long[] encryptionKey = {sampleE[i], sampleN[i]};
                return encryptionKey;
        }
	
	public static void main(String[] args) {
	
		long n, eulerN;
		long p, q;
		long e, d;
		long startTime, endTime, elapsed;
		long[] pq, vals;
		
                // Initializing values for user input
                Scanner reader = new Scanner(System.in);
                String input = "";
                
                long[] sampleEncryptionKey;
                boolean nSet = false;  
                
                n = 0; 
                e = 0;
                
                // Get input for n and e 
                System.out.println("Please enter a modulus (n). It must be a semiprime!\n"
                        + "A semiprime is the product of two prime numbers - i.e. it's only factors are 1, itself, and two primes.\n"
                        + "[press Enter for sample values]:");
                while (!input.isEmpty() || !tryParse(input)) {
                    input = reader.nextLine();
                    if (input.isEmpty()) {
                        sampleEncryptionKey = getSampleValues();
                        e = sampleEncryptionKey[0];
                        n = sampleEncryptionKey[1];
                        break;
                    } else if (tryParse(input)) {
                        if (!nSet) {
                            n = Long.parseLong(input);
                            nSet = true;
                            System.out.println("\nPlease enter the encryption exponent (e).\n"
                                + "Choosing a prime number for e will ensure that it has a multiplicative inverse (d).\n"
                                + "[press Enter for sample values]:");
                            continue;
                        }
                        if (nSet) {
                            e = Long.parseLong(input);
                            break;
                        }    
                    }
                }
       
                // Values have not been set
                if (n == 0 || e == 0) {
                    System.out.println("\nInvalid encryption key!");
                    System.exit(0);
                }
                
                // Print input (encryption key)
                System.out.println("\nPublic Encryption Key: ("+e+", "+n+")\n");
                
		// Calculate ϕn given n
		System.out.println("Calculating ϕn from n:");
		startTime = System.nanoTime();
		eulerN = getEulerTotient(n);
		endTime = System.nanoTime();
		elapsed = endTime - startTime;
		System.out.println("Elapsed time = "+elapsed+" nanoseconds "+"("+elapsed/1000000000.0+" seconds)");	
		System.out.println("n = "+n+"\nϕn = "+eulerN+"\n");
		
		// Calculate p and q given n and ϕn
		System.out.println("Calculating p and q using quadratic formula:");
		startTime = System.nanoTime();
		pq = calculatePQQuad(n, eulerN);
		endTime = System.nanoTime();
		elapsed = endTime - startTime;
		p = pq[0];
		q = pq[1];
		if (p*q != n) {
			System.out.println("\nInvalid modulus! n is not a semiprime!");
			System.exit(0);
		} else {
			System.out.println("Elapsed time = "+elapsed+" nanoseconds "+"("+elapsed/1000000000.0+" seconds)");	
			System.out.println("p = "+p+"\nq = "+q+"\n");
		}	
		
		// Use the Extended Euclidean Algorithm to compute the GCD of ϕn and e, as well as the multiplicative inverse of e mod ϕn
		System.out.println("Computing GCD using Euclidean Algorithm for "+eulerN+" and "+e+":");
		startTime = System.nanoTime();
		vals = gcd(e, eulerN);
                if (vals[0] != 1) {
                    System.out.println("\nInvalid encryption key! ϕn is not a multiple of e!");
                    System.exit(0);
                }
		endTime = System.nanoTime();
		elapsed = endTime - startTime;
		d = vals[1];
		if (d < 0) {
			d = d + eulerN;
		}
		System.out.println("Elapsed time = "+elapsed+" nanoseconds "+"("+elapsed/1000000000.0+" seconds)");	
		System.out.println("GCD("+e+", "+eulerN +") = "+vals[0]);
                System.out.println(vals[1]+"(" + e + ") + "+vals[2]+"(" + eulerN + ") = "+vals[0]+"\n");
                
                // Final ouput
                System.out.println("e = "+e);
                System.out.println("d = "+vals[1]+" % "+eulerN+" = "+d+"\n");
                System.out.println("Public Encryption Key: ("+e+", "+n+")");
                System.out.println("Private Decryption Key: ("+d+", "+n+")");
        }
        
        // Attempts to parse a variable of type long from a given String, returns true or false
        private static boolean tryParse(String s) {
                try {
                    long l = Long.parseLong(s);
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
        }
}


