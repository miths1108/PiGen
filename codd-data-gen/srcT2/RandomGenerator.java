import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RandomGenerator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc=new Scanner(System.in);
		List<String> queriesF=new ArrayList<>();
		System.out.println("Enter n");
		int n=sc.nextInt();
		System.out.println("Enter queries");
		while(n-->=0) {
			queriesF.add(sc.nextLine());
		}
		
		
		for(int i=0;i<3;i++) {
			Collections.shuffle(queriesF);
			System.out.println("i="+i);
			System.out.println("set1");
			for(int j=0;j<queriesF.size()/2;j++)
				System.out.println(queriesF.get(j));
			System.out.println("set2");
			for(int j=queriesF.size()/2;j<queriesF.size();j++)
				System.out.println(queriesF.get(j));
		}
	}

}
