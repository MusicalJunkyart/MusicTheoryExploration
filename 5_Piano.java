import java.util.ArrayList;

public class Piano {

	public static void main(String[] args) {
						
		ArrayList<Structure> list = Structure.allCombinations(3);
		for (Structure s : list) 
			System.out.println(s + " " + s.complexity());
		
	}
}
 