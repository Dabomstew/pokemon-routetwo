import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//a trainer has a class name and some pokemon, corresponding to some location in memory
public class Trainer implements Battleable, Iterable<Pokemon> {
	private String className, name;
	private ArrayList<Pokemon> pokes;
	private int offset;
	private IVs dvs;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Trainer)) {
			return false;
		} else {
			return offset == ((Trainer) o).offset; // TODO check for more?
		}
	}

	@Override
	public void battle(Pokemon p, BattleOptions options) {
		for (Pokemon tp : pokes) {
			tp.battle(p, options);
		}
	}

	@Override
	public Iterator<Pokemon> iterator() {
		return pokes.iterator();
	}

	public String toString() {
		return String.format("%s %s (0x%X: %s)", className, name, offset,
				allPokes());
	}

	public String allPokes() {
		StringBuilder sb = new StringBuilder();
		for (Pokemon p : pokes) {
			sb.append(p.levelName() + ", ");
		}
		return sb.toString();
	}

	private static HashMap<Integer, Trainer> allTrainers;

	public static Trainer getTrainer(int offset) {
		if (!allTrainers.containsKey(offset))
			return null;
		else
			return allTrainers.get(offset);
	}

	// must be called before any other calls are made
	public static void initTrainers() {
		allTrainers = new HashMap<Integer, Trainer>();

		List<Trainer> trainerList = null;
		if (Settings.isGS)
			trainerList = getData("trainer_data_gs.txt");
		else
			trainerList = getData("trainer_data_c.txt");

		for (Trainer t : trainerList) {
			allTrainers.put(new Integer(t.offset), t);
		}
	}

	// reads trainer_data_(blue|yellow).txt to get trainer data
	private static List<Trainer> getData(String filename) {
		ArrayList<Trainer> trainers = new ArrayList<Trainer>();
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(System.class
					.getResource("/resources/" + filename).openStream()));

			String currentClassName = "";
			IVs currentIVs = new IVs();
			Trainer t;
			while (in.ready()) {
				String text = in.readLine();
				// names are formatted as [NAME]
				if (text.startsWith("[")) {
					// TODO: error checking is for noobs
					currentClassName = text.substring(1, text.length() - 1);
					continue;
				} else if (text.startsWith("0x")) { // line is a 0x(pointer):
													// list of pokes
					String[] parts = text.split(" "); // this should be length
														// 2+numpokes*7
					int offset = Integer.parseInt(parts[0].substring(2), 16);

					t = new Trainer();
					t.name = parts[1];
					t.className = currentClassName;
					t.offset = offset;
					t.pokes = new ArrayList<Pokemon>();
					t.dvs = currentIVs;

					// read off pokemon
					int pokecount = (parts.length - 2) / 7;
					for (int i = 0; i < pokecount; i++) {
						int pokoffset = 2 + i * 7;
						int number = Integer.parseInt(parts[pokoffset]);
						int level = Integer.parseInt(parts[pokoffset + 1]);
						int move1 = Integer.parseInt(parts[pokoffset + 3]);
						int move2 = Integer.parseInt(parts[pokoffset + 4]);
						int move3 = Integer.parseInt(parts[pokoffset + 5]);
						int move4 = Integer.parseInt(parts[pokoffset + 6]);
						Species s = Species.getSpecies(number);
						Moveset m = new Moveset();
						if (move1 != 0)
							m.addMove(move1);
						if (move2 != 0)
							m.addMove(move2);
						if (move3 != 0)
							m.addMove(move3);
						if (move4 != 0)
							m.addMove(move4);
						Pokemon pk = new Pokemon(s, level, m, currentIVs, false);
						t.pokes.add(pk);
					}
					trainers.add(t);
				} else {
					// new set of DVs
					String[] parts = text.split(" "); // this should be length 4
					// error checking is indeed for scrubs
					int atkIV = Integer.parseInt(parts[0]);
					int defIV = Integer.parseInt(parts[1]);
					int spdIV = Integer.parseInt(parts[2]);
					int spcIV = Integer.parseInt(parts[3]);
					currentIVs = new IVs(atkIV, defIV, spdIV, spcIV);
				}
			}
			in.close();
			return trainers;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public int getOffset() {
		return offset;
	}
}
