//represents a battle, with planned statmods
public class Battle extends GameAction {
    private Battleable opponent;
    private BattleOptions options;
    
    
    public Battle(Battleable b) {
        opponent = b;
        options = new BattleOptions();
    }
    public Battle(Battleable b, BattleOptions options) {
        opponent = b;
        this.options = options;
    }
    
    public BattleOptions getOptions() {
        return options;
    }
    public StatModifier getMod1() {
        return options.getMod1();
    }
    public StatModifier getMod2() {
        return options.getMod2();
    }
    public int getVerbose() {
        return options.getVerbose();
    }
    
    public static Battle makeBattle(int offset) {
        return new Battle(Trainer.getTrainer(offset));
    }
    public static Battle makeBattle(int offset, BattleOptions options) {
        return new Battle(Trainer.getTrainer(offset), options);
    }
    public static Battle makeBattle(Pokemon p) {
        return new Battle(p);
    }
    public static Battle makeBattle(Pokemon p, BattleOptions options) {
        return new Battle(p, options);
    }
    
    @Override
    public void performAction(Pokemon p) {        
        doBattle(p);
        
        //check for special gym leader badges
        if(Settings.isGS) {
            if (Trainer.getTrainer(0x399C2).equals(opponent)) //falkner zephyr badge
                p.setAtkBadge(true);
            else if (Trainer.getTrainer(0x39A42).equals(opponent)) //jasmine mineral badge
                p.setDefBadge(true);
            else if (Trainer.getTrainer(0x399D8).equals(opponent)) //whitney plain badge
                p.setSpdBadge(true);
            else if (Trainer.getTrainer(0x39A28).equals(opponent)) //pryce glacier badge
                p.setSpcBadge(true);
        } else {
        	if (Trainer.getTrainer(0x39A1F).equals(opponent)) //falkner zephyr badge
                p.setAtkBadge(true);
            else if (Trainer.getTrainer(0x39A9F).equals(opponent)) //jasmine mineral badge
                p.setDefBadge(true);
            else if (Trainer.getTrainer(0x39A35).equals(opponent)) //whitney plain badge
                p.setSpdBadge(true);
            else if (Trainer.getTrainer(0x39A85).equals(opponent)) //pryce glacier badge
                p.setSpcBadge(true);
        }
    }
    
    private void doBattle(Pokemon p) {
        //TODO: automatically determine whether or not to print
        if (opponent instanceof Pokemon) {
            if(getVerbose() == BattleOptions.ALL) printBattle(p, (Pokemon) opponent);
            else if (getVerbose() == BattleOptions.SOME) printShortBattle(p, (Pokemon) opponent);
            
            opponent.battle(p, options);
        } else { //is a Trainer
            Trainer t = (Trainer) opponent;
            if(getVerbose() == BattleOptions.ALL || getVerbose() == BattleOptions.SOME) Main.appendln(t.toString());
            int lastLvl = p.getLevel();
            for(Pokemon opps : t) {
                if(getVerbose() == BattleOptions.ALL) printBattle(p, (Pokemon) opps);
                else if (getVerbose() == BattleOptions.SOME) printShortBattle(p, (Pokemon) opps);
                opps.battle(p, options);
                //test if you leveled up on this pokemon
                if(p.getLevel() > lastLvl) {
                    lastLvl = p.getLevel();
                    if(options.isPrintSRsOnLvl()) {
                        Main.appendln(p.statRanges(false));
                    }
                    if(options.isPrintSRsBoostOnLvl()) {
                        Main.appendln(p.statRanges(true));
                    }
                }
            }
        }
        if(getVerbose() == BattleOptions.ALL || getVerbose() == BattleOptions.SOME) {
            Main.appendln(String.format("LVL: %d EXP NEEDED: %d/%d", p.getLevel(),
                    p.expToNextLevel(), p.expForLevel()));
        }
    }
    
    //does not actually do the battle, just prints summary
    public void printBattle(Pokemon us, Pokemon them) {
        Main.appendln(DamageCalculator.summary(us, them, options));
    }
    
    //does not actually do the battle, just prints short summary
    public void printShortBattle(Pokemon us, Pokemon them) {
        Main.appendln(DamageCalculator.shortSummary(us, them, options));
    }
}

class Encounter extends Battle {
    Encounter(Species s, int lvl, BattleOptions options) {
        super(new Pokemon(s, lvl), options);
    }
    Encounter(String s, int lvl) { this(PokemonNames.getSpeciesFromName(s), lvl, new BattleOptions()); }
    Encounter(String s, int lvl, BattleOptions options) {
        this(PokemonNames.getSpeciesFromName(s), lvl, options);
    }
}

class TrainerPoke extends Battle {
    TrainerPoke(Species s, int lvl, BattleOptions options) {
        super(new Pokemon(s, lvl), options);
        }
    TrainerPoke(String s, int lvl) { this(PokemonNames.getSpeciesFromName(s),lvl, new BattleOptions()); }
    TrainerPoke(String s, int lvl, BattleOptions options) {
        this(PokemonNames.getSpeciesFromName(s), lvl, options);
    }
}
